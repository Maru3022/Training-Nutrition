# Training Nutrition Service

Сервис расчёта питания и трекинга рациона fitness-платформы. Считает КБЖУ по формуле Миффлина — Сан Жеора, хранит дневник приёмов пищи и тренировок, выдаёт персонализированные советы по питанию на основе фактического потребления калорий, а советы по питанию ищет полнотекстовым поиском в Elasticsearch. Участвует в Saga создания пользователя как шаг `NUTRITION`.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-6DB33F?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791?logo=postgresql)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-full--text%20search-005571?logo=elasticsearch)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-spring--kafka-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-cache-DC382D?logo=redis)
![OpenAPI](https://img.shields.io/badge/OpenAPI-springdoc-6BA539?logo=swagger)

## Что делает сервис

- Рассчитывает суточную норму калорий и БЖУ по формуле BMR Миффлина — Сан Жеора с поправкой на уровень активности (`NutritionCalculationService`), исходя из веса, роста, возраста и `activityLevel` пользователя.
- Ведёт дневник тренировок и приёмов пищи (`/api/v1/training/log`, `/api/v1/meal-logs`) с привязкой к `userId`.
- Генерирует персональный совет по питанию (`/api/v1/advice/{userId}`): суммирует калории из дневника пользователя за все записи, относит его к категории `CUTTING`/`MAINTENANCE`/`BULKING` и подбирает подходящий совет.
- Хранит советы по питанию одновременно в PostgreSQL (источник правды) и Elasticsearch (полнотекстовый поиск по `title`/`content`), синхронизируя запись и удаление в обоих хранилищах в рамках одного сервисного метода.
- Участвует в Saga создания пользователя как шаг `NUTRITION`: слушает `saga-nutrition-command`, создаёт (или находит существующий) профиль и отвечает оркестратору через Transactional Outbox; поддерживает компенсацию через удаление профиля.
- Параллельно поддерживает второй, независимый saga-путь (`saga.nutrition.calculate` → `NutritionSagaConsumer`), который выполняет полноценный расчёт по формуле, а не дефолтный профиль — см. предупреждение ниже.


## Архитектура

```text
+------------------+      saga-nutrition-command       +---------------------------+
|  Saga-Orchestrator|----------------------------------->| Training-Nutrition (8083) |
+------------------+      saga-nutrition-response        |                            |
        ^             <----------- (Outbox) -------------|  SagaNutritionCommand-     |
        |                                                 |  Listener / NutritionSaga- |
        |             saga.nutrition.calculate            |  Consumer                  |
        +------------------------------------------------>|                            |
                       saga.nutrition.response             +-------+-----------+-------+
                       <------------------------------             |           |
                                                                    v           v
   Client ---> /api/v1/training/log -----------------------> PostgreSQL   Redis (кэш
   Client ---> /api/v1/meal-logs ---------------------------> (Flyway,     Spring Cache
   Client ---> /api/v1/advice/{userId} ----------------------> JPA)         для tips)
   Client ---> /api/v1/tips, /api/v1/tips/search ------------------------> Elasticsearch
                                                                            (nutrition_tips)
   Admin --->  /api/v1/admin/tips (create/delete) ---> dual-write: PostgreSQL + Elasticsearch
                                                         + Kafka событие в nutrition-topic
```

## Архитектурные решения

### 1. Dual-write в PostgreSQL и Elasticsearch с явной синхронизацией в сервисном слое

`DefaultTipServiceImpl.save()` сначала сохраняет совет в PostgreSQL через JPA (источник правды, `@Transactional`), затем строит `NutritionTipDocument` и индексирует его в Elasticsearch, и только после этого публикует Kafka-событие в `nutrition-topic` о появлении нового совета. Поиск (`searchTips`) идёт исключительно по Elasticsearch (`findByTitleContainingOrContentContaining`), а листинг всех советов — по PostgreSQL с кэшированием через `@Cacheable("tips")`/`@CacheEvict`. Это сознательный компромисс CQRS-style: запись остаётся консистентной относительно реляционной БД, а скорость полнотекстового поиска обеспечивает отдельный индекс, при этом нет распределённой транзакции между Postgres и Elasticsearch — окно рассинхронизации существует, но ограничено временем одного HTTP-запроса.

### 2. Saga-шаг с идемпотентностью и Transactional Outbox (тот же паттерн, что и в Notification-сервисе)

`NutritionSagaConsumer` (топик `saga.nutrition.calculate`) проверяет `existsByCorrelationId` перед расчётом, чтобы повторная доставка Kafka-сообщения не создала дублирующую запись `NutritionResult`. Ответ оркестратору (`saga-nutrition-response` / `saga.nutrition.response`) не пишется в Kafka напрямую — `SagaNutritionCommandListener` сохраняет `OutboxEvent` в той же транзакции, что и изменение `UserNutritionProfile`, а фоновый `OutboxProcessor` асинхронно публикует событие, исключая dual-write между БД и брокером.

### 3. Персонализация совета на основе истории, а не статичного контента

`PersonalizedAdvisorImpl` не отдаёт случайный совет: он подтягивает все `MealLog` пользователя, суммирует калории, относит результат к одной из трёх диетических категорий (`CUTTING` < 1800 ккал, `MAINTENANCE` < 2600 ккал, `BULKING` ≥ 2600 ккал) и ищет среди сохранённых советов первый, у которого совпадает категория. Логика простая (линейный поиск по списку, без ML), но она реально завязана на фактическое поведение пользователя, а не на статичном шаблоне — это стоит подчеркнуть отдельно от заглушки в saga-пути выше.

## API-эндпоинты

| Метод | Путь | Контроллер | Описание |
|---|---|---|---|
| GET | `/api/v1/advice/{userId}` | `AdviceController` | Персональный совет по питанию на основе дневника приёмов пищи |
| POST | `/api/v1/training/log` | `TrainingController` | Залогировать тренировку (тип упражнения, длительность) |
| POST | `/api/v1/meal-logs` | `MealLogController` | Залогировать приём пищи |
| GET | `/api/v1/meal-logs/{userId}` | `MealLogController` | Список приёмов пищи пользователя |
| GET | `/api/v1/tips` | `NutritionTipController` | Список всех советов по питанию (кэшируется в Redis) |
| POST | `/api/v1/tips` | `NutritionTipController` | Создать совет (dual-write PostgreSQL + Elasticsearch) |
| GET | `/api/v1/tips/search?term=` | `NutritionTipController` | Полнотекстовый поиск советов по Elasticsearch |
| POST | `/api/v1/admin/tips` | `AdminTipController` | Административное создание совета |
| DELETE | `/api/v1/admin/tips/{id}` | `AdminTipController` | Удаление совета (PostgreSQL + Elasticsearch) |

Документация OpenAPI/Swagger доступна на `/swagger-ui.html` (springdoc), спецификация — на `/api-docs`.

## Технологический стек

| Категория | Технологии |
|---|---|
| Язык / платформа | Java 21, Spring Boot 3.4.2, Spring Cloud 2024.0.0 |
| Данные | PostgreSQL + Flyway, Elasticsearch (Spring Data Elasticsearch), Redis (Spring Cache) |
| Messaging | Apache Kafka, Spring Kafka |
| Service discovery | Netflix Eureka Client |
| API | Spring Web + WebFlux, Bean Validation, springdoc-openapi (Swagger UI) |
| Тестирование | JUnit 5, Mockito, Spring Kafka Test, H2 |
| CI/CD | GitHub Actions: тесты, Trivy (FS + image), сборка и публикация образа в GHCR, условный деплой в Kubernetes по наличию `KUBE_CONFIG` |
| Контейнеризация | Docker (multi-stage build, `eclipse-temurin:21-jre-alpine`, non-root, `HEALTHCHECK`) |
| Деплой | Kubernetes-манифесты (`k8s/deployment.yaml`, `k8s/service.yaml`) |

## Локальный запуск

### Зависимости

JDK 21+, Maven Wrapper, PostgreSQL, Redis, Elasticsearch, Kafka, Eureka Server (опционально).

### Переменные окружения

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nutrition_db
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=mypassword
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_ELASTICSEARCH_URIS=http://localhost:9200
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

### Сборка и тесты

```bash
./mvnw clean verify
```

### Запуск

```bash
./mvnw spring-boot:run
```

Сервис поднимется на `localhost:8083`. Swagger UI — `/swagger-ui.html`, health — `/actuator/health`.

## Связанные репозитории

- [Saga-Orchestrator](https://github.com/Maru3022/Saga-Orchestrator) — оркестратор саги создания пользователя, источник команд `saga-nutrition-command`
- [Training_Notification](https://github.com/Maru3022/Training_Notification) — соседний шаг саги, доставка уведомлений
- [Trains-Service](https://github.com/Maru3022/Trains-Service) — источник данных о тренировках
- [Eureka-server](https://github.com/Maru3022/Eureka-server) — service discovery для всей платформы
