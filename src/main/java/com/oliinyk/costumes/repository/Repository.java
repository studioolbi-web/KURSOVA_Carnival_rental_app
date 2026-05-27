package com.oliinyk.costumes.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовий інтерфейс репозиторію для виконання CRUD операцій.
 *
 * @param <T> Тип об'єкта моделі
 */
public interface Repository<T> {
    /**
     * Зберегти новий об'єкт у базі даних.
     *
     * @param entity Об'єкт для збереження
     */
    void save(T entity);

    /**
     * Знайти об'єкт за його унікальним ідентифікатором.
     *
     * @param id UUID об'єкта
     * @return Optional, що містить об'єкт, або порожній, якщо не знайдено
     */
    Optional<T> findById(UUID id);

    /**
     * Отримати список усіх об'єктів даного типу.
     *
     * @return Список усіх об'єктів
     */
    List<T> findAll();

    /**
     * Оновити існуючий об'єкт у базі даних.
     *
     * @param entity Об'єкт з оновленими даними
     */
    void update(T entity);

    /**
     * Видалити об'єкт за його ідентифікатором.
     *
     * @param id UUID об'єкта для видалення
     */
    void delete(UUID id);
}
