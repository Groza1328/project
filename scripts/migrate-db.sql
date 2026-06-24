-- Миграция схемы для локальной БД (колонки city, car_id)
-- Hibernate ddl-auto=update не может добавить NOT NULL без DEFAULT к существующим строкам

ALTER TABLE cars ADD COLUMN IF NOT EXISTS city VARCHAR(255) DEFAULT 'Омск';

UPDATE cars SET city = 'Новосибирск'
WHERE regexp_replace(plate_number, '[^0-9]', '', 'g') ~ '(54|154)$';

UPDATE cars SET city = 'Омск'
WHERE regexp_replace(plate_number, '[^0-9]', '', 'g') ~ '(55|155)$';

UPDATE cars SET city = 'Омск' WHERE city IS NULL;

ALTER TABLE cars ALTER COLUMN city SET DEFAULT 'Омск';
ALTER TABLE cars ALTER COLUMN city SET NOT NULL;

ALTER TABLE orders ADD COLUMN IF NOT EXISTS car_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'orders' AND constraint_name = 'orders_car_id_fkey'
    ) THEN
        ALTER TABLE orders
            ADD CONSTRAINT orders_car_id_fkey
            FOREIGN KEY (car_id) REFERENCES cars(id);
    END IF;
END $$;
