package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.RentalItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC реалізація репозиторію елементів оренди. Підтримує транзакційність та забезпечує взаємодію з
 * таблицею rental_items.
 */
public class JdbcRentalItemRepository implements RentalItemRepository {

    /**
     * Зберігає новий елемент оренди в базі даних. Створює нове з'єднання для виконання операції.
     *
     * @param item об'єкт елемента оренди для збереження
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(RentalItem item) {
        try (Connection conn = DatabaseManager.getConnection()) {
            save(item, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні елемента оренди", e);
        }
    }

    /**
     * Зберігає елемент оренди в межах існуючої транзакції.
     *
     * @param item об'єкт елемента оренди для збереження
     * @param conn існуюче SQL з'єднання
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(RentalItem item, Connection conn) {
        String sql =
                "INSERT INTO rental_items (rental_id, costume_id, price_at_rental) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, item.getRentalId());
            stmt.setObject(2, item.getCostumeId());
            stmt.setBigDecimal(3, item.getPriceAtRental());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні елемента оренди в транзакції", e);
        }
    }

    /**
     * Знаходить усі елементи для конкретної оренди за її ідентифікатором.
     *
     * @param rentalId унікальний ідентифікатор оренди
     * @return список елементів оренди
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<RentalItem> findByRentalId(UUID rentalId) {
        String sql = "SELECT * FROM rental_items WHERE rental_id = ?";
        List<RentalItem> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, rentalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToRentalItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку елементів оренди", e);
        }
        return items;
    }

    /**
     * Перетворює рядок ResultSet у об'єкт RentalItem.
     *
     * @param rs ResultSet з результатами запиту
     * @return об'єкт RentalItem
     * @throws SQLException якщо виникла помилка при читанні з ResultSet
     */
    private RentalItem mapResultSetToRentalItem(ResultSet rs) throws SQLException {
        return RentalItem.builder()
                .rentalId(rs.getObject("rental_id", UUID.class))
                .costumeId(rs.getObject("costume_id", UUID.class))
                .priceAtRental(rs.getBigDecimal("price_at_rental"))
                .build();
    }
}
