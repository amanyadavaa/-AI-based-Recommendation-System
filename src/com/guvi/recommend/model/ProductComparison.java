package com.guvi.recommend.model;

import java.util.*;

public class ProductComparison {
    private String productId;
    private String productName;
    private String brand;
    private String bestStore;
    private double lowestPrice;
    private double highestPrice;
    private double averagePrice;
    private double maxSavings;
    private double savingsPercentage;
    private String priceTrend;
    private String recommendationVerdict;
    private List<StoreListing> storeListings;

    public static class StoreListing {
        private String storeName;
        private double price;
        private String currency;
        private double rating;
        private int reviewCount;
        private String shippingInfo;
        private boolean inStock;
        private String directUrl;
        private String badge; // "Best Deal", "Fastest Shipping", etc.

        public StoreListing(String storeName, double price, String currency, double rating, int reviewCount, String shippingInfo, boolean inStock, String directUrl, String badge) {
            this.storeName = storeName;
            this.price = price;
            this.currency = currency;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.shippingInfo = shippingInfo;
            this.inStock = inStock;
            this.directUrl = directUrl;
            this.badge = badge;
        }

        public String getStoreName() { return storeName; }
        public double getPrice() { return price; }
        public String getCurrency() { return currency; }
        public double getRating() { return rating; }
        public int getReviewCount() { return reviewCount; }
        public String getShippingInfo() { return shippingInfo; }
        public boolean isInStock() { return inStock; }
        public String getDirectUrl() { return directUrl; }
        public String getBadge() { return badge; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("storeName", storeName);
            map.put("price", price);
            map.put("currency", currency);
            map.put("rating", rating);
            map.put("reviewCount", reviewCount);
            map.put("shippingInfo", shippingInfo);
            map.put("inStock", inStock);
            map.put("directUrl", directUrl);
            map.put("badge", badge);
            return map;
        }
    }

    public ProductComparison(String productId, String productName, String brand, List<StoreListing> listings) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.storeListings = listings != null ? listings : new ArrayList<>();
        calculateMetrics();
    }

    private void calculateMetrics() {
        if (storeListings.isEmpty()) return;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        String best = "";
        for (StoreListing s : storeListings) {
            if (s.getPrice() < min) {
                min = s.getPrice();
                best = s.getStoreName();
            }
            if (s.getPrice() > max) {
                max = s.getPrice();
            }
            sum += s.getPrice();
        }
        this.lowestPrice = Math.round(min * 100.0) / 100.0;
        this.highestPrice = Math.round(max * 100.0) / 100.0;
        this.averagePrice = Math.round((sum / storeListings.size()) * 100.0) / 100.0;
        this.bestStore = best;
        this.maxSavings = Math.round((highestPrice - lowestPrice) * 100.0) / 100.0;
        this.savingsPercentage = highestPrice > 0 ? Math.round(((highestPrice - lowestPrice) / highestPrice * 100.0) * 10.0) / 10.0 : 0.0;
        if (savingsPercentage >= 15.0) {
            this.priceTrend = "🔥 All-Time Low Deal";
            this.recommendationVerdict = "Strong Buy: Save " + savingsPercentage + "% at " + bestStore;
        } else if (savingsPercentage >= 8.0) {
            this.priceTrend = "🏷️ Great Deal";
            this.recommendationVerdict = "Recommended: Best Price at " + bestStore;
        } else {
            this.priceTrend = "⚖️ Fair Market Price";
            this.recommendationVerdict = "Market Standard across retailers";
        }
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getBestStore() { return bestStore; }
    public double getLowestPrice() { return lowestPrice; }
    public double getHighestPrice() { return highestPrice; }
    public double getAveragePrice() { return averagePrice; }
    public double getMaxSavings() { return maxSavings; }
    public double getSavingsPercentage() { return savingsPercentage; }
    public String getPriceTrend() { return priceTrend; }
    public String getRecommendationVerdict() { return recommendationVerdict; }
    public List<StoreListing> getStoreListings() { return storeListings; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("brand", brand);
        map.put("bestStore", bestStore);
        map.put("lowestPrice", lowestPrice);
        map.put("highestPrice", highestPrice);
        map.put("averagePrice", averagePrice);
        map.put("maxSavings", maxSavings);
        map.put("savingsPercentage", savingsPercentage);
        map.put("priceTrend", priceTrend);
        map.put("recommendationVerdict", recommendationVerdict);
        List<Map<String, Object>> list = new ArrayList<>();
        for (StoreListing s : storeListings) {
            list.add(s.toMap());
        }
        map.put("storeListings", list);
        return map;
    }
}
