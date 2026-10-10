package com.guvi.recommend.model;

import java.util.*;

public class FoodReview {
    private String foodId;
    private String name;
    private String cuisine;
    private String priceRange; // "$", "$$", "$$$"
    private double overallRating;
    private int totalReviews;
    private double sentimentScore; // 0.0 to 100.0%
    private int positivePercent;
    private int neutralPercent;
    private int negativePercent;
    private List<String> bestRecommendedDishes;
    private List<String> dietaryTags;
    private List<String> pros;
    private List<String> cons;
    private List<ReviewItem> sampleReviews;

    public static class ReviewItem {
        private String source; // "Google Reviews", "Yelp", "TripAdvisor", "Zomato"
        private String reviewer;
        private double rating;
        private String comment;
        private String sentiment; // "POSITIVE", "NEUTRAL", "NEGATIVE"
        private String date;

        public ReviewItem(String source, String reviewer, double rating, String comment, String sentiment, String date) {
            this.source = source;
            this.reviewer = reviewer;
            this.rating = rating;
            this.comment = comment;
            this.sentiment = sentiment;
            this.date = date;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("source", source);
            map.put("reviewer", reviewer);
            map.put("rating", rating);
            map.put("comment", comment);
            map.put("sentiment", sentiment);
            map.put("date", date);
            return map;
        }
    }

    public FoodReview(String foodId, String name, String cuisine, String priceRange, double overallRating, int totalReviews, double sentimentScore, int positivePercent, int neutralPercent, int negativePercent, List<String> bestRecommendedDishes, List<String> dietaryTags, List<String> pros, List<String> cons, List<ReviewItem> sampleReviews) {
        this.foodId = foodId;
        this.name = name;
        this.cuisine = cuisine;
        this.priceRange = priceRange;
        this.overallRating = overallRating;
        this.totalReviews = totalReviews;
        this.sentimentScore = sentimentScore;
        this.positivePercent = positivePercent;
        this.neutralPercent = neutralPercent;
        this.negativePercent = negativePercent;
        this.bestRecommendedDishes = bestRecommendedDishes != null ? bestRecommendedDishes : new ArrayList<>();
        this.dietaryTags = dietaryTags != null ? dietaryTags : new ArrayList<>();
        this.pros = pros != null ? pros : new ArrayList<>();
        this.cons = cons != null ? cons : new ArrayList<>();
        this.sampleReviews = sampleReviews != null ? sampleReviews : new ArrayList<>();
    }

    public String getFoodId() { return foodId; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getPriceRange() { return priceRange; }
    public double getOverallRating() { return overallRating; }
    public int getTotalReviews() { return totalReviews; }
    public double getSentimentScore() { return sentimentScore; }
    public int getPositivePercent() { return positivePercent; }
    public int getNeutralPercent() { return neutralPercent; }
    public int getNegativePercent() { return negativePercent; }
    public List<String> getBestRecommendedDishes() { return bestRecommendedDishes; }
    public List<String> getDietaryTags() { return dietaryTags; }
    public List<String> getPros() { return pros; }
    public List<String> getCons() { return cons; }
    public List<ReviewItem> getSampleReviews() { return sampleReviews; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("foodId", foodId);
        map.put("name", name);
        map.put("cuisine", cuisine);
        map.put("priceRange", priceRange);
        map.put("overallRating", overallRating);
        map.put("totalReviews", totalReviews);
        map.put("sentimentScore", sentimentScore);
        map.put("positivePercent", positivePercent);
        map.put("neutralPercent", neutralPercent);
        map.put("negativePercent", negativePercent);
        map.put("bestRecommendedDishes", bestRecommendedDishes);
        map.put("dietaryTags", dietaryTags);
        map.put("pros", pros);
        map.put("cons", cons);
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (ReviewItem r : sampleReviews) reviews.add(r.toMap());
        map.put("sampleReviews", reviews);
        return map;
    }
}
