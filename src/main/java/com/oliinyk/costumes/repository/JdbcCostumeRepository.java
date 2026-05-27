package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.Costume;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JDBC реалізація репозиторію костюмів. Забезпечує взаємодію з таблицею costumes у базі даних. */
public class JdbcCostumeRepository implements CostumeRepository {

    /**
     * Зберігає новий костюм у базі даних.
     *
     * @param costume об'єкт костюма для збереження
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(Costume costume) {
        String sql =
                "INSERT INTO costumes (id, category_id, name, description, price_per_day, image_path, deposit_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, costume.getId());
            stmt.setObject(2, costume.getCategoryId());
            stmt.setString(3, costume.getName());
            stmt.setString(4, costume.getDescription());
            stmt.setBigDecimal(5, costume.getPricePerDay());
            stmt.setString(6, costume.getImagePath());
            stmt.setBigDecimal(7, costume.getDepositAmount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні костюма", e);
        }
    }

    /**
     * Знаходить костюм за його ідентифікатором.
     *
     * @param id унікальний ідентифікатор костюма
     * @return Optional з костюмом, якщо знайдено, або порожній Optional
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public Optional<Costume> findById(UUID id) {
        String sql = "SELECT * FROM costumes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCostume(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку костюма за ID", e);
        }
        return Optional.empty();
    }

    /**
     * Повертає список усіх костюмів з бази даних.
     *
     * @return список усіх костюмів
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<Costume> findAll() {
        String sql = "SELECT * FROM costumes";
        List<Costume> costumes = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                costumes.add(mapResultSetToCostume(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх костюмів", e);
        }
        return costumes;
    }

    /**
     * Оновлює дані існуючого костюма в базі даних.
     *
     * @param costume об'єкт костюма з оновленими даними
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void update(Costume costume) {
        String sql =
                "UPDATE costumes SET category_id = ?, name = ?, description = ?, price_per_day = ?, image_path = ?, deposit_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, costume.getCategoryId());
            stmt.setString(2, costume.getName());
            stmt.setString(3, costume.getDescription());
            stmt.setBigDecimal(4, costume.getPricePerDay());
            stmt.setString(5, costume.getImagePath());
            stmt.setBigDecimal(6, costume.getDepositAmount());
            stmt.setObject(7, costume.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні костюма", e);
        }
    }

    /**
     * Видаляє костюм за його ідентифікатором.
     *
     * @param id унікальний ідентифікатор костюма для видалення
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM costumes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні костюма", e);
        }
    }

    /**
     * Перетворює рядок ResultSet у об'єкт Costume.
     *
     * @param rs ResultSet з результатами запиту
     * @return об'єкт Costume
     * @throws SQLException якщо виникла помилка при читанні з ResultSet
     */
    private Costume mapResultSetToCostume(ResultSet rs) throws SQLException {
        return Costume.builder()
                .id(rs.getObject("id", UUID.class))
                .categoryId(rs.getObject("category_id", UUID.class))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .pricePerDay(rs.getBigDecimal("price_per_day"))
                .imagePath(rs.getString("image_path"))
                .depositAmount(rs.getBigDecimal("deposit_amount"))
                .build();
    }
}
