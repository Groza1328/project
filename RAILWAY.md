# Деплой СибМобиль на Railway.app

Папка **Хост** — готовая к публикации версия приложения.  
PostgreSQL на Railway — **отдельный сервис**, не конфликтует с локальной БД.

## Быстрый старт

### 1. GitHub
Загрузите **содержимое папки `Хост`** как корень репозитория.

### 2. Railway
1. [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub**
2. **+ New** → **Database** → **PostgreSQL**
3. В Web-сервисе: **Variables** → **Add Reference** → переменные `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
4. Добавьте переменные:

| Переменная | Значение |
|------------|----------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `MAIL_USERNAME` | Gmail |
| `MAIL_PASSWORD` | пароль приложения |
| `MAIL_FROM` | тот же email |

5. **Settings** → **Networking** → **Generate Domain**

### 3. Перезапуск
После деплоя Hibernate обновит схему БД (`ddl-auto=update`) — появятся колонки `city`, `car_id` и др.

## Файлы для хостинга

| Файл | Назначение |
|------|------------|
| `Dockerfile` | Сборка JAR в контейнере |
| `railway.toml` | Healthcheck `/actuator/health` |
| `.dockerignore` | Исключения при сборке |
| `application.properties` | Общие настройки, `ddl-auto=update` |
| `application-prod.properties` | БД Railway через `PGHOST`… |
| `RailwayDatabaseConfig.java` | Резерв через `DATABASE_URL` |
| `.env.example` | Шаблон переменных |

## Локальный запуск

```powershell
copy src\main\resources\application-local.properties.example src\main\resources\application-local.properties
# укажите DB_PASSWORD и почту
.\mvnw.cmd spring-boot:run
```

## Админ по умолчанию
- Логин: `Admin777`
- Пароль: `Admin123`

## Изображения
Добавьте в `src/main/resources/static/images/`:
- `Auto1.jpg`, `Auto2.jpg`, `Auto3.jpg`
- `lobbiD.jpg`, `lobbiN.jpg` (фон главной)

## Если БД не обновилась

В PostgreSQL на Railway выполните:

```sql
ALTER TABLE cars ADD COLUMN IF NOT EXISTS city VARCHAR(255) NOT NULL DEFAULT 'Омск';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS car_id BIGINT REFERENCES cars(id);
```

Затем перезапустите Web-сервис.
