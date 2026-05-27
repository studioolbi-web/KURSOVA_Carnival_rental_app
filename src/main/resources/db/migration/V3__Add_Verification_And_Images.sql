-- Додавання поля для коду верифікації
ALTER TABLE users ADD COLUMN verification_code VARCHAR(10);

-- Додавання поля для зображення костюма
ALTER TABLE costumes ADD COLUMN image_path VARCHAR(255);
