# Training-Nutrition: Полная документация проекта

## 1) Что это за проект

`Training-Nutrition` — это backend-приложение на `Spring Boot 3` для хранения и выдачи советов по питанию, с поддержкой:

- транзакционного хранения данных в `PostgreSQL` через `Spring Data JPA`;
- полнотекстового поиска по советам в `Elasticsearch`;
- кэширования списка советов через `Spring Cache` (`Redis` как провайдер);
- асинхронной отправки событий через `Kafka`;
- базового UI в виде статических HTML-страниц;
- unit/integration-style тестов и скриптов нагрузочного тестирования `k6`.

Проект демонстрирует архитектуру с несколькими инфраструктурными компонентами (DB + Cache + Search + Messaging), но бизнес-логика остается достаточно компактной и понятной.

---

## 2) Технологии и зависимости

Основной стек (из `pom.xml`):

- `Java 21`
- `Spring Boot 3.4.2`
- `spring-boot-starter-web` (REST)
- `spring-boot-starter-webflux` (подключен, но в текущем коде реактивные контроллеры не используются)
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-data-redis`
- `spring-boot-starter-data-elasticsearch`
- `spring-kafka`
- `springdoc-openapi` (Swagger UI)
- `Lombok`
- `PostgreSQL` (runtime)
- `H2` (test scope)
- `spring-boot-starter-test` (JUnit 5, Mockito)

---

## 3) Архитектура проекта (слои)

Пакеты:

- `controller` — REST API
- `service` — бизнес-логика
- `repository` — доступ к данным (`jpa` и `elastic`)
- `domain` — модели данных (JPA entities + Elasticsearch document)
- `dto` — DTO-классы (часть пока не используется в контроллерах)
- `config` — конфигурация инфраструктуры

Принцип:

1. Контроллер принимает HTTP-запрос.
2. Передает в сервис.
3. Сервис работает с JPA и/или Elasticsearch, кэшем, Kafka.
4. Возвращается результат в JSON.

---

## 4) Доменные модели

### 4.1 `NutritionTipEntity` (JPA)

Таблица `nutrition_tips`:

- `id: Long`
- `title: String`
- `content: String`
- `category: String`

Это основная сущность советов питания, которая используется в REST API и хранится в PostgreSQL.

### 4.2 `NutritionTipDocument` (Elasticsearch)

Индекс `nutrition_tips`:

- `id: String` (важно: строковый id, обычно из `NutritionTipEntity.id`)
- `title: Text`
- `content: Text`
- `category: Keyword`

Используется для поиска по `title`/`content`.

### 4.3 `MealLog` (JPA)

Сущность логов питания:

- `id`
- `foodName`
- `calories`
- `protein`
- `carbs`
- `fats`
- `consumedAt`
- `userId`

Используется сервисами рекомендаций и трекинга.

### 4.4 Дополнительные сущности

- `Ingredient` — таблица `ingredients`
- `DietaryRecommendation` — таблица `dietary_recommendation`

Эти модели в текущей версии API напрямую не экспонируются контроллерами, но заложены как расширение доменной области.

---

## 5) Репозитории и хранение данных

### JPA

- `NutritionTipRepository extends JpaRepository<NutritionTipEntity, Long>`
- `MealLogRepository extends JpaRepository<MealLog, Long>`
  - кастомный метод: `findByUserId(String userId)`

### Elasticsearch

- `NutritionTipSearchRepository extends ElasticsearchRepository<NutritionTipDocument, String>`
  - кастомный метод:
    - `findByTitleContainingOrContentContaining(String title, String content)`

---

## 6) Сервисный слой (бизнес-логика)

## `DefaultTipServiceImpl` (главный сервис советов)

Реализует интерфейс `TipService`.

### При `save(tip)`:

1. Сохраняет сущность в PostgreSQL.
2. Преобразует ее в `NutritionTipDocument`.
3. Сохраняет документ в Elasticsearch.
4. Отправляет событие в Kafka:
   - topic: `new-tip`
   - message: `Добавлен новый совет: <title>`
5. Очищает кэш `tips` (`@CacheEvict`).

### Другие методы:

- `getAllTips()` — читает все советы из JPA, кэшируется через `@Cacheable("tips")`.
- `getTipById(id)` — получение по id (при отсутствии бросает `RuntimeException`).
- `searchTips(term)` — поиск в Elasticsearch.
- `deleteById(id)` — удаление из JPA + очистка кэша.

### Важно:

- При удалении tip из JPA документ в Elasticsearch в текущем коде **не удаляется** (возможна рассинхронизация).
- События публикуются в `new-tip`, а consumers слушают `nutrition-topic` (см. раздел “Особенности и риски”).

## `MealLogServiceImpl`

- `saveLog()` — выставляет `consumedAt = now()` и сохраняет в JPA.
- `findByUserId()` и `getLogsByUserId()` — оба возвращают логи пользователя.

## `PersonalizedAdvisorImpl` (генератор персонального совета)

Алгоритм:

1. Берет все `MealLog` пользователя.
2. Суммирует калории.
3. Выбирает категорию:
   - `< 1500` -> `BULKING`
   - `< 2500` -> `CUTTING`
   - `>= 2500` -> `MAINTENANCE`
4. Ищет любой совет этой категории в `tipService.getAllTips()`.
5. Возвращает строку с пояснением или fallback:
   - `Keep maintaining a balanced diet and stay hydrated!`

## `TrainingService`

- Формирует текст о тренировке и отправляет в Kafka topic `training-topic`.
- Полезен как точка интеграции с event-driven pipeline.

## `KafkaProducerService`

- Обертка над `KafkaTemplate<String, Object>`.
- Универсальный метод `sendMessage(topic, message)`.

## Consumers

- `KafkaConsumerService` слушает `nutrition-topic`.
- `NotificationService` слушает `nutrition-topic` и имитирует push-уведомление (лог).

---

## 7) REST API

Базовый префикс: `/api/v1`

## 7.1 `NutritionTipController`

Маршрут: `/api/v1/tips`

- `GET /api/v1/tips`
  - Возвращает `List<NutritionTipEntity>`.
- `POST /api/v1/tips`
  - Создает совет (`NutritionTipEntity` в body).
  - Дополнительно синхронизирует в Elastic и публикует Kafka-событие.
- `GET /api/v1/tips/search?term=...`
  - Поиск в Elasticsearch, возвращает `List<NutritionTipDocument>`.

## 7.2 `AdminTipController`

Маршрут: `/api/v1/admin/tips`

- `POST /api/v1/admin/tips`
  - Создание совета, ответ `201 Created`.
- `DELETE /api/v1/admin/tips/{id}`
  - Удаление совета, ответ `204 No Content`.

## Swagger/OpenAPI

- UI: `http://localhost:8083/swagger-ui.html`
- JSON docs: `http://localhost:8083/api-docs`

---

## 8) Конфигурация приложения

Файл: `src/main/resources/application.properties`

Ключевые параметры:

- `server.port=8083`
- Docker compose integration включен:
  - `spring.docker.compose.enabled=true`
  - `spring.docker.compose.file=compose.yaml`
- PostgreSQL:
  - `jdbc:postgresql://localhost:5434/mydatabase`
- Redis:
  - `localhost:6381`
- Elasticsearch:
  - `http://localhost:9200`
  - включается флагом `app.elasticsearch.enabled=true`
- Kafka:
  - `localhost:9094`

Логирование:

- SQL и Elasticsearch debug-level включены.

---

## 9) Docker и инфраструктура

`compose.yaml` поднимает:

- `postgres` (`5434 -> 5432`)
- `redis` (`6381 -> 6379`)
- `elasticsearch` (`9200`)
- `zookeeper` (`2181`)
- `kafka` (`9094`)

`Dockerfile`:

1. Build stage (`maven:3.9.9 + temurin 21`), `mvn clean package -DskipTests`.
2. Runtime stage (`eclipse-temurin:21-jre-alpine`), запуск от non-root user.
3. `EXPOSE 8083`.
4. Healthcheck на `/actuator/health` (но actuator dependency в `pom.xml` не подключен).

---

## 10) Frontend-страницы (статические)

Расположение: `src/main/resources/static`

- `index.html` — dashboard:
  - читает советы через `GET /api/v1/tips`;
  - “сохранение приема пищи” на самом деле делает `POST /api/v1/tips` и создает nutrition-tip с категорией `USER_LOG`.
- `indexElastic.html` — страница поиска:
  - вызывает `GET /api/v1/tips/search?term=...`.

Это не полноценный frontend (без сборки, роутинга и т.п.), а удобная демо-панель для ручной проверки API.

---

## 11) Тестирование

### 11.1 Unit tests (Mockito)

- `DefaultTipServiceImplTest`
  - проверяет сохранение в JPA/Elastic и отправку Kafka-события.
- `MealLogServiceImplTest`
  - проверяет, что проставляется `consumedAt`.
- `PersonalizedAdvisorImplTest`
  - проверяет выбор совета категории `CUTTING` при 2000 kcal.
- `TrainingServiceTest`
  - проверяет отправку правильного сообщения в `training-topic`.

### 11.2 Spring context test

- `TrainingNutritionApplicationTests`
  - проверяет загрузку контекста;
  - отключает часть авто-конфигураций (Elastic/Redis/Kafka) и мокает зависимости.

### 11.3 Test profile

`application-test.properties`:

- Docker Compose отключен;
- Elasticsearch отключен;
- БД = in-memory H2 (режим PostgreSQL);
- кэш `simple`;
- Kafka auto-startup отключен.

### 11.4 Нагрузочное тестирование (`k6`)

- `k6/Stress-test.js`:
  - сценарий до `20 000` виртуальных пользователей;
  - целевой endpoint: `GET /api/v1/tips`;
  - thresholds:
    - `http_req_failed < 10%`
    - `p95 < 500ms`
- `k6/debug.js`:
  - простой smoke/debug запрос.

---

## 12) CI/CD (GitHub Actions)

Workflow: `.github/workflows/main.yml`

Пайплайн содержит несколько стадий:

1. `build-and-test` — unit tests + package + upload JAR.
2. `code-quality` — placeholder security scan (имитация).
3. `docker-build-push` — подготовка docker build/push (частично placeholder).
4. `deploy-staging` — для `develop`.
5. `integration-tests` — после staging.
6. `deploy-production` — для `main`.
7. `notify` — финальное уведомление.

Часть шагов сейчас демонстрационные (`echo`, `sleep`), то есть это каркас для дальнейшего продакшн-усиления.

---

## 13) Потоки данных (как всё связано)

### Поток A: создание nutrition tip

1. Клиент вызывает `POST /api/v1/tips` или `/api/v1/admin/tips`.
2. `DefaultTipServiceImpl.save()`:
   - пишет в PostgreSQL (`nutrition_tips`);
   - индексирует в Elasticsearch (`nutrition_tips`);
   - публикует Kafka event в `new-tip`;
   - очищает кэш `tips`.
3. Повторный `GET /api/v1/tips` заполнит кэш заново.

### Поток B: поиск советов

1. Клиент вызывает `GET /api/v1/tips/search?term=...`.
2. Поиск идет напрямую в Elasticsearch-индекс.
3. Возвращаются документы `NutritionTipDocument`.

### Поток C: рекомендации пользователя

1. `PersonalizedAdvisorImpl` берет meal logs пользователя.
2. Считает общий caloric intake.
3. Выбирает категорию и соответствующий совет.
4. Возвращает готовый текст рекомендации.

### Поток D: лог тренировки

1. `TrainingService.logWorkout(...)` формирует сообщение.
2. Публикует в `training-topic`.
3. Дальнейшая обработка внешними/внутренними consumer-сервисами (в проекте базовая заготовка).

---

## 14) Особенности, ограничения и риски текущей реализации

1. **Kafka topic mismatch**
   - producer в `DefaultTipServiceImpl` пишет в `new-tip`;
   - consumers слушают `nutrition-topic`;
   - итог: события о новых советах не будут обработаны текущими listeners без унификации topic.

2. **Удаление из Elastic не синхронизировано**
   - `deleteById` удаляет только из JPA, не удаляет индексный документ.

3. **Дубликат методов в `MealLogService`**
   - есть и `findByUserId`, и `getLogsByUserId` с одинаковой сутью.

4. **DTO пока почти не используются**
   - `TipResponse`, `UserPreferenceDTO` есть, но контроллеры работают напрямую с entity.

5. **Healthcheck в Dockerfile**
   - использует `/actuator/health`, но actuator-starter не добавлен.

6. **WebFlux dependency**
   - подключена, но реактивный стек в коде не задействован.

7. **Валидация входных данных**
   - в контроллерах нет `@Valid` и ограничений полей (например, пустые title/content допустимы).

---

## 15) Как запустить проект локально

### Вариант 1: через Spring Boot + auto docker compose

1. Убедиться, что Docker Desktop запущен.
2. Запустить приложение:
   - Windows: `mvnw.cmd spring-boot:run`
3. Проверить:
   - API: `http://localhost:8083/api/v1/tips`
   - Swagger: `http://localhost:8083/swagger-ui.html`
   - Search UI: `http://localhost:8083/indexElastic.html`

### Вариант 2: вручную

1. Поднять инфраструктуру:
   - `docker compose -f compose.yaml up -d`
2. Собрать приложение:
   - `mvnw.cmd clean package`
3. Запустить:
   - `java -jar target/<jar-file>.jar`

---

## 16) Полезные примеры API-запросов

### Создать совет

`POST /api/v1/tips`

Body:

```json
{
  "title": "Protein Recovery",
  "content": "Consume 25-30g protein after workout",
  "category": "MAINTENANCE"
}
```

### Получить все советы

`GET /api/v1/tips`

### Поиск в Elastic

`GET /api/v1/tips/search?term=protein`

### Удалить совет

`DELETE /api/v1/admin/tips/1`

---

## 17) Направления развития (рекомендуемый roadmap)

1. Выровнять Kafka topics (`new-tip` vs `nutrition-topic`) и формат событий.
2. Сделать двустороннюю синхронизацию JPA <-> Elastic при delete/update.
3. Ввести DTO на вход/выход + валидацию (`@Valid`, constraints).
4. Добавить глобальный exception handling (`@ControllerAdvice`).
5. Покрыть интеграционными тестами реальные сценарии REST + DB + Kafka (testcontainers).
6. Укрепить security (authN/authZ, CORS policy, rate limiting).
7. Доработать CI/CD: реальные шаги сканирования, docker push/deploy, integration checks.

---

## 18) Краткий итог для онбординга

Если нужно понять проект “с нуля”:

1. Начните с `TipService`/`DefaultTipServiceImpl` — это центр логики.
2. Посмотрите контроллеры `NutritionTipController` и `AdminTipController`.
3. Проверьте настройки `application.properties` и `compose.yaml`.
4. Прогоните unit-тесты и вызовите API через Swagger.
5. После этого переходите к Kafka/Elastic улучшениям из roadmap.

В текущем виде проект хорошо подходит как учебно-практический шаблон high-load backend с несколькими инфраструктурными интеграциями и базовой демонстрацией event-driven подхода.
