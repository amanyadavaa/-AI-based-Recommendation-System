package com.guvi.recommend.test;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.model.*;
import com.guvi.recommend.util.JsonUtil;

import java.util.*;

/**
 * Phase 1 Test Suite: Verification of foundational models,
 * JSON serialization, configuration normalization, and domain structures.
 */
public class Phase1TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("      RUNNING PHASE 1 AUTOMATED VERIFICATION TEST SUITE          ");
        System.out.println("==================================================================");

        runTest("JsonUtil: Object to JSON Serialization", Phase1TestSuite::testJsonSerialization);
        runTest("JsonUtil: JSON String Parsing into Map & List", Phase1TestSuite::testJsonParsing);
        runTest("SystemConfig: Parameter Initialization & Normalization", Phase1TestSuite::testSystemConfig);
        runTest("User: Profile & Preference Management Defaults", Phase1TestSuite::testUserModel);
        runTest("Item: Multi-Domain Entity Structure", Phase1TestSuite::testItemModel);
        runTest("Interaction: Telemetry Event Logging", Phase1TestSuite::testInteractionModel);
        runTest("ProductComparison: Cross-Store Metric Calculation", Phase1TestSuite::testProductComparison);
        runTest("RouteResult: Shortest Path Data & Duration Formatting", Phase1TestSuite::testRouteResult);
        runTest("FoodReview: Sentiment Analysis Percentage Breakdown", Phase1TestSuite::testFoodReview);
        runTest("MovieDetails: Verified Direct Download Links", Phase1TestSuite::testMovieDetails);

        System.out.println("==================================================================");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed (Total %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.err.println("❌ Phase 1 test suite failed!");
            System.exit(1);
        } else {
            System.out.println("✔ ALL PHASE 1 TESTS COMPLETED SUCCESSFULLY!");
        }
    }

    private static void runTest(String testName, Runnable testMethod) {
        try {
            testMethod.run();
            System.out.printf("  [PASS] %s%n", testName);
            testsPassed++;
        } catch (Throwable t) {
            System.err.printf("  [FAIL] %s - Reason: %s%n", testName, t.getMessage());
            t.printStackTrace(System.err);
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    // 1. JSON Serialization
    private static void testJsonSerialization() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", "Sony Headphones");
        data.put("price", 399.99);
        data.put("inStock", true);
        data.put("tags", List.of("audio", "wireless"));

        String json = JsonUtil.toJson(data);
        assertTrue(json.contains("\"title\":\"Sony Headphones\""), "Should contain title string");
        assertTrue(json.contains("399.99"), "Should contain price number");
        assertTrue(json.contains("true"), "Should contain boolean true");
        assertTrue(json.contains("[\"audio\",\"wireless\"]"), "Should serialize list tags");
    }

    // 2. JSON Parsing
    private static void testJsonParsing() {
        String json = "{\"category\":\"MOVIE\",\"rating\":8.8,\"features\":[\"4K\",\"Direct Download\"],\"active\":true}";
        Map<String, Object> map = JsonUtil.parseMap(json);

        assertEquals("MOVIE", map.get("category"), "Category should parse correctly");
        assertEquals(8.8, ((Number) map.get("rating")).doubleValue(), "Rating should parse correctly");
        assertEquals(true, map.get("active"), "Boolean should parse correctly");

        @SuppressWarnings("unchecked")
        List<Object> features = (List<Object>) map.get("features");
        assertEquals(2, features.size(), "List length should be 2");
        assertEquals("4K", features.get(0), "First feature should be 4K");
    }

    // 3. System Config
    private static void testSystemConfig() {
        SystemConfig config = new SystemConfig();
        assertEquals(0.40, config.getContentWeight(), "Initial content weight");
        assertEquals(0.35, config.getCollaborativeWeight(), "Initial collaborative weight");
        assertEquals(0.25, config.getPopularityWeight(), "Initial popularity weight");

        // Update weights
        Map<String, Object> updates = Map.of(
                "contentWeight", 0.50,
                "collaborativeWeight", 0.30,
                "popularityWeight", 0.20,
                "explorationRate", 0.25
        );
        config.updateFromMap(updates, "TestRunner");

        assertEquals(0.50, config.getContentWeight(), "Updated content weight");
        assertEquals(0.30, config.getCollaborativeWeight(), "Updated collaborative weight");
        assertEquals(0.20, config.getPopularityWeight(), "Updated popularity weight");
        assertEquals(0.25, config.getExplorationRate(), "Updated exploration rate");
    }

    // 4. User Model
    private static void testUserModel() {
        User user = new User("usr-test", "Tester", "test@guvi.com", "USER");
        assertEquals("usr-test", user.getId(), "User ID check");
        assertEquals("USER", user.getRole(), "User role check");

        Map<String, Object> prefs = user.getPreferences();
        assertTrue(prefs.containsKey("preferredCategories"), "Should initialize preferred categories");
        assertTrue(prefs.containsKey("pricePreference"), "Should initialize price preference");

        Map<String, Object> serialized = user.toMap();
        assertEquals("Tester", serialized.get("username"), "Serialized username match");
    }

    // 5. Item Model
    private static void testItemModel() {
        Item item = new Item(
                "prod-test", "Test Laptop", "PRODUCT",
                "High performance laptop", 4.9, 500,
                "http://img.jpg", List.of("Tech", "PC"), 999.0
        );
        assertEquals("prod-test", item.getId(), "Item ID check");
        assertEquals("PRODUCT", item.getCategory(), "Item category check");
        assertEquals(999.0, item.getPrice(), "Price check");

        item.setMatchScore(0.88);
        Map<String, Object> map = item.toMap();
        assertEquals(0.88, map.get("matchScore"), "Match score export");
    }

    // 6. Interaction Model
    private static void testInteractionModel() {
        Interaction in = new Interaction("usr-1", "prod-1", "PRODUCT", "CLICK", null);
        assertEquals("usr-1", in.getUserId(), "User ID match");
        assertEquals("CLICK", in.getType(), "Interaction type match");
        assertTrue(in.getTimestamp() > 0, "Timestamp should be positive");
    }

    // 7. Product Comparison
    private static void testProductComparison() {
        List<ProductComparison.StoreListing> listings = List.of(
                new ProductComparison.StoreListing("Store A", 100.0, "USD", 4.5, 100, "Free", true, "http://a", "Best Deal"),
                new ProductComparison.StoreListing("Store B", 120.0, "USD", 4.6, 200, "$5", true, "http://b", "Standard"),
                new ProductComparison.StoreListing("Store C", 90.0, "USD", 4.8, 300, "Free", true, "http://c", "Lowest Price")
        );
        ProductComparison comp = new ProductComparison("p1", "Test Item", "Brand X", listings);

        assertEquals(90.0, comp.getLowestPrice(), "Lowest price must be 90.0");
        assertEquals(120.0, comp.getHighestPrice(), "Highest price must be 120.0");
        assertEquals("Store C", comp.getBestStore(), "Best store must be Store C");
        assertEquals(30.0, comp.getMaxSavings(), "Max savings must be 30.0");
    }

    // 8. Route Result
    private static void testRouteResult() {
        List<RouteResult.RouteStep> steps = List.of(
                new RouteResult.RouteStep("Head north", 500, 60, "straight"),
                new RouteResult.RouteStep("Turn right", 1000, 120, "turn")
        );
        RouteResult route = new RouteResult(
                "Point A", 48.85, 2.35,
                "Point B", 48.86, 2.36,
                5.5, 12.0, "Fastest route", steps, Collections.emptyList()
        );
        assertEquals(5.5, route.getDistanceKm(), "Distance in km");
        assertEquals("12 min", route.getFormattedDuration(), "Formatted duration");
        assertTrue(route.getNavigationUrl().contains("maps/dir/"), "Google maps URL valid");
    }

    // 9. Food Review
    private static void testFoodReview() {
        FoodReview review = new FoodReview(
                "f1", "Gourmet Pizza", "Italian", "$$",
                4.8, 1200, 92.5,
                88, 8, 4,
                List.of("Margherita Special"), List.of("Vegetarian"),
                List.of("Great crust"), List.of("Busy hours"),
                Collections.emptyList()
        );
        assertEquals(100, review.getPositivePercent() + review.getNeutralPercent() + review.getNegativePercent(),
                "Sentiment percentages must sum to 100%");
        assertEquals("Gourmet Pizza", review.getName(), "Name match");
        assertEquals(4.8, review.getOverallRating(), "Overall rating match");
    }

    // 10. Movie Details
    private static void testMovieDetails() {
        List<MovieDetails.DownloadLink> downloads = List.of(
                new MovieDetails.DownloadLink("Archive Vault", "1080p MP4", "2.5 GB", "https://archive.org/movie.mp4", "MP4", true)
        );
        List<MovieDetails.StreamingPlatform> streams = List.of(
                new MovieDetails.StreamingPlatform("Netflix", "Subscription", "https://netflix.com", "Free with Sub")
        );
        MovieDetails movie = new MovieDetails(
                "m1", "Sci-Fi Thriller", 2024, "Sci-Fi", "Director X",
                List.of("Actor A"), "2h 15m", 8.9, 94, "A great story", "http://trailer",
                downloads, streams
        );
        assertEquals(8.9, movie.getImdbRating(), "IMDb rating");
        assertEquals(94, movie.getRottenTomatoesScore(), "Rotten Tomatoes score");
        assertEquals(1, movie.getDownloadLinks().size(), "Download links count");
        assertEquals("MP4", movie.getDownloadLinks().get(0).toMap().get("format"), "Download format");
    }
}
