-- Додаємо нову категорію для елементів образу (капелюхи, маски, штани тощо)
INSERT INTO categories (id, name, description) 
VALUES ('44444444-4444-4444-4444-444444444444', 'Елементи образу', 'Окремі деталі для створення власного образу');

-- Додаємо кілька тестових елементів
INSERT INTO costumes (id, category_id, name, description, price_per_day, image_path)
VALUES 
('a1111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 'Піратський капелюх', 'Капелюх з пером', 50.00, '/images/pirate_hat.png'),
('a2222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', 'Маска Зорро', 'Чорна маска на очі', 30.00, '/images/zorro_mask.png'),
('a3333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 'Червоний плащ', 'Довгий червоний плащ', 80.00, '/images/red_cape.png');

-- Таблиця для збереження згенерованих образів
CREATE TABLE custom_looks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    image_path VARCHAR(500),
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Таблиця зв'язку образу з конкретними деталями (costumes)
CREATE TABLE look_items (
    look_id UUID NOT NULL,
    costume_id UUID NOT NULL,
    PRIMARY KEY (look_id, costume_id),
    FOREIGN KEY (look_id) REFERENCES custom_looks(id) ON DELETE CASCADE,
    FOREIGN KEY (costume_id) REFERENCES costumes(id) ON DELETE CASCADE
);
