-- Оновлення категорій на Чоловічі, Жіночі, Дитячі
UPDATE categories SET name = 'Чоловічі', description = 'Костюми для чоловіків' WHERE id = '11111111-1111-1111-1111-111111111111';
UPDATE categories SET name = 'Жіночі', description = 'Костюми для жінок' WHERE id = '22222222-2222-2222-2222-222222222222';
UPDATE categories SET name = 'Дитячі', description = 'Костюми для дітей' WHERE id = '33333333-3333-3333-3333-333333333333';

-- Вампірський плащ переносимо в Чоловічі (замість минулої категорії "Супергерої", яка тепер стала "Дитячі")
UPDATE costumes SET category_id = '11111111-1111-1111-1111-111111111111' WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccccc';
