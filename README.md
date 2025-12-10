# Iwawka Messenger Backend

Iwawka — это бэкенд мессенджера на Kotlin/ Ktor с PostgreSQL (основное хранилище), Redis (кэш/сессии) и ClickHouse (аналитика). Приложение использует Flyway для миграций и может работать как локально через Gradle, так и полностью в Docker.

## Требования
- Docker 24+ и Docker Compose v2
- JDK 21+ 

## Запуск 

### Полный запуск в контейнере

```shell
docker compose --env-file .\.env up --build
```

### Запуск базовых сервисов

```shell
docker compose --env-file .\.env up -d postgres redis clickhouse
```

### Локальный запуск приложения
После запуска базовых сервисов выполните:

Linux/MacOS:
```shell
./gradlew run
```

Windows:
```powershell
./gradlew.bat run
```

Команда выполнит сборку Gradle-проекта, прогонит миграции и откроет API на `http://localhost:8080`.

## Миграции
Flyway автоматически запускается при старте приложения. Скрипты лежат в `src/main/resources/db/migration`. Чтобы отключить миграции (например, для отладки), установите `FLYWAY_ENABLED=false` в `.env`.

## Конфигурация
Основные настройки описаны в `application.conf`, но всегда могут быть переопределены переменными окружения (`DATABASE_POSTGRES_URL`, `SERVER_PORT` и т.д.). Для локального запуска без Docker достаточно убедиться, что PostgreSQL/Redis/ClickHouse доступны по адресам из файла конфигурации.

## JWT Аутентификация

Приложение использует JWT для аутентификации пользователей. Реализованы access и refresh токены.

1. **Регистрация:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"password123"}'
```

2. **Использование токена:**
```bash
curl -X GET http://localhost:8080/api/user/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Защищённые endpoints

Все API endpoints (кроме `/api/auth/*` и публичных `/api/images/{id}`) требуют JWT аутентификацию через заголовок:
```
Authorization: Bearer <access_token>
```

## Проверка работы

```bash
# Проверка здоровья
curl http://localhost:8080/health

# Регистрация пользователя
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@test.com","password":"password123"}'
```

В (`logs/application.log`) появятся записи уровня INFO. Для детального логирования, измените `logging.level` в `application.conf`.
