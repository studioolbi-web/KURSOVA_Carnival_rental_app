-- Оновлення існуючих костюмів
UPDATE costumes SET image_path = 'https://via.placeholder.com/200x200.png?text=Knight+Armor' WHERE id = '66666666-6666-6666-6666-666666666666';
UPDATE costumes SET image_path = 'https://via.placeholder.com/200x200.png?text=Elf+Costume' WHERE id = '77777777-7777-7777-7777-777777777777';
UPDATE costumes SET image_path = 'https://via.placeholder.com/200x200.png?text=Batman' WHERE id = '88888888-8888-8888-8888-888888888888';

-- Додавання нових костюмів з картинками
INSERT INTO costumes (id, category_id, name, description, price_per_day, image_path) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Костюм пірата', 'Капелюх, гак та повязка на око', 400.00, 'https://via.placeholder.com/200x200.png?text=Pirate'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Сукня принцеси', 'Пишна рожева сукня з діадемою', 600.00, 'https://via.placeholder.com/200x200.png?text=Princess'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Вампірський плащ', 'Елегантний чорно-червоний плащ з високим коміром', 350.00, 'https://via.placeholder.com/200x200.png?text=Vampire');
