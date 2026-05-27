package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.model.Rental;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторій для управління даними про оренду. Розширює базовий інтерфейс Repository для моделі
 * Rental.
 */
public interface RentalRepository extends Repository<Rental> {
    /**
     * Знаходить усі оренди, що належать конкретному користувачу.
     *
     * @param userId унікальний ідентифікатор користувача
     * @return список усіх оренд користувача
     */
    List<Rental> findByUserId(UUID userId);

    /**
     * Зберігає дані про оренду в межах існуючої транзакції.
     *
     * @param rental об'єкт оренди для збереження
     * @param conn існуюче SQL з'єднання
     */
    void save(Rental rental, Connection conn);

    /**
     * Перевіряє, чи є вказаний костюм вільним для оренди на заданий період.
     *
     * @param costumeId унікальний ідентифікатор костюма
     * @param start дата початку бажаного періоду
     * @param end дата закінчення бажаного періоду
     * @return true, якщо костюм доступний для оренди, false — якщо зайнятий
     */
    boolean isCostumeAvailable(UUID costumeId, java.time.LocalDate start, java.time.LocalDate end);
}
