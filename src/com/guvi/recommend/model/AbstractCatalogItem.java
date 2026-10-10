package com.guvi.recommend.model;

import java.util.*;

/**
 * Abstract catalog item defining common attributes and polymorphic affinity scoring.
 */
public abstract class AbstractCatalogItem extends BaseEntity implements Comparable<AbstractCatalogItem> {
    protected String title;
    protected String category;
    protected String description;
    protected double rating;
    protected int reviewCount;
    protected String imageUrl;
    protected List<String> tags;
    protected Double price;
    protected Map<String, Object> metadata;
    protected transient double matchScore;

    public AbstractCatalogItem() {
        super();
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public AbstractCatalogItem(String id, String title, String category, String description, double rating, int reviewCount, String imageUrl, List<String> tags, Double price) {
        super(id);
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

    /**
     * Polymorphic method overridden by subclasses for domain-specific affinity scoring.
     */
    public abstract double calculateCategoryAffinity(User user);

    @Override
    public int compareTo(AbstractCatalogItem other) {
        return Double.compare(other.matchScore, this.matchScore); // Descending score order
    }

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

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

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
        return map;
    }
}
