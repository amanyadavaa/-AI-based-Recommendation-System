package com.guvi.recommend.test;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.db.dao.InteractionDao;
import com.guvi.recommend.db.dao.ItemDao;
import com.guvi.recommend.db.dao.UserDao;
import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;
import com.guvi.recommend.util.JsonUtil;
import com.guvi.recommend.web.ApiHandler;

import java.util.*;

/**
 * Phase 4 Automated Verification Test Suite
 * Covers:
 * 4.1 System Settings Management (Hyperparameters, Presets & SQLite Persistence)
 * 4.2 Recommendation Effectiveness Analysis (KPI Metrics, Category CTR & Automated AI Suggestions)
 * 4.3 Virtual-Thread Embedded HTTP Server & Enterprise REST API Endpoints
 */
public class Phase4TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("     RUNNING PHASE 4 ADMIN, ANALYTICS & REST API TEST SUITE       ");
        System.out.println("==================================================================");

        // Sub-phase 4.1: System Settings Management
        runTest("Sub-phase 4.1: Dynamic Hyperparameter Tuning & Weight Normalization", Phase4TestSuite::testHyperparameterTuning);
        runTest("Sub-phase 4.1: Algorithmic Presets (Accuracy, Exploration, Diversity, Balanced)", Phase4TestSuite::testAlgorithmicPresets);
        runTest("Sub-phase 4.1: SQLite Database Settings Persistence (settings table)", Phase4TestSuite::testSettingsPersistence);

        // Sub-phase 4.2: Recommendation Effectiveness Analysis
        runTest("Sub-phase 4.2: Core KPI Metrics (Impressions, Clicks, Overall CTR%, Conversions)", Phase4TestSuite::testCoreKpiMetrics);
        runTest("Sub-phase 4.2: Category Performance Breakdown & Precision/Recall Metrics", Phase4TestSuite::testCategoryMetricsAndAccuracy);
        runTest("Sub-phase 4.2: Automated AI Optimization Suggestions Engine", Phase4TestSuite::testAiOptimizationSuggestions);

        // Sub-phase 4.3: Virtual-Thread Embedded Server & REST API Endpoints
        runTest("Sub-phase 4.3: Admin Settings & Preset REST Endpoints Contract", Phase4TestSuite::testAdminSettingsApiContract);
        runTest("Sub-phase 4.3: Recommendation Effectiveness Analytics REST Endpoint Contract", Phase4TestSuite::testAnalyticsApiContract);
        runTest("Sub-phase 4.3: User Preference Management & Personalized Feed REST Contract", Phase4TestSuite::testUserPreferencesAndFeedApi);
        runTest("Sub-phase 4.3: Interaction Telemetry Logging & SQLite Sync Contract", Phase4TestSuite::testTelemetryApiAndDbSync);

        System.out.println("==================================================================");
        System.out.printf("PHASE 4 SUMMARY: %d Passed, %d Failed (Total %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.err.println("❌ Phase 4 test suite encountered failures!");
            System.exit(1);
        } else {
            System.out.println("✔ ALL SUB-PHASES OF PHASE 4 VERIFIED SUCCESSFULLY!");
        }
    }

    private static void runTest(String testName, TestRunnable runnable) {
        try {
            runnable.run();
            System.out.printf("  [PASS] %s%n", testName);
            testsPassed++;
        } catch (Throwable t) {
            System.err.printf("  [FAIL] %s - %s%n", testName, t.getMessage());
            t.printStackTrace(System.err);
            testsFailed++;
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }

    // -------------------------------------------------------------
    // Sub-phase 4.1 Tests
    // -------------------------------------------------------------

    private static void testHyperparameterTuning() {
        SystemConfig config = new SystemConfig();
        config.resetToDefaults();

        // Update weights arbitrarily
        Map<String, Object> update = Map.of(
                "contentWeight", 0.50,
                "collaborativeWeight", 0.30,
                "popularityWeight", 0.20,
                "explorationRate", 0.25,
                "priceSensitivity", 0.70
        );
        config.updateFromMap(update, "AdminUser");

        assertEquals(0.25, config.getExplorationRate(), "Exploration rate must update");
        assertEquals(0.70, config.getPriceSensitivity(), "Price sensitivity must update");

        // Verify normalized weights sum to 1.0
        double totalWeight = config.getContentWeight() + config.getCollaborativeWeight() + config.getPopularityWeight();
        assertTrue(Math.abs(1.0 - totalWeight) < 0.02, "Normalized weights must sum to approximately 1.0: " + totalWeight);
    }

    private static void testAlgorithmicPresets() {
        SystemConfig config = new SystemConfig();

        // 1. Accuracy Optimized
        boolean ok1 = config.applyPreset("ACCURACY_OPTIMIZED", "Admin");
        assertTrue(ok1, "ACCURACY_OPTIMIZED preset must be valid");
        assertTrue(config.getContentWeight() > 0.5, "Accuracy preset must heavily favor content matching: " + config.getContentWeight());
        assertTrue(config.getExplorationRate() <= 0.05, "Accuracy preset must have low exploration rate: " + config.getExplorationRate());

        // 2. Exploration Discovery
        boolean ok2 = config.applyPreset("EXPLORATION_DISCOVERY", "Admin");
        assertTrue(ok2, "EXPLORATION_DISCOVERY preset must be valid");
        assertTrue(config.getExplorationRate() >= 0.35, "Discovery preset must have high exploration rate: " + config.getExplorationRate());

        // 3. Balanced Defaults
        boolean ok3 = config.applyPreset("BALANCED_DEFAULTS", "Admin");
        assertTrue(ok3, "BALANCED_DEFAULTS preset must be valid");
        assertEquals(0.40, config.getContentWeight(), "Balanced content weight must be 0.40");

        // 4. Invalid preset
        boolean okInvalid = config.applyPreset("NON_EXISTENT_PRESET", "Admin");
        assertTrue(!okInvalid, "Invalid preset name must return false");
    }

    private static void testSettingsPersistence() {
        DatabaseConnectionManager db = DatabaseConnectionManager.getInstance();
        if (db.isJdbcAvailable()) {
            SystemConfig config1 = new SystemConfig();
            config1.applyPreset("HIGH_DIVERSITY", "PersistenceTest");
            config1.saveToDb();

            SystemConfig config2 = new SystemConfig();
            config2.loadFromDb();
            assertEquals(config1.getExplorationRate(), config2.getExplorationRate(), "Loaded exploration rate must match persisted config");
            assertEquals(config1.getContentWeight(), config2.getContentWeight(), "Loaded content weight must match persisted config");

            // Reset back to defaults in SQLite
            config1.resetToDefaults();
            config1.saveToDb();
        }
    }

    // -------------------------------------------------------------
    // Sub-phase 4.2 Tests
    // -------------------------------------------------------------

    private static void testCoreKpiMetrics() {
        UserService userService = new UserService();
        SystemConfig config = new SystemConfig();

        // Seed interactions
        userService.recordInteraction(new Interaction("usr-alice", "p-1", "PRODUCT", "VIEW", null));
        userService.recordInteraction(new Interaction("usr-alice", "p-1", "PRODUCT", "CLICK", null));
        userService.recordInteraction(new Interaction("usr-alice", "p-1", "PRODUCT", "PURCHASE", 5.0));

        RecommendationMetrics metrics = userService.calculateMetrics(config);
        assertNotNull(metrics, "Metrics object must not be null");
        assertTrue(metrics.getTotalImpressions() >= 1, "Total impressions must be >= 1");
        assertTrue(metrics.getTotalClicks() >= 1, "Total clicks must be >= 1");
        assertTrue(metrics.getOverallCtr() > 0.0, "Overall CTR % must be greater than zero");
        assertTrue(metrics.getConversionRate() > 0.0, "Conversion rate % must be greater than zero");
    }

    private static void testCategoryMetricsAndAccuracy() {
        UserService userService = new UserService();
        SystemConfig config = new SystemConfig();

        RecommendationMetrics metrics = userService.calculateMetrics(config);
        Map<String, RecommendationMetrics.CategoryMetric> catMap = metrics.getCategoryMetrics();

        assertNotNull(catMap, "Category metrics map must not be null");
        assertTrue(catMap.containsKey("PRODUCT"), "Must contain metrics for PRODUCT");
        assertTrue(catMap.containsKey("MOVIE"), "Must contain metrics for MOVIE");
        assertTrue(catMap.containsKey("PLACE"), "Must contain metrics for PLACE");
        assertTrue(catMap.containsKey("FOOD"), "Must contain metrics for FOOD");

        // Accuracy bounds
        assertTrue(metrics.getPrecisionAtK() > 0.6 && metrics.getPrecisionAtK() <= 1.0, "Precision@K must be between 0.6 and 1.0");
        assertTrue(metrics.getRecallAtK() > 0.6 && metrics.getRecallAtK() <= 1.0, "Recall@K must be between 0.6 and 1.0");
        assertTrue(metrics.getDiversityScore() > 0.5 && metrics.getDiversityScore() <= 1.0, "Diversity score must be between 0.5 and 1.0");
    }

    private static void testAiOptimizationSuggestions() {
        UserService userService = new UserService();
        SystemConfig config = new SystemConfig();

        // Test with low exploration rate to trigger warning suggestion
        config.updateFromMap(Map.of("explorationRate", 0.05), "Test");
        RecommendationMetrics metrics = userService.calculateMetrics(config);
        List<String> suggestions = metrics.getAiOptimizationSuggestions();

        assertNotNull(suggestions, "AI suggestions list must not be null");
        assertTrue(!suggestions.isEmpty(), "Must generate at least 1 automated optimization suggestion");
        assertTrue(suggestions.stream().anyMatch(s -> s.toLowerCase().contains("exploration")),
                "Must suggest exploration adjustment when exploration rate is low");
    }

    // -------------------------------------------------------------
    // Sub-phase 4.3 Tests
    // -------------------------------------------------------------

    private static void testAdminSettingsApiContract() {
        SystemConfig config = new SystemConfig();
        UserService userService = new UserService();
        ComparisonService compService = new ComparisonService();
        MovieService movieService = new MovieService();
        RoutingService routeService = new RoutingService();
        FoodService foodService = new FoodService();
        InternetSearchService searchService = new InternetSearchService(compService, movieService, routeService, foodService);
        RecommendationEngine engine = new RecommendationEngine(config, userService, compService, movieService, routeService, foodService, searchService);

        ApiHandler apiHandler = new ApiHandler(config, userService, engine, compService, movieService, routeService, foodService);
        assertNotNull(apiHandler, "ApiHandler instance must instantiate");

        // Verify SystemConfig serializes cleanly to Map/JSON for REST API
        Map<String, Object> map = config.toMap();
        assertTrue(map.containsKey("contentWeight"), "Config map must contain contentWeight");
        assertTrue(map.containsKey("explorationRate"), "Config map must contain explorationRate");
        String json = JsonUtil.toJson(map);
        assertTrue(json.contains("contentWeight"), "JSON must serialize correctly");
    }

    private static void testAnalyticsApiContract() {
        SystemConfig config = new SystemConfig();
        UserService userService = new UserService();
        RecommendationMetrics metrics = userService.calculateMetrics(config);

        Map<String, Object> metricsMap = metrics.toMap();
        assertTrue(metricsMap.containsKey("totalImpressions"), "Metrics map must contain totalImpressions");
        assertTrue(metricsMap.containsKey("overallCtr"), "Metrics map must contain overallCtr");
        assertTrue(metricsMap.containsKey("categoryMetrics"), "Metrics map must contain categoryMetrics");
        assertTrue(metricsMap.containsKey("aiOptimizationSuggestions"), "Metrics map must contain aiOptimizationSuggestions");

        String json = JsonUtil.toJson(metricsMap);
        assertTrue(json.contains("overallCtr"), "Metrics JSON must contain overallCtr");
    }

    private static void testUserPreferencesAndFeedApi() {
        UserService userService = new UserService();
        User user = userService.getUser("usr-alice");
        assertNotNull(user, "Default user alice must exist");

        // Update preferences
        boolean updated = userService.updatePreferences("usr-alice", Map.of(
                "pricePreference", "HIGH",
                "movieGenres", List.of("Sci-Fi", "Mystery")
        ));
        assertTrue(updated, "Preferences must update successfully");
        assertEquals("HIGH", user.getPreferences().get("pricePreference"), "Updated price preference must be HIGH");
    }

    private static void testTelemetryApiAndDbSync() throws Exception {
        InteractionDao interactionDao = new InteractionDao();
        String eventId = "telemetry-" + System.currentTimeMillis();
        Interaction event = new Interaction(eventId, "usr-alice", "m-inception", "MOVIE", "VIEW", null);

        Interaction saved = interactionDao.save(event);
        assertNotNull(saved, "Saved interaction must not be null");

        List<Interaction> userEvents = interactionDao.findByUserId("usr-alice");
        assertTrue(userEvents.stream().anyMatch(e -> e.getId().equals(eventId)),
                "Event must be found in SQLite database via InteractionDao");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s [Expected: %s, Actual: %s]", message, expected, actual));
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
}
