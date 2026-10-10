package com.guvi.recommend.model;

import java.util.List;
import java.util.Map;

/**
 * Concrete Place entity demonstrating Polymorphism and Inheritance.
 */
public class PlaceItem extends AbstractCatalogItem {

    public PlaceItem() {
        super();
        this.category = "PLACE";
    }

    public PlaceItem(String id, String title, String description, double rating, int reviewCount, String imageUrl, List<String> tags) {
        super(id, title, "PLACE", description, rating, reviewCount, imageUrl, tags, null);
    }

    @Override
    public double calculateCategoryAffinity(User user) {
        double score = 0.5;
        if (user == null || user.getPreferences() == null) return score;
        Map<String, Object> prefs = user.getPreferences();

        if (prefs.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> cats = (List<String>) prefs.get("preferredCategories");
            if (cats != null && cats.contains("PLACE")) score += 0.35;
        }

        // Travel mode preference
        if (prefs.containsKey("travelMode")) {
            score += 0.15;
        }

        return Math.min(1.0, score);
    }
}
