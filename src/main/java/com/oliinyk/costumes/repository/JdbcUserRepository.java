package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JDBC реалізація репозиторію користувачів. Забезпечує взаємодію з таблицею users у базі даних. */
public class JdbcUserRepository implements UserRepository {

    /**
     * Зберігає нового користувача в базі даних.
     *
     * @param user об'єкт користувача для збереження
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void save(User user) {
        String sql =
                "INSERT INTO users (id, email, password_hash, role, verification_code, is_verified, is_blocked, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getVerificationCode());
            stmt.setBoolean(6, user.isVerified());
            stmt.setBoolean(7, user.isBlocked());
            stmt.setTimestamp(
                    8,
                    user.getCreatedAt() != null
                            ? Timestamp.valueOf(user.getCreatedAt())
                            : new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні користувача", e);
        }
    }

    /**
     * Знаходить користувача за його ідентифікатором.
     *
     * @param id унікальний ідентифікатор користувача
     * @return Optional з користувачем, якщо знайдено, або порожній Optional
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку користувача за ID", e);
        }
        return Optional.empty();
    }

    /**
     * Знаходить користувача за його електронною поштою.
     *
     * @param email електронна пошта користувача
     * @return Optional з користувачем, якщо знайдено, або порожній Optional
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку користувача за email", e);
        }
        return Optional.empty();
    }

    /**
     * Повертає список усіх користувачів з бази даних.
     *
     * @return список усіх користувачів
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх користувачів", e);
        }
        return users;
    }

    /**
     * Оновлює дані існуючого користувача в базі даних.
     *
     * @param user об'єкт користувача з оновленими даними
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void update(User user) {
        String sql =
                "UPDATE users SET email = ?, password_hash = ?, role = ?, verification_code = ?, is_verified = ?, is_blocked = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getVerificationCode());
            stmt.setBoolean(5, user.isVerified());
            stmt.setBoolean(6, user.isBlocked());
            stmt.setObject(7, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні користувача", e);
        }
    }

    /**
     * Видаляє користувача за його ідентифікатором.
     *
     * @param id унікальний ідентифікатор користувача для видалення
     * @throws RuntimeException якщо виникла помилка SQL під час виконання запиту
     */
    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні користувача", e);
        }
    }

    /**
     * Перетворює рядок ResultSet у об'єкт User.
     *
     * @param rs ResultSet з результатами запиту
     * @return об'єкт User
     * @throws SQLException якщо виникла помилка при читанні з ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getObject("id", UUID.class))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .role(rs.getString("role"))
                .verificationCode(rs.getString("verification_code"))
                .isVerified(rs.getBoolean("is_verified"))
                .isBlocked(rs.getBoolean("is_blocked"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
