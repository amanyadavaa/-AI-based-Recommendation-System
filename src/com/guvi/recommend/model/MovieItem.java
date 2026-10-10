package com.guvi.recommend.model;

import java.util.List;
import java.util.Map;

/**
 * Concrete Movie entity demonstrating Polymorphism and Inheritance.
 */
public class MovieItem extends AbstractCatalogItem {

    public MovieItem() {
        super();
        this.category = "MOVIE";
    }

    public MovieItem(String id, String title, String description, double rating, int reviewCount, String imageUrl, List<String> tags) {
        super(id, title, "MOVIE", description, rating, reviewCount, imageUrl, tags, null);
    }

    @Override
    public double calculateCategoryAffinity(User user) {
        double score = 0.5;
        if (user == null || user.getPreferences() == null) return score;
        Map<String, Object> prefs = user.getPreferences();

        // Genre matching
        if (prefs.containsKey("movieGenres") && tags != null) {
            @SuppressWarnings("unchecked")
            List<String> userGenres = (List<String>) prefs.get("movieGenres");
            if (userGenres != null) {
                for (String g : userGenres) {
                    if (tags.contains(g)) {
                        score += 0.35;
                        break;
                    }
                }
            }
        }

        // Category preference
        if (prefs.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> cats = (List<String>) prefs.get("preferredCategories");
            if (cats != null && cats.contains("MOVIE")) score += 0.25;
        }

        return Math.min(1.0, score);
    }
}
