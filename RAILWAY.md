# Деплой «СибМобиль» на Railway.app

Эта папка — копия приложения, подготовленная для хостинга. Локальная версия в `sibmobile/` и продакшен на Railway используют **разные базы данных** и не конфликтуют.

## Быстрый старт

1. Зарегистрируйтесь на [railway.app](https://railway.app).
2. Создайте **New Project** → **Deploy from GitHub** (или загрузите папку `Хост` через CLI).
3. В проекте добавьте сервис **PostgreSQL** (Add Service → Database → PostgreSQL).
4. Откройте ваш веб-сервис → **Variables** → **Add Reference** → выберите PostgreSQL и переменные:
   - `PGHOST`
   - `PGPORT`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGDATABASE`
5. Добавьте переменные почты:
   - `MAIL_USERNAME`
   - `MAIL_PASSWORD`
   - `MAIL_FROM` (опционально)
6. Убедитесь, что задано: `SPRING_PROFILES_ACTIVE=prod` (по умолчанию уже prod).
7. Railway соберёт Docker-образ и выдаст публичный URL.

## Как разделены БД

| Окружение | Профиль Spring | База данных |
|-----------|----------------|-------------|
| Локально (`sibmobile/`) | по умолчанию в `application.properties` | `localhost:5432/sibmobile` |
| Хостинг (`Хост/`) | `prod` | PostgreSQL на Railway |

Hibernate (`ddl-auto=update`) создаст таблицы в облачной БД при первом запуске. Начальные данные (админ, автопарк) загрузит `DataLoader`.

**Админ по умолчанию:** логин `Admin777`, пароль `Admin123`.

## Локальная проверка перед деплоем

```bash
cd Хост
set SPRING_PROFILES_ACTIVE=local
mvnw.cmd spring-boot:run
```

Скопируйте `.env.example` в `.env` и заполните при необходимости.

## Полезные команды Railway CLI

```bash
npm i -g @railway/cli
railway login
railway init
railway add --database postgres
railway up
```

## Примечания

- Порт приложения берётся из переменной `PORT` (Railway задаёт её автоматически).
- Загруженные в админке фото машин на Railway не сохраняются между перезапусками (эфемерная файловая система контейнера).
- Схема БД для справки: `database/schema.sql`.
