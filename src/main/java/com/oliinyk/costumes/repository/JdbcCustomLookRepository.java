package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.infrastructure.DatabaseManager;
import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.model.CustomLook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcCustomLookRepository implements CustomLookRepository {

    private final CostumeRepository costumeRepository = RepositoryProvider.getCostumeRepository();

    @Override
    public void save(CustomLook entity) {
        String sqlLook = "INSERT INTO custom_looks (id, user_id, name, image_path, total_price) VALUES (?, ?, ?, ?, ?)";
        String sqlItems = "INSERT INTO look_items (look_id, costume_id) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlLook)) {
                stmt.setObject(1, entity.getId());
                stmt.setObject(2, entity.getUserId());
                stmt.setString(3, entity.getName());
                stmt.setString(4, entity.getImagePath());
                stmt.setBigDecimal(5, entity.getTotalPrice());
                stmt.executeUpdate();
            }

            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlItems)) {
                    for (Costume costume : entity.getItems()) {
                        stmt.setObject(1, entity.getId());
                        stmt.setObject(2, costume.getId());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save custom look", e);
        }
    }

    @Override
    public Optional<CustomLook> findById(UUID id) {
        String sql = "SELECT * FROM custom_looks WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                CustomLook look = mapResultSetToCustomLook(rs);
                look.setItems(findItemsByLookId(look.getId()));
                return Optional.of(look);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<CustomLook> findAll() {
        List<CustomLook> looks = new ArrayList<>();
        String sql = "SELECT * FROM custom_looks";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                CustomLook look = mapResultSetToCustomLook(rs);
                look.setItems(findItemsByLookId(look.getId()));
                looks.add(look);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return looks;
    }

    @Override
    public void update(CustomLook entity) {
        // Оновлення зазвичай стосується лише базових полів, рідше - складу.
        String sql = "UPDATE custom_looks SET name = ?, image_path = ?, total_price = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getImagePath());
            stmt.setBigDecimal(3, entity.getTotalPrice());
            stmt.setObject(4, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM custom_looks WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CustomLook> findByUserId(UUID userId) {
        List<CustomLook> looks = new ArrayList<>();
        String sql = "SELECT * FROM custom_looks WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CustomLook look = mapResultSetToCustomLook(rs);
                look.setItems(findItemsByLookId(look.getId()));
                looks.add(look);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return looks;
    }

    private List<Costume> findItemsByLookId(UUID lookId) {
        List<Costume> items = new ArrayList<>();
        String sql = "SELECT costume_id FROM look_items WHERE look_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setObject(1, lookId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                UUID costumeId = (UUID) rs.getObject("costume_id");
                costumeRepository.findById(costumeId).ifPresent(items::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private CustomLook mapResultSetToCustomLook(ResultSet rs) throws SQLException {
        return CustomLook.builder()
                .id((UUID) rs.getObject("id"))
                .userId((UUID) rs.getObject("user_id"))
                .name(rs.getString("name"))
                .imagePath(rs.getString("image_path"))
                .totalPrice(rs.getBigDecimal("total_price"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
