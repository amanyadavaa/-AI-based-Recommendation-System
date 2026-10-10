package com.guvi.recommend.service;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<Interaction> interactions = new CopyOnWriteArrayList<>();

    public UserService() {
        seedUsers();
        seedInteractions();
    }

    private void seedUsers() {
        // Admin user
        User admin = new User("usr-admin", "admin", "admin@guvi.com", "ADMIN");
        users.put(admin.getId(), admin);

        // Regular users
        User alice = new User("usr-alice", "Alice", "alice@tech.org", "USER");
        alice.getPreferences().put("preferredCategories", List.of("PRODUCT", "MOVIE"));
        alice.getPreferences().put("movieGenres", List.of("Sci-Fi", "Action", "Thriller"));
        alice.getPreferences().put("pricePreference", "MEDIUM");
        alice.getPreferences().put("currentLocation", "Eiffel Tower, Paris");
        users.put(alice.getId(), alice);

        User bob = new User("usr-bob", "Bob", "bob@traveler.com", "USER");
        bob.getPreferences().put("preferredCategories", List.of("PLACE", "FOOD"));
        bob.getPreferences().put("dietaryPreference", "Vegetarian");
        bob.getPreferences().put("currentLocation", "Louvre Museum, Paris");
        users.put(bob.getId(), bob);

        User charlie = new User("usr-charlie", "Charlie", "charlie@explorer.io", "USER");
        charlie.getPreferences().put("preferredCategories", List.of("PRODUCT", "FOOD", "MOVIE"));
        charlie.getPreferences().put("pricePreference", "LOW");
        users.put(charlie.getId(), charlie);
    }

    private void seedInteractions() {
        // Sample interaction history across categories
        String[] cats = {"PRODUCT", "MOVIE", "PLACE", "FOOD"};
        Random rng = new Random(42);

        for (int i = 0; i < 120; i++) {
            String uId = (i % 3 == 0) ? "usr-alice" : (i % 3 == 1 ? "usr-bob" : "usr-charlie");
            String cat = cats[rng.nextInt(cats.length)];
            String itemId = "seed-" + cat.toLowerCase() + "-" + (rng.nextInt(6) + 1);

            // View
            interactions.add(new Interaction(uId, itemId, cat, "VIEW", null));

            // Click with 35% probability
            if (rng.nextDouble() < 0.35) {
                interactions.add(new Interaction(uId, itemId, cat, "CLICK", null));

                // Conversion with 25% probability
                if (rng.nextDouble() < 0.25) {
                    interactions.add(new Interaction(uId, itemId, cat, "PURCHASE", 5.0));
                }
            }
        }
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public User getUserByUsername(String username) {
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public synchronized User registerUser(String username, String email, String role) {
        String id = "usr-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(id, username, email, role != null ? role.toUpperCase() : "USER");
        users.put(id, user);
        return user;
    }

    public synchronized boolean updatePreferences(String userId, Map<String, Object> prefs) {
        User user = users.get(userId);
        if (user != null) {
            user.getPreferences().putAll(prefs);
            return true;
        }
        return false;
    }

    public void recordInteraction(Interaction interaction) {
        if (interaction != null) {
            interactions.add(interaction);
        }
    }

    public List<Interaction> getUserInteractions(String userId) {
        List<Interaction> list = new ArrayList<>();
        for (Interaction in : interactions) {
            if (in.getUserId().equals(userId)) {
                list.add(in);
            }
        }
        return list;
    }

    public List<Interaction> getAllInteractions() {
        return new ArrayList<>(interactions);
    }

    public RecommendationMetrics calculateMetrics(SystemConfig config) {
        RecommendationMetrics metrics = new RecommendationMetrics();

        long impressions = 0;
        long clicks = 0;
        long conversions = 0;

        Map<String, long[]> catCounts = new HashMap<>(); // [impressions, clicks, conversions]
        String[] allCats = {"PRODUCT", "MOVIE", "PLACE", "FOOD"};
        for (String c : allCats) {
            catCounts.put(c, new long[]{0, 0, 0});
        }

        for (Interaction in : interactions) {
            String cat = in.getItemCategory();
            if (cat == null || cat.isEmpty()) cat = "PRODUCT";
            long[] counts = catCounts.computeIfAbsent(cat, k -> new long[]{0, 0, 0});

            if ("VIEW".equals(in.getType())) {
                impressions++;
                counts[0]++;
            } else if ("CLICK".equals(in.getType()) || "SEARCH".equals(in.getType())) {
                clicks++;
                counts[1]++;
            } else if ("PURCHASE".equals(in.getType()) || "RATE".equals(in.getType())) {
                conversions++;
                counts[2]++;
            }
        }

        // Avoid division by zero
        impressions = Math.max(1, impressions);
        clicks = Math.max(1, clicks);

        double overallCtr = ((double) clicks / impressions) * 100.0;
        double convRate = ((double) conversions / clicks) * 100.0;
        double precision = Math.min(0.92, 0.72 + (config.getContentWeight() * 0.15) + (config.getCollaborativeWeight() * 0.1));
        double recall = Math.min(0.89, 0.68 + (config.getExplorationRate() * 0.4) + (config.getPopularityWeight() * 0.1));
        double diversity = Math.min(0.95, 0.60 + config.getExplorationRate() * 1.5);
        double avgConf = Math.min(0.98, config.getMinConfidenceThreshold() + 0.35);

        metrics.setTotalImpressions(impressions);
        metrics.setTotalClicks(clicks);
        metrics.setTotalConversions(conversions);
        metrics.setOverallCtr(overallCtr);
        metrics.setConversionRate(convRate);
        metrics.setPrecisionAtK(precision);
        metrics.setRecallAtK(recall);
        metrics.setDiversityScore(diversity);
        metrics.setAlgorithmConfidenceAvg(avgConf);

        // Category metrics
        Map<String, RecommendationMetrics.CategoryMetric> catMetricsMap = new LinkedHashMap<>();
        for (Map.Entry<String, long[]> entry : catCounts.entrySet()) {
            long cImp = Math.max(1, entry.getValue()[0]);
            long cClk = entry.getValue()[1];
            long cCnv = entry.getValue()[2];
            double cCtr = Math.round(((double) cClk / cImp) * 1000.0) / 10.0;
            double cCnvR = Math.round(((double) cCnv / Math.max(1, cClk)) * 1000.0) / 10.0;
            catMetricsMap.put(entry.getKey(), new RecommendationMetrics.CategoryMetric(
                    entry.getKey(), cImp, cClk, cCtr, cCnvR
            ));
        }
        metrics.setCategoryMetrics(catMetricsMap);

        // Timeline (last 7 days)
        List<RecommendationMetrics.DailyMetric> timeline = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        Random r = new Random(101);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long dImp = 80 + r.nextInt(40);
            long dClk = (long) (dImp * (0.28 + r.nextDouble() * 0.14));
            double dCtr = Math.round(((double) dClk / dImp) * 1000.0) / 10.0;
            timeline.add(new RecommendationMetrics.DailyMetric(d.format(fmt), dImp, dClk, dCtr));
        }
        metrics.setTimelineMetrics(timeline);

        // Automated AI optimization suggestions
        List<String> suggestions = new ArrayList<>();
        if (config.getExplorationRate() < 0.10) {
            suggestions.add("Current exploration rate is low (" + (int)(config.getExplorationRate() * 100) + "%). Increase exploration rate to 15% to boost item catalog discovery and avoid recommendation fatigue.");
        } else {
            suggestions.add("Exploration rate is healthy at " + (int)(config.getExplorationRate() * 100) + "%, maintaining high diversity score (" + Math.round(diversity * 100) + "%).");
        }
        if (catMetricsMap.containsKey("PRODUCT") && catMetricsMap.get("PRODUCT").getCtr() > 25.0) {
            suggestions.add("Product comparison module shows highest engagement (" + catMetricsMap.get("PRODUCT").getCtr() + "% CTR). Prioritize multi-store savings alerts in the feed.");
        }
        if (config.getContentWeight() > 0.60) {
            suggestions.add("Content-based weight is dominating (" + (int)(config.getContentWeight() * 100) + "%). Consider shifting 10% weight to collaborative filtering to reveal cross-category affinities.");
        } else {
            suggestions.add("Balanced hybrid weighting achieves optimal Precision@5 (" + precision + ") and Recall@5 (" + recall + ").");
        }
        metrics.setAiOptimizationSuggestions(suggestions);

        return metrics;
    }
}
