-- Створення індексів для оптимізації пошуку
-- Вимога розділу 3.4

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_costumes_name ON costumes(name);
CREATE INDEX idx_rentals_user_id ON rentals(user_id);
CREATE INDEX idx_rental_items_rental_id ON rental_items(rental_id);
