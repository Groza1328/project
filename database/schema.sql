-- Схема БД веб-сервиса «СибМобиль» (PostgreSQL)
-- Соответствует JPA-сущностям в пакете ru.sibmobile.model
-- Hibernate при spring.jpa.hibernate.ddl-auto=update создаёт/обновляет таблицы автоматически

-- Создание БД (выполнить от имени суперпользователя, один раз):
-- CREATE DATABASE sibmobile ENCODING 'UTF8';
-- \c sibmobile

-- ============================================================
-- Пользователи (регистрация, коды подтверждения, тарифы)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,  -- BCrypt-хеш, не открытый текст
    full_name           VARCHAR(255),
    role                VARCHAR(50),
    last_login_at       TIMESTAMP,
    enabled             BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_code   VARCHAR(6),             -- код подтверждения email при регистрации
    reset_code          VARCHAR(6),             -- код восстановления пароля
    tariff              VARCHAR(32)  DEFAULT 'STANDARD',
    created_at          TIMESTAMP,
    tariff_since        TIMESTAMP,
    blocked             BOOLEAN      DEFAULT FALSE,
    restricted_until    TIMESTAMP,
    restriction_reason  VARCHAR(500),
    complaints_fines_count INTEGER DEFAULT 0
);

COMMENT ON COLUMN users.enabled IS 'FALSE до ввода верного verification_code';
COMMENT ON COLUMN users.verification_code IS 'Одноразовый 6-значный код; NULL после успешной верификации';
COMMENT ON COLUMN users.reset_code IS 'Одноразовый код сброса пароля; NULL после смены пароля';

-- ============================================================
-- Сотрудники (админ-панель, отдельные учётные записи)
-- ============================================================
CREATE TABLE IF NOT EXISTS employees (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) UNIQUE,
    password_hash   VARCHAR(255),
    full_name       VARCHAR(255) NOT NULL,
    position        VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(50),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    hired_at        TIMESTAMP,
    role            VARCHAR(50),
    last_login_at   TIMESTAMP
);

-- ============================================================
-- Автопарк
-- ============================================================
CREATE TABLE IF NOT EXISTS cars (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32)  NOT NULL,   -- CarType: ECONOMY, COMFORT, BUSINESS
    plate_number    VARCHAR(32)  NOT NULL UNIQUE,
    body_type       VARCHAR(64),
    image_path      VARCHAR(512),
    description     VARCHAR(500),
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Заказы проката
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    car_type         VARCHAR(32) NOT NULL,
    start_date_time  TIMESTAMP NOT NULL,
    end_date_time    TIMESTAMP NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled);
