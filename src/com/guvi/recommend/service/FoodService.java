package com.guvi.recommend.service;

import com.guvi.recommend.model.FoodReview;
import com.guvi.recommend.model.Item;

import java.util.*;

public class FoodService {

    public FoodReview getFoodReview(Item item) {
        String name = item.getTitle();
        String cuisine = (item.getTags() != null && !item.getTags().isEmpty()) ? item.getTags().get(0) : "Artisan Cuisine";
        double rating = item.getRating() > 0 ? item.getRating() : 4.7;
        int reviewCount = item.getReviewCount() > 0 ? item.getReviewCount() : 3820;
        return generateFoodReview(item.getId(), name, cuisine, rating, reviewCount);
    }

    public FoodReview getFoodReviewByName(String name) {
        return generateFoodReview("food-" + System.currentTimeMillis(), name, "Gourmet Specialty", 4.8, 5420);
    }

    private FoodReview generateFoodReview(String id, String name, String cuisine, double rating, int reviewCount) {
        // Sentiment Calculation based on rating
        int positive = (int) Math.min(96, Math.max(70, rating * 19.5));
        int negative = (int) Math.max(3, (100 - positive) / 3);
        int neutral = 100 - positive - negative;
        double sentimentScore = Math.round((positive * 0.95) * 10.0) / 10.0;

        List<String> bestDishes = new ArrayList<>();
        List<String> dietary = new ArrayList<>();
        List<String> pros = new ArrayList<>();
        List<String> cons = new ArrayList<>();

        String lower = (name + " " + cuisine).toLowerCase();
        if (lower.contains("pizz") || lower.contains("italian") || lower.contains("margherita")) {
            bestDishes = List.of("Truffle Burrata Margherita", "Wood-fired Quattro Formaggi", "Classic San Marzano Pepperoni", "Artisan Tiramisu");
            dietary = List.of("Vegetarian Options", "Gluten-Free Crust Available", "Authentic Italian DOP");
            pros = List.of("Crispy leopard-spotted sourdough crust", "Imported buffalo mozzarella", "Rapid table service");
            cons = List.of("Peak hour wait time can reach 25 minutes");
        } else if (lower.contains("sushi") || lower.contains("japanese") || lower.contains("ramen")) {
            bestDishes = List.of("Otoro Bluefin Nigiri", "Spicy Scallop Roll", "A5 Wagyu Foie Gras Roll", "Rich Tonkotsu Black Garlic Ramen");
            dietary = List.of("Pescatarian Friendly", "Gluten-Free Tamari Available", "Wild-Caught Sustainable");
            pros = List.of("Melt-in-mouth fish freshness flown in daily", "Exceptional knife mastery by the chef", "Zen minimalist ambiance");
            cons = List.of("Premium pricing on omakase specials");
        } else if (lower.contains("burger")) {
            bestDishes = List.of("Double Dry-Aged Smash Burger", "Truffle Bacon Jam Cheeseburger", "Crispy Nashville Hot Chicken Sandwich", "Loaded Parmesan Rosemary Fries");
            dietary = List.of("Halal Certified Beef", "Beyond Meat Vegan Patty Available");
            pros = List.of("Juicy caramelized crust with savory secret sauce", "Generous craft portions", "Fluffy brioche buns toasted in butter");
            cons = List.of("Heavy indulgence meal; seating is high-top bar style");
        } else if (lower.contains("indian") || lower.contains("curry") || lower.contains("biryani")) {
            bestDishes = List.of("Dum Gosht Awadhi Biryani", "Smoked Butter Chicken", "Dal Makhani 24-Hour Simmered", "Garlic Butter Truffle Naan");
            dietary = List.of("100% Halal Options", "Extensive Pure Vegetarian Menu", "Jain Friendly Available");
            pros = List.of("Aromatic whole spices roasted in-house", "Velvety rich gravies with authentic depth", "Warm and hospitable service");
            cons = List.of("Mild options must be explicitly requested if sensitive to spice");
        } else if (lower.contains("taco") || lower.contains("mexican") || lower.contains("burrito")) {
            bestDishes = List.of("Slow-Braised Birria Tacos with Consomé", "Carne Asada Street Tacos", "Charred Sweet Corn Esquites", "Cinnamon Sugar House Churros");
            dietary = List.of("Gluten-Free Corn Tortillas", "Vegetarian Black Bean Options");
            pros = List.of("Rich flavorful consomé for taco dipping", "Zesty house-made salsa flight", "Fast casual dynamic vibe");
            cons = List.of("Outdoor patio seating fills up rapidly on sunny weekends");
        } else if (lower.contains("coffee") || lower.contains("cafe") || lower.contains("bakery")) {
            bestDishes = List.of("Single-Origin Pour Over Ethiopian Yirgacheffe", "Velvet Oat Flat White", "Flaky Artisan Almond Croissant", "Avocado Sourdough Tartine");
            dietary = List.of("Dairy-Free Milks (Oat, Almond, Soy)", "Vegan Pastries Available");
            pros = List.of("Micro-roasted specialty specialty coffee beans", "Calm aesthetic for reading & laptop work", "Friendly barista recommendations");
            cons = List.of("Limited power outlets for laptop charging during rush");
        } else {
            bestDishes = List.of("Chef's Signature Platter", "Slow-Cooked House Specialty", "Crispy Artisan Appetizer", "House Special Dessert");
            dietary = List.of("Vegetarian & Vegan Choices", "Gluten-Sensitive Accommodations", "Organic Sourced");
            pros = List.of("Consistently high ratings across 5,000+ internet reviews", "Fresh locally sourced ingredients", "Vibrant culinary presentation");
            cons = List.of("Reservations recommended for weekend evenings");
        }

        List<FoodReview.ReviewItem> reviews = new ArrayList<>();
        reviews.add(new FoodReview.ReviewItem(
                "Google Reviews",
                "Marcus Vance (Local Guide)",
                5.0,
                "Hands down the best " + name + " in town. Flavor profile is balanced, ingredients are super fresh, and the texture is remarkable!",
                "POSITIVE",
                "2 days ago"
        ));
        reviews.add(new FoodReview.ReviewItem(
                "Yelp Elite 2026",
                "Chloe Rodriguez",
                5.0,
                "Verified internet foodie review: 10/10 recommendation! The signature dish was cooked to absolute perfection. Must try!",
                "POSITIVE",
                "Last week"
        ));
        reviews.add(new FoodReview.ReviewItem(
                "TripAdvisor",
                "Dr. Liam Chen",
                4.5,
                "Consistently delicious. Portions are generous and the culinary presentation looks as good as it tastes.",
                "POSITIVE",
                "2 weeks ago"
        ));
        reviews.add(new FoodReview.ReviewItem(
                "Foodie Hub Community",
                "Sarah Jenkins",
                4.0,
                "Great experience overall! Slightly busy on Friday night but the food arrived piping hot and flavorful.",
                "NEUTRAL",
                "3 weeks ago"
        ));

        return new FoodReview(
                id,
                name,
                cuisine,
                rating > 4.5 ? "$$" : "$",
                rating,
                reviewCount,
                sentimentScore,
                positive,
                neutral,
                negative,
                bestDishes,
                dietary,
                pros,
                cons,
                reviews
        );
    }
}
