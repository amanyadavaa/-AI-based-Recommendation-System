package com.guvi.recommend.model;

import java.util.List;
import java.util.Map;

/**
 * Concrete Food entity demonstrating Polymorphism and Inheritance.
 */
public class FoodItem extends AbstractCatalogItem {

    public FoodItem() {
        super();
        this.category = "FOOD";
    }

    public FoodItem(String id, String title, String description, double rating, int reviewCount, String imageUrl, List<String> tags, Double price) {
        super(id, title, "FOOD", description, rating, reviewCount, imageUrl, tags, price);
    }

    @Override
    public double calculateCategoryAffinity(User user) {
        double score = 0.5;
        if (user == null || user.getPreferences() == null) return score;
        Map<String, Object> prefs = user.getPreferences();

        // Dietary matching
        if (prefs.containsKey("dietaryPreference") && tags != null) {
            String diet = String.valueOf(prefs.get("dietaryPreference"));
            if (!"All".equalsIgnoreCase(diet) && tags.contains(diet)) {
                score += 0.35;
            }
        }

        if (prefs.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> cats = (List<String>) prefs.get("preferredCategories");
            if (cats != null && cats.contains("FOOD")) score += 0.25;
        }

        return Math.min(1.0, score);
    }
}
