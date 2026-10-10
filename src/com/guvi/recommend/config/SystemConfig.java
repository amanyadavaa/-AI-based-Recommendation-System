package com.guvi.recommend.config;

import java.util.*;

public class SystemConfig {
    private double contentWeight = 0.40;
    private double collaborativeWeight = 0.35;
    private double popularityWeight = 0.25;
    private double priceSensitivity = 0.50;
    private double explorationRate = 0.15; // Epsilon-greedy exploration
    private double minConfidenceThreshold = 0.40;
    private boolean enableLiveWebSearch = true;
    private int maxRecommendationsPerQuery = 12;
    private int defaultSearchRadiusKm = 35;
    private String activeRoutingEngine = "OSRM_NOMINATIM";
    private long lastUpdatedAt = System.currentTimeMillis();
    private String lastUpdatedBy = "system";

    public SystemConfig() {}

    public synchronized void updateFromMap(Map<String, Object> map, String updatedBy) {
        if (map.containsKey("contentWeight")) {
            this.contentWeight = toDouble(map.get("contentWeight"));
        }
        if (map.containsKey("collaborativeWeight")) {
            this.collaborativeWeight = toDouble(map.get("collaborativeWeight"));
        }
        if (map.containsKey("popularityWeight")) {
            this.popularityWeight = toDouble(map.get("popularityWeight"));
        }
        if (map.containsKey("priceSensitivity")) {
            this.priceSensitivity = toDouble(map.get("priceSensitivity"));
        }
        if (map.containsKey("explorationRate")) {
            this.explorationRate = toDouble(map.get("explorationRate"));
        }
        if (map.containsKey("minConfidenceThreshold")) {
            this.minConfidenceThreshold = toDouble(map.get("minConfidenceThreshold"));
        }
        if (map.containsKey("enableLiveWebSearch")) {
            this.enableLiveWebSearch = Boolean.parseBoolean(String.valueOf(map.get("enableLiveWebSearch")));
        }
        if (map.containsKey("maxRecommendationsPerQuery")) {
            this.maxRecommendationsPerQuery = (int) toDouble(map.get("maxRecommendationsPerQuery"));
        }
        if (map.containsKey("defaultSearchRadiusKm")) {
            this.defaultSearchRadiusKm = (int) toDouble(map.get("defaultSearchRadiusKm"));
        }
        if (map.containsKey("activeRoutingEngine")) {
            this.activeRoutingEngine = String.valueOf(map.get("activeRoutingEngine"));
        }
        normalizeWeights();
        this.lastUpdatedAt = System.currentTimeMillis();
        this.lastUpdatedBy = updatedBy != null ? updatedBy : "admin";
    }

    private void normalizeWeights() {
        double sum = contentWeight + collaborativeWeight + popularityWeight;
        if (sum > 0) {
            contentWeight = Math.round((contentWeight / sum) * 100.0) / 100.0;
            collaborativeWeight = Math.round((collaborativeWeight / sum) * 100.0) / 100.0;
            popularityWeight = Math.round((1.0 - contentWeight - collaborativeWeight) * 100.0) / 100.0;
        }
    }

    public synchronized void resetToDefaults() {
        this.contentWeight = 0.40;
        this.collaborativeWeight = 0.35;
        this.popularityWeight = 0.25;
        this.priceSensitivity = 0.50;
        this.explorationRate = 0.15;
        this.minConfidenceThreshold = 0.40;
        this.enableLiveWebSearch = true;
        this.maxRecommendationsPerQuery = 12;
        this.defaultSearchRadiusKm = 35;
        this.activeRoutingEngine = "OSRM_NOMINATIM";
        this.lastUpdatedAt = System.currentTimeMillis();
        this.lastUpdatedBy = "system";
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getContentWeight() { return contentWeight; }
    public double getCollaborativeWeight() { return collaborativeWeight; }
    public double getPopularityWeight() { return popularityWeight; }
    public double getPriceSensitivity() { return priceSensitivity; }
    public double getExplorationRate() { return explorationRate; }
    public double getMinConfidenceThreshold() { return minConfidenceThreshold; }
    public boolean isEnableLiveWebSearch() { return enableLiveWebSearch; }
    public int getMaxRecommendationsPerQuery() { return maxRecommendationsPerQuery; }
    public int getDefaultSearchRadiusKm() { return defaultSearchRadiusKm; }
    public String getActiveRoutingEngine() { return activeRoutingEngine; }
    public long getLastUpdatedAt() { return lastUpdatedAt; }
    public String getLastUpdatedBy() { return lastUpdatedBy; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("contentWeight", contentWeight);
        map.put("collaborativeWeight", collaborativeWeight);
        map.put("popularityWeight", popularityWeight);
        map.put("priceSensitivity", priceSensitivity);
        map.put("explorationRate", explorationRate);
        map.put("minConfidenceThreshold", minConfidenceThreshold);
        map.put("enableLiveWebSearch", enableLiveWebSearch);
        map.put("maxRecommendationsPerQuery", maxRecommendationsPerQuery);
        map.put("defaultSearchRadiusKm", defaultSearchRadiusKm);
        map.put("activeRoutingEngine", activeRoutingEngine);
        map.put("lastUpdatedAt", lastUpdatedAt);
        map.put("lastUpdatedBy", lastUpdatedBy);
        return map;
    }
}
