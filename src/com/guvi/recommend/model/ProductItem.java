package com.guvi.recommend.model;

import java.util.List;
import java.util.Map;

/**
 * Concrete Product entity demonstrating Polymorphism and Inheritance.
 */
public class ProductItem extends AbstractCatalogItem {

    public ProductItem() {
        super();
        this.category = "PRODUCT";
    }

    public ProductItem(String id, String title, String description, double rating, int reviewCount, String imageUrl, List<String> tags, Double price) {
        super(id, title, "PRODUCT", description, rating, reviewCount, imageUrl, tags, price);
    }

    @Override
    public double calculateCategoryAffinity(User user) {
        double score = 0.5;
        if (user == null || user.getPreferences() == null) return score;
        Map<String, Object> prefs = user.getPreferences();

        // Price tier matching
        if (prefs.containsKey("pricePreference") && price != null) {
            String pref = String.valueOf(prefs.get("pricePreference"));
            if ("LOW".equalsIgnoreCase(pref) && price < 50.0) score += 0.35;
            else if ("MEDIUM".equalsIgnoreCase(pref) && price >= 50.0 && price <= 400.0) score += 0.35;
            else if ("HIGH".equalsIgnoreCase(pref) && price > 400.0) score += 0.35;
        }

        // Category preference
        if (prefs.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> cats = (List<String>) prefs.get("preferredCategories");
            if (cats != null && cats.contains("PRODUCT")) score += 0.25;
        }

        return Math.min(1.0, score);
    }
}
