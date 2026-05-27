package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC реалізація репозиторію категорій. Забезпечує взаємодію з таблицею categories у базі даних.
 */
public class JdbcCategoryRepository implements CategoryRepository {

    /**
     * Зберігає нову категорію в базі даних.
     *
     * @param category об'єкт категорії для збереження
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(Category category) {
        String sql = "INSERT INTO categories (id, name, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні категорії", e);
        }
    }

    /**
     * Знаходить категорію за її ідентифікатором.
     *
     * @param id унікальний ідентифікатор категорії
     * @return Optional з категорією, якщо знайдено, або порожній Optional
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public Optional<Category> findById(UUID id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку категорії за ID", e);
        }
        return Optional.empty();
    }

    /**
     * Повертає список усіх категорій з бази даних.
     *
     * @return список усіх категорій
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх категорій", e);
        }
        return categories;
    }

    /**
     * Оновлює дані існуючої категорії в базі даних.
     *
     * @param category об'єкт категорії з оновленими даними
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setObject(3, category.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні категорії", e);
        }
    }

    /**
     * Видаляє категорію за її ідентифікатором.
     *
     * @param id унікальний ідентифікатор категорії для видалення
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні категорії", e);
        }
    }

    /**
     * Перетворює рядок ResultSet у об'єкт Category.
     *
     * @param rs ResultSet з результатами запиту
     * @return об'єкт Category
     * @throws SQLException якщо виникла помилка при читанні з ResultSet
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return Category.builder()
                .id(rs.getObject("id", UUID.class))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .build();
    }
}
