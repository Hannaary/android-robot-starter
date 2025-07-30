# Android Robot Starter

Этот проект демонстрирует создание и использование Spring Boot Starter‑модуля для приёма и обработки «команд» (Command) с очередью, метриками и аудитом.

## Структура проекта

```
android-robot-starter/            # корневой POM
 ├─ starter/                     # библиотечный модуль-стартер
 └─ prototype/                   # демонстрационное приложение
```

### Модуль `starter`

- **Авто‑конфигурация** (`RobotAutoConfiguration`):
  - `CommandService` для приёма и выполнения команд
  - `MetricsPublisher` для публикации метрик в Micrometer
  - `AuditAspect` для AOP‑аудита методов, помеченных `@WeylandWatchingYou`
- **Модель **``:
  ```java
  record Command(
    @NotBlank @Size(max = 1000) String description,
    @NotNull Priority priority,
    @NotBlank @Size(max = 100) String author,
    @NotBlank @Pattern(...) String time
  ) {}
  ```
- **Логика обработки**:
  - `CRITICAL` – синхронное выполнение
  - `COMMON` – асинхронно через `ThreadPoolExecutor` (очередь = 100), при переполнении – `QueueFullException`
- **Метрики** (`MetricsPublisher`):
  - `android.commands.total` – счётчик всех команд
  - `android.queue.size` – размер очереди команд
  - `android.author.commands` – счётчик команд по авторам
- **Аудит** (`@WeylandWatchingYou` + `AuditAspect`):
  - логирование до и после вызова метода (режим `CONSOLE` или `KAFKA`)

### Модуль `prototype`

- Демонстрационное Spring Boot-приложение, подключающее модуль `starter`
- REST‑контроллеры:
  - `POST /api/commands` – приём `Command`, валидация (`@Valid`), вызов `CommandService`
  - `GET /api/demo/audit-test` – тест работы аудита
- `GlobalExceptionHandler` для кастомных ответов:
  - `400 Bad Request` при валидации
  - `429 Too Many Requests` при переполнении очереди

## Требования

- Java 21
- Maven
- Spring Boot 3.5.3
- Micrometer Core
- Spring AOP, Validation

## Сборка и запуск

1. Клонируйте репозиторий:
   ```bash
   git clone <repo-url>
   cd android-robot-starter
   ```
2. Соберите и запустите demo-приложение:
   ```bash
   mvn clean package -pl prototype
   mvn spring-boot:run -pl prototype
   ```
3. Приложение будет доступно на `http://localhost:8080`.

## Тестирование API

### 1. Отправка команды

#### CRITICAL (синхронно)

```bash
curl -i -X POST http://localhost:8080/api/commands \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"description":"First command","priority":"CRITICAL","author":"Alice","time":"2025-07-30T12:00:00Z"}'
```

Ожидается `HTTP/1.1 202 Accepted` и в консоли:

```
Executing: Command(description=First command, ...)
```

#### COMMON (асинхронно)

```bash
curl -i -X POST http://localhost:8080/api/commands \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"description":"Async cmd","priority":"COMMON","author":"Bob","time":"2025-07-30T12:00:00Z"}'
```

### 2. Просмотр метрик (Spring Actuator)

```bash
curl -s http://localhost:8080/actuator/metrics/android.queue.size
curl -s http://localhost:8080/actuator/metrics/android.commands.total
curl -s http://localhost:8080/actuator/metrics/android.author.commands
```

### 3. Проверка аудита (AOP)

```bash
curl -i "http://localhost:8080/api/demo/audit-test?input=test123"
```

В логах:

```
[CONSOLE AUDIT] Method: testAudit | Args: [test123] | Result: Processed: TEST123
```

### 4. Тест переполнения очереди

```bash
for i in {1..110}; do
  curl -s -X POST http://localhost:8080/api/commands \
    -H "Content-Type: application/json" \
    -d '{"description":"cmd'$i'","priority":"COMMON","author":"Bob","time":"2025-07-30T12:00:00Z"}' &
done
wait
```

- Первые 100 запросов вернут `202 Accepted`
- Последующие – `429 Too Many Requests` с телом:

```json
{ "code":"Queue overflow", "message":"Queue is full" }
```
