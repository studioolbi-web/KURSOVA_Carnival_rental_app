-- Таблиця users
-- Класифікація: Головна сутність (Entity)
-- Нормальна форма: 3НФ
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця categories
-- Класифікація: Довідник (Dictionary)
-- Нормальна форма: 3НФ
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

-- Таблиця costumes
-- Класифікація: Головна сутність (Entity)
-- Нормальна форма: 3НФ
CREATE TABLE costumes (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price_per_day DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- Таблиця rentals
-- Класифікація: Транзакційна таблиця (Transaction)
-- Нормальна форма: 3НФ
CREATE TABLE rentals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Таблиця rental_items
-- Класифікація: Таблиця зв'язку (Join Table / Transaction Details)
-- Нормальна форма: 3НФ
CREATE TABLE rental_items (
    rental_id UUID NOT NULL,
    costume_id UUID NOT NULL,
    price_at_rental DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (rental_id, costume_id),
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    FOREIGN KEY (costume_id) REFERENCES costumes(id) ON DELETE RESTRICT
);
