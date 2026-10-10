package com.guvi.recommend.model;

import java.util.*;

public class Interaction {
    private String id;
    private String userId;
    private String itemId;
    private String itemCategory;
    private String type; // "VIEW", "CLICK", "SEARCH", "RATE", "PURCHASE"
    private Double ratingValue;
    private long timestamp;

    public Interaction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Interaction(String userId, String itemId, String itemCategory, String type, Double ratingValue) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.itemId = itemId;
        this.itemCategory = itemCategory;
        this.type = type;
        this.ratingValue = ratingValue;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemCategory() { return itemCategory; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getRatingValue() { return ratingValue; }
    public void setRatingValue(Double ratingValue) { this.ratingValue = ratingValue; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("itemId", itemId);
        map.put("itemCategory", itemCategory);
        map.put("type", type);
        map.put("ratingValue", ratingValue);
        map.put("timestamp", timestamp);
        return map;
    }
}
