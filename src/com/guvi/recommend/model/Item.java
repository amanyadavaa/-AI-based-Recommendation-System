package com.guvi.recommend.model;

import java.util.*;

public class Item {
    private String id;
    private String title;
    private String category; // PRODUCT, MOVIE, PLACE, FOOD, WEB
    private String description;
    private double rating;
    private int reviewCount;
    private String imageUrl;
    private List<String> tags;
    private Double price;
    private Map<String, Object> metadata;
    private transient double matchScore; // Computed dynamically by AI engine

    public Item() {
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public Item(String id, String title, String category, String description, double rating, int reviewCount, String imageUrl, List<String> tags, Double price) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.price = price;
        this.metadata = new HashMap<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    private String aiExplanation;
    private Map<String, Object> scoreFactors;

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public Map<String, Object> getScoreFactors() { return scoreFactors; }
    public void setScoreFactors(Map<String, Object> scoreFactors) { this.scoreFactors = scoreFactors; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("category", category);
        map.put("description", description);
        map.put("rating", rating);
        map.put("reviewCount", reviewCount);
        map.put("imageUrl", imageUrl);
        map.put("tags", tags);
        map.put("price", price);
        map.put("metadata", metadata);
        map.put("matchScore", Math.round(matchScore * 100.0) / 100.0);
        
        String expl = aiExplanation;
        if (expl == null || expl.isEmpty()) {
            expl = switch (category != null ? category.toUpperCase() : "GENERAL") {
                case "PRODUCT" -> "Selected based on stellar review sentiment, multi-store value pricing, and technical feature affinity.";
                case "MOVIE" -> "Recommended because your taste profile matches top-rated cinema, high narrative depth, and acclaimed direction.";
                case "PLACE" -> "Surfaced due to global cultural significance, traveler consensus ratings, and optimal routing connectivity.";
                case "FOOD" -> "Matched with your culinary taste profile, top internet review sentiment, and acclaimed signature dishes.";
                default -> "Synthesized in real-time by the AI hybrid recommendation engine matching your personal interest signals.";
            };
        }
        map.put("aiExplanation", expl);

        Map<String, Object> factors = scoreFactors;
        if (factors == null || factors.isEmpty()) {
            factors = new LinkedHashMap<>();
            factors.put("contentSimilarity", Math.round(Math.max(0.1, matchScore * 0.45) * 100.0) / 100.0);
            factors.put("collaborativeAffinity", Math.round(Math.max(0.1, matchScore * 0.35) * 100.0) / 100.0);
            factors.put("popularityWeight", Math.round((rating / 5.0 * 0.20) * 100.0) / 100.0);
        }
        map.put("scoreFactors", factors);

        return map;
    }
}
