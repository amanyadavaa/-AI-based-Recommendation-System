package com.guvi.recommend.db.dao;

import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.exception.DatabaseOperationException;
import com.guvi.recommend.model.User;
import com.guvi.recommend.util.JsonUtil;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object (DAO) for User entity implementing JDBC operations.
 */
public class UserDao implements Repository<User, String> {
    private final DatabaseConnectionManager dbManager;
    private final Map<String, User> memoryCache = new ConcurrentHashMap<>();

    public UserDao() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public User save(User user) throws DatabaseOperationException {
        if (user == null || user.getId() == null) {
            throw new DatabaseOperationException("Cannot save null user or user with null ID");
        }
        memoryCache.put(user.getId(), user);

        if (!dbManager.isJdbcAvailable()) {
            return user;
        }

        String sql = """
            INSERT INTO users (id, username, email, role, preferences, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                username = excluded.username,
                email = excluded.email,
                role = excluded.role,
                preferences = excluded.preferences;
        """;

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.setString(5, JsonUtil.toJson(user.getPreferences()));
            ps.setLong(6, user.getCreatedAt());
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save user via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(String id) throws DatabaseOperationException {
        if (id == null) return Optional.empty();
        if (memoryCache.containsKey(id)) {
            return Optional.of(memoryCache.get(id));
        }

        if (!dbManager.isJdbcAvailable()) {
            return Optional.empty();
        }

        String sql = "SELECT id, username, email, role, preferences, created_at FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = mapRow(rs);
                    memoryCache.put(u.getId(), u);
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find user by ID via JDBC: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) {
            return new ArrayList<>(memoryCache.values());
        }

        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, email, role, preferences, created_at FROM users";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = mapRow(rs);
                list.add(u);
                memoryCache.put(u.getId(), u);
            }
        } catch (SQLException e) {
            return new ArrayList<>(memoryCache.values());
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) throws DatabaseOperationException {
        memoryCache.remove(id);
        if (!dbManager.isJdbcAvailable()) return true;

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) return memoryCache.size();

        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            return memoryCache.size();
        }
        return memoryCache.size();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getLong("created_at"));
        String prefJson = rs.getString("preferences");
        if (prefJson != null && !prefJson.isEmpty()) {
            u.setPreferences(JsonUtil.parseMap(prefJson));
        }
        return u;
    }
}
