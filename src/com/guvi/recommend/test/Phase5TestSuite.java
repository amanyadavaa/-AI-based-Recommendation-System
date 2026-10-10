package com.guvi.recommend.test;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;
import com.guvi.recommend.web.StaticFileHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Phase 5 Automated Verification Test Suite
 * Covers:
 * 5.1 Responsive Web Dashboard Interface & Static Asset Integrity
 * 5.2 Interactive Feature Modals & Contracts (Price Comparison, Movie Downloads, Shortest Path Maps, Food Sentiment)
 * 5.3 Admin Control Panel, Live Preset Synchronization & Security Routing
 */
public class Phase5TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   RUNNING PHASE 5 RESPONSIVE WEB DASHBOARD & UI TEST SUITE       ");
        System.out.println("==================================================================");

        // Sub-phase 5.1: Web Dashboard Interface & Asset Integrity
        runTest("Sub-phase 5.1: Static Web Assets Integrity (index.html, style.css, app.js)", Phase5TestSuite::testStaticAssetsIntegrity);
        runTest("Sub-phase 5.1: Strict Brand Sanitization (Zero Unwanted Course Tags in Web UI)", Phase5TestSuite::testStrictBrandingSanitization);
        runTest("Sub-phase 5.1: Static File Server MIME Types Contract", Phase5TestSuite::testStaticFileServerMimeTypes);
        runTest("Sub-phase 5.1: Path Traversal Security & SPA Fallback Routing", Phase5TestSuite::testStaticFileServerSecurityAndSpaFallback);

        // Sub-phase 5.2: Interactive Feature Modals & Contracts
        runTest("Sub-phase 5.2: Recommendation Cards UI Feed Data Contract", Phase5TestSuite::testRecommendationsCardContract);
        runTest("Sub-phase 5.2: Multi-Store Product Comparison Modal Data Contract", Phase5TestSuite::testProductComparisonModalContract);
        runTest("Sub-phase 5.2: Movie Direct Download Mirrors & Streaming Providers Modal Contract", Phase5TestSuite::testMovieDetailsModalContract);
        runTest("Sub-phase 5.2: Leaflet Geodesic Shortest Path & Step Directions Modal Contract", Phase5TestSuite::testRoutingShortestPathModalContract);
        runTest("Sub-phase 5.2: Food Reviews Tri-Color Sentiment Breakdown Modal Contract", Phase5TestSuite::testFoodSentimentModalContract);

        // Sub-phase 5.3: Admin Control Panel & Live Preset Synchronization
        runTest("Sub-phase 5.3: Admin Presets Dynamic Recalibration & Live Web Toggle Contract", Phase5TestSuite::testAdminPresetSyncAndToggleContract);

        System.out.println("==================================================================");
        System.out.printf("PHASE 5 SUMMARY: %d Passed, %d Failed (Total %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.err.println("❌ Phase 5 test suite encountered failures!");
            System.exit(1);
        } else {
            System.out.println("✔ ALL SUB-PHASES OF PHASE 5 VERIFIED SUCCESSFULLY!");
        }
    }

    private static void runTest(String testName, Runnable testMethod) {
        try {
            testMethod.run();
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] " + testName + " -> " + t.getMessage());
            t.printStackTrace(System.err);
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("Assertion Failed: %s [Expected: %s, Actual: %s]", message, expected, actual));
        }
    }

    // --- TEST IMPLEMENTATIONS ---

    private static void testStaticAssetsIntegrity() {
        Path htmlPath = Paths.get("web", "index.html");
        Path cssPath = Paths.get("web", "css", "style.css");
        Path jsPath = Paths.get("web", "js", "app.js");

        assertTrue(Files.exists(htmlPath), "web/index.html must exist on filesystem");
        assertTrue(Files.exists(cssPath), "web/css/style.css must exist on filesystem");
        assertTrue(Files.exists(jsPath), "web/js/app.js must exist on filesystem");

        try {
            long htmlSize = Files.size(htmlPath);
            long cssSize = Files.size(cssPath);
            long jsSize = Files.size(jsPath);

            assertTrue(htmlSize > 5000, "web/index.html must be substantial HTML SPA (size: " + htmlSize + ")");
            assertTrue(cssSize > 500, "web/css/style.css must have stylesheet rules (size: " + cssSize + ")");
            assertTrue(jsSize > 10000, "web/js/app.js must contain frontend application logic (size: " + jsSize + ")");

            String html = Files.readString(htmlPath);
            assertTrue(html.contains("id=\"searchForm\""), "HTML must contain omnichannel searchForm");
            assertTrue(html.contains("id=\"recommendationsGrid\""), "HTML must contain recommendationsGrid");
            assertTrue(html.contains("id=\"chartCategoryCtr\""), "HTML must contain Chart.js canvas chartCategoryCtr");
            assertTrue(html.contains("id=\"chartTimeline\""), "HTML must contain Chart.js canvas chartTimeline");
            assertTrue(html.contains("id=\"modalProduct\""), "HTML must contain modalProduct");
            assertTrue(html.contains("id=\"modalMovie\""), "HTML must contain modalMovie");
            assertTrue(html.contains("id=\"modalRoute\""), "HTML must contain modalRoute");
            assertTrue(html.contains("id=\"modalFood\""), "HTML must contain modalFood");
            assertTrue(html.contains("id=\"modalPreferences\""), "HTML must contain modalPreferences");
        } catch (Exception e) {
            throw new RuntimeException("Failed reading web assets: " + e.getMessage(), e);
        }
    }

    private static void testStrictBrandingSanitization() {
        try {
            String html = Files.readString(Paths.get("web", "index.html"));
            String js = Files.readString(Paths.get("web", "js", "app.js"));
            String css = Files.readString(Paths.get("web", "css", "style.css"));

            assertTrue(!html.toLowerCase().contains("guvi"), "web/index.html must not contain external branding GUVI");
            assertTrue(!js.toLowerCase().contains("guvi"), "web/js/app.js must not contain external branding GUVI");
            assertTrue(!css.toLowerCase().contains("guvi"), "web/css/style.css must not contain external branding GUVI");
        } catch (Exception e) {
            throw new RuntimeException("Brand sanitization check error: " + e.getMessage(), e);
        }
    }

    private static void testStaticFileServerMimeTypes() {
        StaticFileHandler handler = new StaticFileHandler("web");
        assertEquals("text/html; charset=UTF-8", handler.getMimeType("index.html"), "MIME type for HTML");
        assertEquals("text/html; charset=UTF-8", handler.getMimeType("page.htm"), "MIME type for HTM");
        assertEquals("text/css; charset=UTF-8", handler.getMimeType("style.css"), "MIME type for CSS");
        assertEquals("application/javascript; charset=UTF-8", handler.getMimeType("app.js"), "MIME type for JS");
        assertEquals("application/json; charset=UTF-8", handler.getMimeType("data.json"), "MIME type for JSON");
        assertEquals("image/svg+xml", handler.getMimeType("icon.svg"), "MIME type for SVG");
        assertEquals("image/png", handler.getMimeType("image.png"), "MIME type for PNG");
        assertEquals("image/jpeg", handler.getMimeType("banner.jpg"), "MIME type for JPG");
        assertEquals("image/x-icon", handler.getMimeType("favicon.ico"), "MIME type for ICO");
        assertEquals("application/octet-stream", handler.getMimeType("archive.zip"), "Default octet-stream for unknown");
    }

    private static void testStaticFileServerSecurityAndSpaFallback() {
        StaticFileHandler handler = new StaticFileHandler("web");

        // 1. Root path resolves to index.html
        Path rootTarget = handler.resolveTarget("/");
        assertTrue(rootTarget.endsWith("index.html"), "Root path / must resolve to index.html");

        // 2. Directory traversal attempt must resolve safely to index.html within root
        Path traversalTarget = handler.resolveTarget("/../../src/Main.java");
        assertTrue(traversalTarget.endsWith("index.html"), "Directory traversal must safely fall back to index.html");

        // 3. Unknown deep client-side SPA route falls back to index.html
        Path spaTarget = handler.resolveTarget("/explore/sci-fi-movies");
        assertTrue(spaTarget.endsWith("index.html"), "SPA route must resolve to index.html");

        // 4. Existing CSS and JS assets resolve directly
        Path cssTarget = handler.resolveTarget("/css/style.css");
        assertTrue(cssTarget.endsWith("style.css") && Files.exists(cssTarget), "Existing CSS asset must resolve directly");

        Path jsTarget = handler.resolveTarget("/js/app.js");
        assertTrue(jsTarget.endsWith("app.js") && Files.exists(jsTarget), "Existing JS asset must resolve directly");
    }

    private static void testRecommendationsCardContract() {
        SystemConfig config = new SystemConfig();
        UserService userService = new UserService();
        ComparisonService comparisonService = new ComparisonService();
        MovieService movieService = new MovieService();
        RoutingService routingService = new RoutingService();
        FoodService foodService = new FoodService();
        InternetSearchService searchService = new InternetSearchService(comparisonService, movieService, routingService, foodService);

        RecommendationEngine engine = new RecommendationEngine(
                config, userService, comparisonService, movieService, routingService, foodService, searchService
        );

        List<Item> recs = engine.getPersonalizedRecommendations("usr-alice", "ALL");
        assertTrue(!recs.isEmpty(), "Engine must return non-empty recommendations for user");

        for (Item item : recs) {
            Map<String, Object> map = item.toMap();
            assertTrue(map.containsKey("id"), "Card requires item ID");
            assertTrue(map.containsKey("title"), "Card requires item title");
            assertTrue(map.containsKey("category"), "Card requires item category");
            assertTrue(map.containsKey("matchScore"), "Card requires AI matchScore");
            assertTrue(map.containsKey("imageUrl"), "Card requires image thumbnail URL");
            assertTrue(map.containsKey("rating"), "Card requires star rating");

            double score = ((Number) map.get("matchScore")).doubleValue();
            assertTrue(score >= 0.0 && score <= 1.0, "matchScore must be normalized between 0.0 and 1.0");
        }
    }

    private static void testProductComparisonModalContract() {
        ComparisonService compService = new ComparisonService();
        ProductComparison comp = compService.compareByQuery("Sony WH-1000XM5");

        Map<String, Object> map = comp.toMap();
        assertTrue(map.containsKey("productName"), "Comparison requires product title");
        assertTrue(map.containsKey("lowestPrice"), "Comparison requires lowest price");
        assertTrue(map.containsKey("highestPrice"), "Comparison requires highest price");
        assertTrue(map.containsKey("averagePrice"), "Comparison requires average price");
        assertTrue(map.containsKey("maxSavings"), "Comparison requires max savings value");
        assertTrue(map.containsKey("storeListings"), "Comparison requires store listings array");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> listings = (List<Map<String, Object>>) map.get("storeListings");
        assertTrue(listings.size() >= 4, "Must compare across multiple verified internet retailers");

        for (Map<String, Object> listing : listings) {
            assertTrue(listing.containsKey("storeName"), "Listing requires retailer name");
            assertTrue(listing.containsKey("price"), "Listing requires price");
            assertTrue(listing.containsKey("inStock"), "Listing requires stock status");
            assertTrue(listing.containsKey("directUrl"), "Listing requires verified outbound URL");
        }
    }

    private static void testMovieDetailsModalContract() {
        MovieService movieService = new MovieService();
        MovieDetails movie = movieService.getMovieDetailsByTitle("Interstellar");

        Map<String, Object> map = movie.toMap();
        assertTrue(map.containsKey("title"), "Movie details requires title");
        assertTrue(map.containsKey("year"), "Movie details requires release year");
        assertTrue(map.containsKey("genre"), "Movie details requires genre");
        assertTrue(map.containsKey("imdbRating"), "Movie details requires IMDb rating");
        assertTrue(map.containsKey("downloadLinks"), "Movie details requires downloadLinks");
        assertTrue(map.containsKey("streamingPlatforms"), "Movie details requires streamingPlatforms");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> downloads = (List<Map<String, Object>>) map.get("downloadLinks");
        assertTrue(downloads.size() >= 3, "Must provide verified direct download links and mirrors");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> streams = (List<Map<String, Object>>) map.get("streamingPlatforms");
        assertTrue(streams.size() >= 2, "Must provide official streaming platforms");
    }

    private static void testRoutingShortestPathModalContract() {
        RoutingService routingService = new RoutingService();
        RouteResult route = routingService.calculateShortestRoute("Eiffel Tower, Paris", "Louvre Museum, Paris");

        Map<String, Object> map = route.toMap();
        assertTrue(map.containsKey("originName"), "Route requires origin place");
        assertTrue(map.containsKey("destinationName"), "Route requires destination place");
        assertTrue(map.containsKey("distanceKm"), "Route requires distance in km");
        assertTrue(map.containsKey("formattedDuration"), "Route requires formatted duration");
        assertTrue(map.containsKey("pathCoordinates"), "Route requires coordinates for Leaflet map polylines");
        assertTrue(map.containsKey("steps"), "Route requires step-by-step turn directions");

        double dist = ((Number) map.get("distanceKm")).doubleValue();
        assertTrue(dist > 0.0, "Shortest driving distance must be strictly greater than 0");

        @SuppressWarnings("unchecked")
        List<Object> steps = (List<Object>) map.get("steps");
        assertTrue(!steps.isEmpty(), "Turn-by-turn driving steps must not be empty");
    }

    private static void testFoodSentimentModalContract() {
        FoodService foodService = new FoodService();
        FoodReview review = foodService.getFoodReviewByName("Neapolitan Pizza Margherita");

        Map<String, Object> map = review.toMap();
        assertTrue(map.containsKey("name"), "Food review requires restaurant name");
        assertTrue(map.containsKey("positivePercent"), "Requires positive sentiment percent");
        assertTrue(map.containsKey("neutralPercent"), "Requires neutral sentiment percent");
        assertTrue(map.containsKey("negativePercent"), "Requires negative sentiment percent");
        assertTrue(map.containsKey("bestRecommendedDishes"), "Requires signature recommended dishes");

        int pos = ((Number) map.get("positivePercent")).intValue();
        int neu = ((Number) map.get("neutralPercent")).intValue();
        int neg = ((Number) map.get("negativePercent")).intValue();
        assertEquals(100, pos + neu + neg, "Sentiment breakdown percentages must sum to 100%");
    }

    private static void testAdminPresetSyncAndToggleContract() {
        SystemConfig config = new SystemConfig();

        // 1. Test Discovery Preset
        boolean discOk = config.applyPreset("discovery", "Admin");
        assertTrue(discOk, "Applying discovery preset must succeed");
        assertTrue(config.getPopularityWeight() >= 0.20, "Discovery preset elevates trend discovery");
        assertTrue(config.getExplorationRate() >= 0.25, "Discovery preset elevates exploration epsilon");

        // 2. Test Diversity Preset
        boolean divOk = config.applyPreset("diversity", "Admin");
        assertTrue(divOk, "Applying diversity preset must succeed");
        assertTrue(config.getContentWeight() + config.getCollaborativeWeight() + config.getPopularityWeight() >= 0.99,
                "Weights must remain normalized to 1.00");

        // 3. Test Balanced Preset
        boolean balOk = config.applyPreset("balanced", "Admin");
        assertTrue(balOk, "Applying balanced preset must succeed");
        assertEquals(0.40, config.getContentWeight(), "Balanced default content weight");
        assertEquals(0.35, config.getCollaborativeWeight(), "Balanced default collaborative weight");
        assertEquals(0.25, config.getPopularityWeight(), "Balanced default popularity weight");

        // 4. Test Live Web Toggle
        config.setEnableLiveWebSearch(false);
        assertTrue(!config.isEnableLiveWebSearch(), "Live web search toggle can be disabled");
        config.setEnableLiveWebSearch(true);
        assertTrue(config.isEnableLiveWebSearch(), "Live web search toggle can be re-enabled");
    }
}
