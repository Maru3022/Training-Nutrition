# Подробная документация по микросервису `Training-Nutrition` (метод за методом)

## 1. Назначение микросервиса

Микросервис хранит и выдает советы по питанию, умеет искать их через Elasticsearch, кэшировать выдачу, публиковать события в Kafka и вести базовые логики трекинга питания/тренировок.

---

## 2. Карта модулей

- `controller` — REST точки входа
- `service` — бизнес-логика
- `repository` — JPA/Elasticsearch доступ к данным
- `domain` — модели хранения
- `dto` — вспомогательные DTO
- `config` — конфигурация инфраструктуры
- `test` — unit и контекстные тесты

---

## 3. Подробно: каждый класс и каждый метод

Ниже перечислены **все явно объявленные методы** в коде (ручные методы).  
Lombok-методы (`get/set/toString/equals/hashCode`, конструкторы) генерируются автоматически и отдельно не расписываются.

## 3.1 Entry point и конфигурация

### `TrainingNutritionApplication`

**Метод:** `public static void main(String[] args)`
- **Где:** `com.example.trainingnutrition.TrainingNutritionApplication`
- **Назначение:** старт Spring Boot приложения.
- **Что делает:** вызывает `SpringApplication.run(...)`, поднимает IoC-контекст, автоконфигурации, бин-граф.
- **Вход:** `args` — аргументы командной строки JVM.
- **Выход:** отсутствует (`void`).
- **Побочные эффекты:** запуск web-сервера и всех подключенных инфраструктурных клиентов.

### `ElasticsearchConfig`

Ручных методов нет.  
Класс включает elastic-репозитории только при `app.elasticsearch.enabled=true` (или по умолчанию, если параметр не задан).

---

## 3.2 REST-контроллеры

### `NutritionTipController`

Базовый путь: `/api/v1/tips`.

**Метод:** `public List<NutritionTipEntity> getAllTips()`
- **HTTP:** `GET /api/v1/tips`
- **Назначение:** вернуть все советы из SQL (через сервис и кэш).
- **Вход:** нет.
- **Выход:** список `NutritionTipEntity`.
- **Внутренний вызов:** `tipService.getAllTips()`.
- **Особенности:** данные возвращаются в entity-форме без отдельного response DTO.

**Метод:** `public NutritionTipEntity createTip(@RequestBody NutritionTipEntity tip)`
- **HTTP:** `POST /api/v1/tips`
- **Назначение:** создать совет по питанию.
- **Вход:** JSON, маппится в `NutritionTipEntity`.
- **Выход:** созданная сущность (включая сгенерированный `id`).
- **Внутренний вызов:** `tipService.save(tip)`.
- **Побочные эффекты:** запись в JPA + индекс в Elasticsearch + Kafka publish + cache evict.
- **Ограничения:** валидация (`@Valid`) сейчас не подключена.

**Метод:** `public List<NutritionTipDocument> search(@RequestParam String term)`
- **HTTP:** `GET /api/v1/tips/search?term=...`
- **Назначение:** полнотекстовый поиск советов.
- **Вход:** query-параметр `term`.
- **Выход:** список документов `NutritionTipDocument`.
- **Внутренний вызов:** `tipService.searchTips(term)`.

### `AdminTipController`

Базовый путь: `/api/v1/admin/tips`.

**Метод:** `public ResponseEntity<NutritionTipEntity> create(@RequestBody NutritionTipEntity nutritionTipEntity)`
- **HTTP:** `POST /api/v1/admin/tips`
- **Назначение:** административное создание совета.
- **Вход:** `NutritionTipEntity`.
- **Выход:** `201 Created` + тело созданной сущности.
- **Внутренний вызов:** `tipService.save(...)`.
- **Побочные эффекты:** те же, что и в обычном `createTip`.

**Метод:** `public ResponseEntity<Void> delete(@PathVariable Long id)`
- **HTTP:** `DELETE /api/v1/admin/tips/{id}`
- **Назначение:** удалить совет по id.
- **Вход:** path variable `id`.
- **Выход:** `204 No Content`.
- **Внутренний вызов:** `tipService.deleteById(id)`.
- **Важно:** в текущей реализации удаление в Elasticsearch не выполняется автоматически (удаляется только SQL-запись).

---

## 3.3 Контракты (интерфейсы)

### `TipService`

**Метод:** `NutritionTipEntity save(NutritionTipEntity tip)`
- Контракт создания/сохранения совета.

**Метод:** `List<NutritionTipEntity> getAllTips()`
- Контракт получения всех советов.

**Метод:** `NutritionTipEntity getTipById(Long id)`
- Контракт получения совета по идентификатору.

**Метод:** `List<NutritionTipDocument> searchTips(String term)`
- Контракт поиска советов в поисковом индексе.

**Метод:** `void deleteById(Long id)`
- Контракт удаления совета.

### `MealLogService`

**Метод:** `MealLog saveLog(MealLog mealLog)`
- Контракт сохранения записи о приеме пищи.

**Метод:** `List<MealLog> getLogsByUserId(String userId)`
- Контракт получения логов по пользователю.

**Метод:** `List<MealLog> findByUserId(String userId)`
- Альтернативный контракт получения логов (дублирует смысл предыдущего).

### `NutritionAdvisor`

**Метод:** `String generateDailyAdvice(String userId)`
- Контракт генерации персонализированного текстового совета на основе intake.

---

## 3.4 Реализации сервисов (основная логика)

### `DefaultTipServiceImpl implements TipService`

**Метод:** `public NutritionTipEntity save(NutritionTipEntity tip)`
- **Назначение:** централизованное создание совета со всеми интеграциями.
- **Шаги выполнения:**
  1. Сохраняет `tip` в `NutritionTipRepository` (PostgreSQL).
  2. Создает `NutritionTipDocument` и копирует поля.
  3. Сохраняет документ в `NutritionTipSearchRepository` (Elasticsearch).
  4. Публикует событие в Kafka: topic `new-tip`.
  5. Возвращает сохраненную JPA-сущность.
- **Аннотации:** `@Transactional`, `@CacheEvict(value = "tips", allEntries = true)`.
- **Почему так:** чтобы API сразу работало и на SQL-выдачу, и на поиск, и на event-сигнал.
- **Риски:** если часть шагов упадет после SQL-save, возможна частичная рассинхронизация SQL/Elastic/Kafka.

**Метод:** `public List<NutritionTipEntity> getAllTips()`
- **Назначение:** вернуть полный список советов из SQL.
- **Вызов:** `jpaRepository.findAll()`.
- **Аннотация:** `@Cacheable("tips")`.
- **Эффект:** повторные вызовы читаются из кэша до cache-evict.

**Метод:** `public NutritionTipEntity getTipById(Long id)`
- **Назначение:** получить один совет по id.
- **Вызов:** `jpaRepository.findById(id)`.
- **Исключение:** `RuntimeException("Tip not found: " + id)`, если запись отсутствует.

**Метод:** `public List<NutritionTipDocument> searchTips(String term)`
- **Назначение:** поиск по title/content в Elasticsearch.
- **Вызов:** `elasticRepository.findByTitleContainingOrContentContaining(term, term)`.
- **Выход:** список документов индекса.

**Метод:** `public void deleteById(Long id)`
- **Назначение:** удалить совет из SQL.
- **Вызов:** `jpaRepository.deleteById(id)`.
- **Аннотация:** `@CacheEvict(value = "tips", allEntries = true)`.
- **Важно:** elastic cleanup не выполняется.

### `MealLogServiceImpl implements MealLogService`

**Метод:** `public MealLog saveLog(MealLog mealLog)`
- **Назначение:** сохранить лог приема пищи.
- **Что делает:** проставляет `consumedAt = LocalDateTime.now()`, затем `save(...)`.
- **Зачем timestamp тут:** чтобы клиент не обязан был передавать время вручную.

**Метод:** `public List<MealLog> findByUserId(String userId)`
- **Назначение:** получить все meal logs пользователя.
- **Вызов:** `mealLogRepository.findByUserId(userId)`.

**Метод:** `public List<MealLog> getLogsByUserId(String userId)`
- **Назначение:** то же, что `findByUserId`, но с логированием.
- **Вызов:** `mealLogRepository.findByUserId(userId)`.
- **Комментарий:** присутствует дублирование интерфейса.

### `PersonalizedAdvisorImpl implements NutritionAdvisor`

**Метод:** `public String generateDailyAdvice(String userId)`
- **Назначение:** вычислить текст рекомендации по суммарной калорийности.
- **Алгоритм:**
  1. Берет логи пользователя: `mealLogService.getLogsByUserId(userId)`.
  2. Суммирует `calories`.
  3. Вычисляет категорию через `determineCategory(totalCalories)`.
  4. Берет все советы `tipService.getAllTips()`.
  5. Фильтрует по `category` (ignoring case), берет любой подходящий.
  6. Формирует текст: `Based on your intake (...)`.
  7. Если ничего не найдено — fallback-строка.
- **Выход:** готовая строка рекомендации.

**Метод:** `private String determineCategory(int calories)`
- **Назначение:** маппинг калорийности в категорию цели.
- **Правила:**
  - `< 1500` -> `BULKING`
  - `< 2500` -> `CUTTING`
  - `>= 2500` -> `MAINTENANCE`
- **Замечание:** логика категорий сейчас нестандартная по спортивным смыслам, но зафиксирована тестом.

### `TrainingService`

**Метод:** `public void logWorkout(String userId, String exerciseType, int durationMinutes)`
- **Назначение:** записать событие о тренировке в Kafka.
- **Что делает:**
  1. Логирует факт вызова.
  2. Формирует текст сообщения.
  3. Публикует в topic `training-topic`.
- **Выход:** `void`.
- **Роль:** интеграционный адаптер в event-driven поток.

### `KafkaProducerService`

**Метод:** `public void sendMessage(String topic, Object message)`
- **Назначение:** единая точка публикации в Kafka.
- **Вызов:** `kafkaTemplate.send(topic, message)`.
- **Гибкость:** payload как `Object` позволяет отправлять строки/DTO, но снижает строгость контракта.

### `KafkaConsumerService`

**Метод:** `public void consume(Object message)`
- **Аннотация:** `@KafkaListener(topics = "nutrition-topic")`
- **Назначение:** базовый consumer для событий nutrition-топика.
- **Текущее поведение:** только логирует входящее сообщение.

### `NotificationService`

**Метод:** `public void handleNutritionEvent(Object message)`
- **Аннотация:** `@KafkaListener(topics = "nutrition-topic", groupId = "notification-group")`
- **Назначение:** реакция на nutrition-события.
- **Что делает:** лог + вызов `sendPushNotification(...)`.

**Метод:** `private void sendPushNotification(String title, String body)`
- **Назначение:** заглушка отправки push-уведомлений.
- **Текущее поведение:** логирует “уведомление отправлено”.
- **Прод-расширение:** здесь обычно интеграция с внешним push/email/sms провайдером.

---

## 3.5 Репозитории и их методы

### `MealLogRepository extends JpaRepository<MealLog, Long>`

**Метод:** `List<MealLog> findByUserId(String userId)`
- **Назначение:** выборка всех логов питания по пользователю.
- **Как работает:** Spring Data автоматически генерирует SQL по имени метода.

### `NutritionTipRepository extends JpaRepository<NutritionTipEntity, Long>`

Собственных методов не объявлено. Используются стандартные:
- `save`
- `findAll`
- `findById`
- `deleteById`
- и др. методы `JpaRepository`.

### `NutritionTipSearchRepository extends ElasticsearchRepository<NutritionTipDocument, String>`

**Метод:** `List<NutritionTipDocument> findByTitleContainingOrContentContaining(String title, String content)`
- **Назначение:** полнотекстовый поиск по двум полям.
- **Как работает:** Spring Data Elasticsearch строит запрос из имени метода.
- **Использование в сервисе:** передается один и тот же `term` в оба параметра.

---

## 3.6 DTO и модели (методы)

### `TipResponse`
Ручных методов нет (только Lombok-generated).

### `UserPreferenceDTO`
Ручных методов нет (только Lombok-generated).

### `Ingredient`, `DietaryRecommendation`, `MealLog`, `NutritionTipEntity`, `NutritionTipDocument`
Ручных методов нет (кроме аннотаций и полей).

---

## 3.7 Тесты (метод за методом)

### `TrainingNutritionApplicationTests`

**Метод:** `void contextLoads()`
- **Назначение:** smoke-проверка загрузки Spring-контекста.
- **Особенность:** через свойства и моки отключает/замещает внешние интеграции.

### `DefaultTipServiceImplTest`

**Метод:** `void save_ShouldSaveAllSystems()`
- **Проверяет:** при `save` сервис:
  - пишет в JPA,
  - пишет в Elasticsearch,
  - отправляет сообщение в Kafka topic `new-tip`,
  - возвращает сущность с id.

### `MealLogServiceImplTest`

**Метод:** `void saveLog_ShouldSetTimestamp()`
- **Проверяет:** `saveLog` выставляет `consumedAt` и вызывает репозиторий.

### `PersonalizedAdvisorImplTest`

**Метод:** `void generateDailyAdvice_ShouldSelectCuttingCategory()`
- **Проверяет:** при 2000 kcal выбирается совет категории `CUTTING`, и текст содержит совет + kcal.

### `TrainingServiceTest`

**Метод:** `void logWorkout_ShouldSendCorrectMessageToKafka()`
- **Проверяет:** точный текст сообщения и topic `training-topic`.

---

## 4. Сквозные сценарии по шагам

## Сценарий A: создание совета
1. Клиент -> `POST /api/v1/tips`.
2. `NutritionTipController.createTip`.
3. `DefaultTipServiceImpl.save`.
4. PostgreSQL `save`.
5. Elasticsearch `save`.
6. Kafka publish (`new-tip`).
7. Cache evict (`tips`).
8. Возврат созданной сущности.

## Сценарий B: чтение списка советов
1. Клиент -> `GET /api/v1/tips`.
2. Контроллер -> `getAllTips`.
3. Сервис пытается отдать из cache `tips`.
4. При cache-miss читает JPA `findAll`.

## Сценарий C: поиск
1. Клиент -> `GET /api/v1/tips/search`.
2. Контроллер -> `search`.
3. Сервис -> Elastic repository method.
4. Возврат найденных документов.

---

## 5. Конфигурационные параметры, влияющие на методы

- `app.elasticsearch.enabled`  
  Влияет на доступность elastic repository layer.

- `spring.data.redis.*` и `spring.cache.*`  
  Влияют на поведение `@Cacheable/@CacheEvict`.

- `spring.kafka.*`  
  Влияют на `KafkaProducerService` и `@KafkaListener`.

- `spring.datasource.*` + `spring.jpa.*`  
  Влияют на JPA-методы и транзакции.

---

## 6. Важные замечания для разработчиков

1. Topic mismatch:
   - producer новых tips -> `new-tip`;
   - consumers -> `nutrition-topic`.
   Это разные каналы, событие сейчас не дойдет до listeners.

2. `deleteById` не чистит Elasticsearch.

3. Нет единообразного DTO-слоя в контроллерах.

4. Нет централизованного `@ControllerAdvice` для ошибок.

5. `RuntimeException` в `getTipById` лучше заменить на доменное исключение + 404 mapping.

---

## 7. Краткая памятка: где что смотреть

- REST: `controller/*`
- Основная бизнес-логика: `service/nutrition/impl/DefaultTipServiceImpl`
- Персональные рекомендации: `service/nutrition/impl/PersonalizedAdvisorImpl`
- События Kafka: `service/messaging/*`, `service/notification/*`, `service/tracking/TrainingService`
- SQL доступ: `repository/jpa/*`
- Поиск: `repository/elastic/*`
- Тесты: `src/test/java/*`

---

## 8. Итог

Документ выше покрывает каждый ручной метод микросервиса, его контракт, поведение, побочные эффекты, связи и ограничения.  
Для нового разработчика этого достаточно, чтобы:

- быстро понять устройство сервиса;
- безопасно менять логику в конкретных методах;
- видеть точки риска перед релизом.
