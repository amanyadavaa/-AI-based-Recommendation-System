package com.guvi.recommend.db.dao;

import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.exception.DatabaseOperationException;
import com.guvi.recommend.model.Item;
import com.guvi.recommend.util.JsonUtil;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object (DAO) for Item entity implementing JDBC operations.
 */
public class ItemDao implements Repository<Item, String> {
    private final DatabaseConnectionManager dbManager;
    private final Map<String, Item> memoryCache = new ConcurrentHashMap<>();

    public ItemDao() {
        this.dbManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Item save(Item item) throws DatabaseOperationException {
        if (item == null || item.getId() == null) {
            throw new DatabaseOperationException("Cannot save null item or item with null ID");
        }
        memoryCache.put(item.getId(), item);

        if (!dbManager.isJdbcAvailable()) {
            return item;
        }

        String sql = """
            INSERT INTO items (id, title, category, description, rating, review_count, image_url, tags, price, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                title = excluded.title,
                category = excluded.category,
                description = excluded.description,
                rating = excluded.rating,
                review_count = excluded.review_count,
                image_url = excluded.image_url,
                tags = excluded.tags,
                price = excluded.price,
                metadata = excluded.metadata;
        """;

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setDouble(5, item.getRating());
            ps.setInt(6, item.getReviewCount());
            ps.setString(7, item.getImageUrl());
            ps.setString(8, JsonUtil.toJson(item.getTags()));
            if (item.getPrice() != null) {
                ps.setDouble(9, item.getPrice());
            } else {
                ps.setNull(9, Types.DOUBLE);
            }
            ps.setString(10, JsonUtil.toJson(item.getMetadata()));
            ps.executeUpdate();
            return item;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save item via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Item> findById(String id) throws DatabaseOperationException {
        if (id == null) return Optional.empty();
        if (memoryCache.containsKey(id)) {
            return Optional.of(memoryCache.get(id));
        }

        if (!dbManager.isJdbcAvailable()) return Optional.empty();

        String sql = "SELECT id, title, category, description, rating, review_count, image_url, tags, price, metadata FROM items WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Item it = mapRow(rs);
                    memoryCache.put(it.getId(), it);
                    return Optional.of(it);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find item via JDBC: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) {
            return new ArrayList<>(memoryCache.values());
        }

        List<Item> list = new ArrayList<>();
        String sql = "SELECT id, title, category, description, rating, review_count, image_url, tags, price, metadata FROM items";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Item it = mapRow(rs);
                list.add(it);
                memoryCache.put(it.getId(), it);
            }
        } catch (SQLException e) {
            return new ArrayList<>(memoryCache.values());
        }
        return list;
    }

    public List<Item> findByCategory(String category) throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) {
            List<Item> res = new ArrayList<>();
            for (Item item : memoryCache.values()) {
                if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)) {
                    res.add(item);
                }
            }
            return res;
        }

        List<Item> list = new ArrayList<>();
        String sql = "SELECT id, title, category, description, rating, review_count, image_url, tags, price, metadata FROM items WHERE UPPER(category) = UPPER(?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find items by category: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean deleteById(String id) throws DatabaseOperationException {
        memoryCache.remove(id);
        if (!dbManager.isJdbcAvailable()) return true;

        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete item via JDBC: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        if (!dbManager.isJdbcAvailable()) return memoryCache.size();

        String sql = "SELECT COUNT(*) FROM items";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            return memoryCache.size();
        }
        return memoryCache.size();
    }

    @SuppressWarnings("unchecked")
    private Item mapRow(ResultSet rs) throws SQLException {
        Item it = new Item();
        it.setId(rs.getString("id"));
        it.setTitle(rs.getString("title"));
        it.setCategory(rs.getString("category"));
        it.setDescription(rs.getString("description"));
        it.setRating(rs.getDouble("rating"));
        it.setReviewCount(rs.getInt("review_count"));
        it.setImageUrl(rs.getString("image_url"));
        double price = rs.getDouble("price");
        if (!rs.wasNull()) {
            it.setPrice(price);
        }
        String tagsJson = rs.getString("tags");
        if (tagsJson != null && !tagsJson.isEmpty()) {
            List<Object> raw = JsonUtil.parseList(tagsJson);
            List<String> tags = new ArrayList<>();
            for (Object o : raw) tags.add(String.valueOf(o));
            it.setTags(tags);
        }
        String metaJson = rs.getString("metadata");
        if (metaJson != null && !metaJson.isEmpty()) {
            it.setMetadata(JsonUtil.parseMap(metaJson));
        }
        return it;
    }
}
