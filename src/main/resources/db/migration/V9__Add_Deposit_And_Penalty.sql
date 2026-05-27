-- Додавання застави для костюмів та штрафів для оренд
-- Вимога розділу: Блок 2 (Фінанси)

ALTER TABLE costumes ADD COLUMN deposit_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Додавання штрафу до таблиці оренд
ALTER TABLE rentals ADD COLUMN penalty_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Оновлення існуючих даних (приблизна застава - 50% від денної вартості * 2 як приклад)
UPDATE costumes SET deposit_amount = price_per_day * 2;
