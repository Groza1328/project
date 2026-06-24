# Деплой СибМобиль на Railway.app

Эта папка — **отдельная копия** приложения, подготовленная для хостинга.  
База данных и веб-приложение работают как **два разных сервиса** на Railway и не конфликтуют с локальной PostgreSQL на вашем компьютере.

## Архитектура на Railway

```
┌─────────────────────┐      переменные PGHOST, PGPORT…      ┌─────────────────────┐
│  Web Service        │ ───────────────────────────────────► │  PostgreSQL         │
│  (Spring Boot)      │                                      │  (отдельный сервис) │
│  Dockerfile         │                                      │                     │
└─────────────────────┘                                      └─────────────────────┘
```

Локально у вас может быть PostgreSQL на `localhost:5432`.  
На Railway — **своя** база в облаке, приложение подключается только через переменные окружения.

## Быстрый старт

### 1. Репозиторий

Загрузите содержимое папки `Хост` в GitHub (только эту папку, как корень репозитория).

### 2. Проект на Railway

1. Зайдите на [railway.app](https://railway.app)
2. **New Project** → **Deploy from GitHub repo**
3. Выберите репозиторий

### 3. PostgreSQL (отдельно от приложения)

1. В проекте Railway: **+ New** → **Database** → **PostgreSQL**
2. Откройте Web-сервис → **Variables** → **Add Reference**
3. Подключите переменные из PostgreSQL: `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`

### 4. Переменные Web-сервиса

| Переменная | Значение |
|------------|----------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `MAIL_USERNAME` | ваш Gmail |
| `MAIL_PASSWORD` | пароль приложения Gmail |
| `MAIL_FROM` | тот же email |

Переменные `PG*` Railway добавит сам при связке с PostgreSQL.

### 5. Деплой

Railway соберёт проект по `Dockerfile` и запустит JAR.  
Проверка здоровья: `/actuator/health`

Публичный URL: **Settings** → **Networking** → **Generate Domain**

## Локальный запуск (из папки Хост)

1. Скопируйте `application-local.properties.example` → `application-local.properties`
2. Укажите пароль локальной БД и почты
3. Запуск:

```bash
./mvnw spring-boot:run
```

Профиль `local` используется по умолчанию и **не трогает** Railway.

## Файлы конфигурации

| Файл | Назначение |
|------|------------|
| `application.properties` | Общие настройки, порт, почта через env |
| `application-prod.properties` | Railway: БД через `PGHOST` и др. |
| `application-local.properties` | Локальная БД (не коммитить!) |
| `Dockerfile` | Сборка для Railway |
| `railway.toml` | Healthcheck и политика перезапуска |

## Админ по умолчанию

После первого запуска создаётся:

- Логин: `Admin777`
- Пароль: `Admin123`

**Смените пароль после деплоя в production.**

## Изображения авто

Положите файлы `Auto1.jpg`, `Auto2.jpg`, `Auto3.jpg` в:

```
src/main/resources/static/images/
```

## Частые проблемы

**Приложение не стартует — ошибка БД**  
Убедитесь, что PostgreSQL-сервис связан с Web-сервисом и переменные `PGHOST`, `PGUSER` видны в Variables.

**Письма не отправляются**  
Проверьте `MAIL_USERNAME` и `MAIL_PASSWORD` (пароль приложения Google, не обычный пароль).

**502 / healthcheck failed**  
Подождите 2–3 минуты после первого деплоя — Hibernate создаёт таблицы в новой БД.
