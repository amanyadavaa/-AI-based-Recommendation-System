package com.guvi.recommend.model;

import java.util.*;

public class User {
    private String id;
    private String username;
    private String email;
    private String role; // "ADMIN" or "USER"
    private Map<String, Object> preferences;
    private long createdAt;

    public User() {
        this.preferences = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
    }

    public User(String id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.preferences = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        initDefaultPreferences();
    }

    private void initDefaultPreferences() {
        preferences.put("preferredCategories", List.of("PRODUCT", "MOVIE", "PLACE", "FOOD"));
        preferences.put("pricePreference", "MEDIUM"); // LOW, MEDIUM, HIGH, ANY
        preferences.put("movieGenres", List.of("Action", "Sci-Fi", "Drama", "Comedy"));
        preferences.put("dietaryPreference", "All"); // All, Veg, Vegan, Non-Veg, Halal
        preferences.put("preferredStores", List.of("Amazon", "Walmart", "BestBuy", "eBay", "Flipkart"));
        preferences.put("travelMode", "driving");
        preferences.put("maxDistanceKm", 50);
        preferences.put("currentLocation", "Paris, France"); // default origin for shortest path demo
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("username", username);
        map.put("email", email);
        map.put("role", role);
        map.put("preferences", preferences);
        map.put("createdAt", createdAt);
        return map;
    }
}
