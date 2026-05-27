-- Додавання можливості блокування користувачів
-- Вимога розділу 6 (Адмін-панель)

ALTER TABLE users ADD COLUMN is_blocked BOOLEAN NOT NULL DEFAULT FALSE;

-- Індекси для прискорення перевірки оренди
CREATE INDEX idx_rentals_dates ON rentals(start_date, end_date);
CREATE INDEX idx_rental_items_costume_id ON rental_items(costume_id);
