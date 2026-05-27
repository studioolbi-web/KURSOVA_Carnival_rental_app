package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.Rental;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC реалізація репозиторію оренди. Підтримує транзакційність та забезпечує взаємодію з таблицею
 * rentals.
 */
public class JdbcRentalRepository implements RentalRepository {

    /**
     * Зберігає нову оренду в базі даних. Створює нове з'єднання для виконання операції.
     *
     * @param rental об'єкт оренди для збереження
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(Rental rental) {
        try (Connection conn = DatabaseManager.getConnection()) {
            save(rental, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні оренди", e);
        }
    }

    /**
     * Зберігає оренду в межах існуючої транзакції.
     *
     * @param rental об'єкт оренди для збереження
     * @param conn існуюче SQL з'єднання
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(Rental rental, Connection conn) {
        String sql =
                "INSERT INTO rentals (id, user_id, start_date, end_date, total_price, penalty_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, rental.getId());
            stmt.setObject(2, rental.getUserId());
            stmt.setObject(3, java.sql.Date.valueOf(rental.getStartDate()));
            stmt.setObject(4, java.sql.Date.valueOf(rental.getEndDate()));
            stmt.setBigDecimal(5, rental.getTotalPrice());
            stmt.setBigDecimal(6, rental.getPenaltyAmount());
            stmt.setString(7, rental.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні оренди в транзакції", e);
        }
    }

    /**
     * Знаходить оренду за її ідентифікатором.
     *
     * @param id унікальний ідентифікатор оренди
     * @return Optional з орендою, якщо знайдено, або порожній Optional
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public Optional<Rental> findById(UUID id) {
        String sql = "SELECT * FROM rentals WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRental(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку оренди за ID", e);
        }
        return Optional.empty();
    }

    /**
     * Повертає список усіх оренд з бази даних.
     *
     * @return список усіх оренд
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<Rental> findAll() {
        String sql = "SELECT * FROM rentals";
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rentals.add(mapResultSetToRental(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх оренд", e);
        }
        return rentals;
    }

    /**
     * Знаходить усі оренди конкретного користувача.
     *
     * @param userId унікальний ідентифікатор користувача
     * @return список оренд користувача
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<Rental> findByUserId(UUID userId) {
        String sql = "SELECT * FROM rentals WHERE user_id = ?";
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rentals.add(mapResultSetToRental(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку оренд користувача", e);
        }
        return rentals;
    }

    /**
     * Оновлює дані існуючої оренди в базі даних.
     *
     * @param rental об'єкт оренди з оновленими даними
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void update(Rental rental) {
        String sql =
                "UPDATE rentals SET user_id = ?, start_date = ?, end_date = ?, total_price = ?, penalty_amount = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, rental.getUserId());
            stmt.setObject(2, java.sql.Date.valueOf(rental.getStartDate()));
            stmt.setObject(3, java.sql.Date.valueOf(rental.getEndDate()));
            stmt.setBigDecimal(4, rental.getTotalPrice());
            stmt.setBigDecimal(5, rental.getPenaltyAmount());
            stmt.setString(6, rental.getStatus());
            stmt.setObject(7, rental.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні оренди", e);
        }
    }

    /**
     * Видаляє оренду за її ідентифікатором.
     *
     * @param id унікальний ідентифікатор оренди для видалення
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void delete(UUID id) {
        String deleteItemsSql = "DELETE FROM rental_items WHERE rental_id = ?";
        String deleteRentalSql = "DELETE FROM rentals WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement itemStmt = conn.prepareStatement(deleteItemsSql);
                 PreparedStatement rentalStmt = conn.prepareStatement(deleteRentalSql)) {
                
                itemStmt.setObject(1, id);
                itemStmt.executeUpdate();
                
                rentalStmt.setObject(1, id);
                rentalStmt.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Помилка при видаленні оренди", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні оренди", e);
        }
    }

    /**
     * Перевіряє, чи доступний костюм для оренди на вказаний період. Костюм вважається зайнятим,
     * якщо він є в активній оренді або резерві, що перетинається з вказаними датами.
     *
     * @param costumeId унікальний ідентифікатор костюма
     * @param start дата початку бажаного періоду оренди
     * @param end дата закінчення бажаного періоду оренди
     * @return true, якщо костюм вільний, false — якщо зайнятий
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public boolean isCostumeAvailable(
            UUID costumeId, java.time.LocalDate start, java.time.LocalDate end) {
        // Умова перетину періодів [A, B] та [C, D]: A <= D AND B >= C
        // Критичний баг 1: Костюм зайнятий, якщо статус ACTIVE, RESERVED, ISSUED або OVERDUE
        // (Вимога стабільності)
        String sql =
                "SELECT COUNT(*) FROM rentals r "
                        + "JOIN rental_items ri ON r.id = ri.rental_id "
                        + "WHERE ri.costume_id = ? AND r.status IN ('ACTIVE', 'RESERVED', 'ISSUED', 'OVERDUE') "
                        + "AND r.start_date <= ? AND r.end_date >= ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, costumeId);
            stmt.setObject(2, java.sql.Date.valueOf(end)); // r.start_date <= end
            stmt.setObject(3, java.sql.Date.valueOf(start)); // r.end_date >= start
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при перевірці доступності костюма", e);
        }
        return false;
    }

    /**
     * Перетворює рядок ResultSet у об'єкт Rental.
     *
     * @param rs ResultSet з результатами запиту
     * @return об'єкт Rental
     * @throws SQLException якщо виникла помилка при читанні з ResultSet
     */
    private Rental mapResultSetToRental(ResultSet rs) throws SQLException {
        return Rental.builder()
                .id(rs.getObject("id", UUID.class))
                .userId(rs.getObject("user_id", UUID.class))
                .startDate(rs.getDate("start_date").toLocalDate())
                .endDate(rs.getDate("end_date").toLocalDate())
                .totalPrice(rs.getBigDecimal("total_price"))
                .penaltyAmount(rs.getBigDecimal("penalty_amount"))
                .status(rs.getString("status"))
                .build();
    }
}
