package com.guvi.recommend.db.dao;

import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.exception.DatabaseOperationException;
import com.guvi.recommend.model.Interaction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data Access Object (DAO) for Interaction telemetry implementing JDBC operations.
 */
public class InteractionDao implements Repository<Interaction, String> {
    private final DatabaseConnectionManager dbManager;
    private final List<Interaction> memoryCache = new CopyOnWriteArrayList<>();

    public InteractionDao() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Interaction save(Interaction interaction) throws DatabaseOperationException {
        if (interaction == null) {
            throw new DatabaseOperationException("Cannot save null interaction");
        }
        memoryCache.add(interaction);

        if (!dbManager.isJdbcAvailable()) {
            return interaction;
        }

        String sql = """
            INSERT INTO interactions (id, user_id, item_id, item_category, type, rating_value, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, interaction.getId());
            ps.setString(2, interaction.getUserId());
            ps.setString(3, interaction.getItemId());
            ps.setString(4, interaction.getItemCategory());
            ps.setString(5, interaction.getType());
            if (interaction.getRatingValue() != null) {
                ps.setDouble(6, interaction.getRatingValue());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }
            ps.setLong(7, interaction.getTimestamp());
            ps.executeUpdate();
            return interaction;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save interaction via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Interaction> findById(String id) throws DatabaseOperationException {
        for (Interaction in : memoryCache) {
            if (in.getId().equals(id)) return Optional.of(in);
        }
        return Optional.empty();
    }

    @Override
    public List<Interaction> findAll() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) {
            return new ArrayList<>(memoryCache);
        }

        List<Interaction> list = new ArrayList<>();
        String sql = "SELECT id, user_id, item_id, item_category, type, rating_value, timestamp FROM interactions";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Interaction in = new Interaction();
                in.setId(rs.getString("id"));
                in.setUserId(rs.getString("user_id"));
                in.setItemId(rs.getString("item_id"));
                in.setItemCategory(rs.getString("item_category"));
                in.setType(rs.getString("type"));
                double val = rs.getDouble("rating_value");
                if (!rs.wasNull()) in.setRatingValue(val);
                in.setTimestamp(rs.getLong("timestamp"));
                list.add(in);
            }
        } catch (SQLException e) {
            return new ArrayList<>(memoryCache);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) throws DatabaseOperationException {
        memoryCache.removeIf(in -> in.getId().equals(id));
        if (!dbManager.isJdbcAvailable()) return true;

        String sql = "DELETE FROM interactions WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete interaction via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) return memoryCache.size();

        String sql = "SELECT COUNT(*) FROM interactions";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            return memoryCache.size();
        }
        return memoryCache.size();
    }

    public List<Interaction> findByUserId(String userId) {
        List<Interaction> list = new ArrayList<>();
        for (Interaction in : memoryCache) {
            if (in.getUserId().equals(userId)) {
                list.add(in);
            }
        }
        return list;
    }
}
