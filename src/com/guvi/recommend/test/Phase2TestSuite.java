package com.guvi.recommend.test;

import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;

import java.util.*;

/**
 * Phase 2 Test Suite: Comprehensive verification of all 5 domain engines:
 * 2.1 ComparisonService (Multi-store price comparison)
 * 2.2 MovieService (Direct downloads & streaming links)
 * 2.3 RoutingService (Shortest path navigation & geocoding)
 * 2.4 FoodService (Internet reviews & sentiment analysis)
 * 2.5 InternetSearchService (Intent detection & live web synthesis)
 */
public class Phase2TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("      RUNNING PHASE 2 CORE DOMAIN SERVICES TEST SUITE            ");
        System.out.println("==================================================================");

        ComparisonService comparisonService = new ComparisonService();
        MovieService movieService = new MovieService();
        RoutingService routingService = new RoutingService();
        FoodService foodService = new FoodService();
        InternetSearchService searchService = new InternetSearchService(comparisonService, movieService, routingService, foodService);

        // Sub-phase 2.1
        runTest("Sub-phase 2.1: Multi-Store Price Comparison (6 Retailers)", () -> testSubphase21(comparisonService));
        runTest("Sub-phase 2.1: Brand Detection & Price Heuristics", () -> testSubphase21Heuristics(comparisonService));

        // Sub-phase 2.2
        runTest("Sub-phase 2.2: Movie Direct Download Mirrors & Streaming Providers", () -> testSubphase22(movieService));
        runTest("Sub-phase 2.2: Year Extraction & Director Recognition", () -> testSubphase22Metadata(movieService));

        // Sub-phase 2.3
        runTest("Sub-phase 2.3: Shortest Route Calculation & Turn Directions", () -> testSubphase23(routingService));
        runTest("Sub-phase 2.3: Geocoding Cache & Global Landmark Resolution", () -> testSubphase23Geocoding(routingService));

        // Sub-phase 2.4
        runTest("Sub-phase 2.4: Food Sentiment Tri-Color Percentage Breakdown", () -> testSubphase24(foodService));
        runTest("Sub-phase 2.4: Multi-Cuisine Signature Dishes & Consensus", () -> testSubphase24Cuisines(foodService));

        // Sub-phase 2.5
        runTest("Sub-phase 2.5: Category Intent Classifier Precision", () -> testSubphase25Intent(searchService));
        runTest("Sub-phase 2.5: Omnichannel Dynamic Web Synthesis", () -> testSubphase25Synthesis(searchService));

        System.out.println("==================================================================");
        System.out.printf("PHASE 2 SUMMARY: %d Passed, %d Failed (Total %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.err.println("❌ Phase 2 test suite failed!");
            System.exit(1);
        } else {
            System.out.println("✔ ALL 5 SUB-PHASES OF PHASE 2 VERIFIED SUCCESSFULLY!");
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

    // --- Sub-phase 2.1 Tests ---
    private static void testSubphase21(ComparisonService svc) {
        Item item = new Item("p1", "Sony WH-1000XM5 Headphones", "PRODUCT", "ANC Headphones", 4.8, 1200, "img.jpg", List.of("Sony", "Audio"), 399.99);
        ProductComparison comp = svc.compareProduct(item);

        assertEquals(6, comp.getStoreListings().size(), "Must compare across 6 stores");
        assertTrue(comp.getLowestPrice() <= comp.getAveragePrice(), "Lowest <= Avg");
        assertTrue(comp.getAveragePrice() <= comp.getHighestPrice(), "Avg <= Highest");
        assertTrue(comp.getMaxSavings() > 0, "Max savings must be positive");
        assertTrue(comp.getSavingsPercentage() > 0, "Savings percentage must be positive");
        assertTrue(comp.getPriceTrend() != null && !comp.getPriceTrend().isEmpty(), "Price trend should be set");
        assertTrue(comp.getRecommendationVerdict() != null, "Recommendation verdict should be set");
    }

    private static void testSubphase21Heuristics(ComparisonService svc) {
        ProductComparison compMac = svc.compareByQuery("Apple MacBook Air 15");
        assertEquals("Apple", compMac.getBrand(), "Brand extraction for Apple");
        assertTrue(compMac.getLowestPrice() > 800, "MacBook price estimate should be reasonable");

        ProductComparison compShoe = svc.compareByQuery("Nike Air Zoom Sneaker");
        assertEquals("Nike", compShoe.getBrand(), "Brand extraction for Nike");
    }

    // --- Sub-phase 2.2 Tests ---
    private static void testSubphase22(MovieService svc) {
        MovieDetails movie = svc.getMovieDetailsByTitle("Inception");
        assertTrue(movie.getDownloadLinks().size() >= 3, "At least 3 verified download mirrors");
        assertTrue(movie.getStreamingPlatforms().size() >= 4, "At least 4 streaming platforms");

        boolean hasArchive = movie.getDownloadLinks().stream().anyMatch(d -> d.toMap().get("sourceName").toString().contains("Internet Archive"));
        assertTrue(hasArchive, "Should include Internet Archive download link");

        boolean hasNetflix = movie.getStreamingPlatforms().stream().anyMatch(s -> s.toMap().get("platformName").toString().contains("Netflix"));
        assertTrue(hasNetflix, "Should include Netflix streaming link");
    }

    private static void testSubphase22Metadata(MovieService svc) {
        MovieDetails m1 = svc.getMovieDetailsByTitle("Interstellar (2014)");
        assertEquals(2014, m1.getYear(), "Year extraction for Interstellar");
        assertEquals("Christopher Nolan", m1.getDirector(), "Director for Interstellar");

        MovieDetails m2 = svc.getMovieDetailsByTitle("The Matrix (1999)");
        assertEquals(1999, m2.getYear(), "Year extraction for Matrix");
        assertEquals("Lana & Lilly Wachowski", m2.getDirector(), "Director for Matrix");
    }

    // --- Sub-phase 2.3 Tests ---
    private static void testSubphase23(RoutingService svc) {
        RouteResult route = svc.calculateShortestRoute("Notre Dame, Paris", "Eiffel Tower, Paris");
        assertTrue(route.getDistanceKm() > 0, "Distance must be > 0");
        assertTrue(route.getDurationMinutes() > 0, "Duration must be > 0");
        assertTrue(!route.getSteps().isEmpty(), "Steps list must not be empty");
        assertTrue(!route.getPathCoordinates().isEmpty(), "Path coordinates must not be empty");
        assertTrue(route.getNavigationUrl().contains("maps/dir/"), "Google maps link must be valid");
    }

    private static void testSubphase23Geocoding(RoutingService svc) {
        double[] tokyo = svc.geocode("Tokyo Tower");
        assertTrue(tokyo[0] > 35.0 && tokyo[0] < 36.0, "Tokyo lat range");
        assertTrue(tokyo[1] > 139.0 && tokyo[1] < 140.0, "Tokyo lon range");

        // Verify caching: second call returns same instance from cache
        double[] cached = svc.geocode("Tokyo Tower");
        assertEquals(tokyo[0], cached[0], "Cached lat must match");
    }

    // --- Sub-phase 2.4 Tests ---
    private static void testSubphase24(FoodService svc) {
        FoodReview review = svc.getFoodReviewByName("Neapolitan Pizza Margherita");
        assertEquals(100, review.getPositivePercent() + review.getNeutralPercent() + review.getNegativePercent(), "Sentiment sum to 100");
        assertTrue(review.getPositivePercent() >= 70, "High rating should have >= 70% positive");
        assertTrue(!review.getSampleReviews().isEmpty(), "Sample reviews must exist");
    }

    private static void testSubphase24Cuisines(FoodService svc) {
        FoodReview pizza = svc.getFoodReviewByName("Artisan Italian Pizza");
        assertTrue(pizza.getBestRecommendedDishes().stream().anyMatch(d -> d.contains("Margherita")), "Pizza dishes check");

        FoodReview sushi = svc.getFoodReviewByName("Bluefin Omakase Sushi");
        assertTrue(sushi.getBestRecommendedDishes().stream().anyMatch(d -> d.contains("Nigiri")), "Sushi dishes check");

        FoodReview biryani = svc.getFoodReviewByName("Hyderabadi Dum Biryani");
        assertTrue(biryani.getBestRecommendedDishes().stream().anyMatch(d -> d.contains("Biryani")), "Biryani dishes check");
    }

    // --- Sub-phase 2.5 Tests ---
    private static void testSubphase25Intent(InternetSearchService svc) {
        assertEquals("PRODUCT", svc.detectCategory("Buy wireless gaming mouse"), "Product intent");
        assertEquals("MOVIE", svc.detectCategory("Watch Interstellar 4K download"), "Movie intent");
        assertEquals("PLACE", svc.detectCategory("Shortest route to Eiffel Tower"), "Place intent");
        assertEquals("FOOD", svc.detectCategory("Best Italian Pizza restaurant reviews"), "Food intent");
        assertEquals("WEB", svc.detectCategory("Quantum entanglement theory"), "General web intent");
    }

    private static void testSubphase25Synthesis(InternetSearchService svc) {
        List<Item> productResults = svc.searchInternet("Mechanical Keyboard", "Paris");
        assertTrue(!productResults.isEmpty(), "Product results should synthesize");
        Item item = productResults.get(0);
        assertEquals("PRODUCT", item.getCategory(), "Synthesized category match");
        assertTrue(item.getMetadata().containsKey("comparison"), "Comparison metadata attached");

        List<Item> placeResults = svc.searchInternet("Tokyo Tower", "Paris");
        assertTrue(!placeResults.isEmpty(), "Place results should synthesize");
        Item placeItem = placeResults.get(0);
        assertEquals("PLACE", placeItem.getCategory(), "Synthesized place match");
        assertTrue(placeItem.getMetadata().containsKey("route"), "Route metadata attached");
    }
}
