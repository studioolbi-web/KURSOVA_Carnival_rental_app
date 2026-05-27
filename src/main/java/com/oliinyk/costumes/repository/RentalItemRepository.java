package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.model.RentalItem;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторій для роботи з елементами оренди. Керує даними про конкретні костюми, що входять до
 * складу замовлення.
 */
public interface RentalItemRepository {
    /**
     * Зберігає новий елемент оренди в базі даних.
     *
     * @param item об'єкт елемента оренди
     */
    void save(RentalItem item);

    /**
     * Зберігає елемент оренди в межах існуючої транзакції.
     *
     * @param item об'єкт елемента оренди для збереження
     * @param conn існуюче SQL з'єднання
     */
    void save(RentalItem item, Connection conn);

    /**
     * Знаходить усі елементи (костюми), що входять до вказаної оренди.
     *
     * @param rentalId унікальний ідентифікатор оренди
     * @return список елементів оренди
     */
    List<RentalItem> findByRentalId(UUID rentalId);
}
