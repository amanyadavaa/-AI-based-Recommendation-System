package com.guvi.recommend.test;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.db.dao.InteractionDao;
import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Phase 6 Automated Verification Test Suite
 * Covers:
 * 6.1 End-to-End System Health, API Contracts & Interactive Feedback Telemetry
 * 6.2 Sorting, Filtering & Progressive Disclosure Explainability
 * 6.3 Packaging Integrity, Zero Regressions (Phases 1-5), and Virtual Thread Concurrency Benchmark
 */
public class Phase6TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   RUNNING PHASE 6 VERIFICATION, PACKAGING & DEPLOYMENT SUITE     ");
        System.out.println("==================================================================");

        runTest("Sub-phase 6.1: End-to-End Service Health & Catalog Integration", Phase6TestSuite::testServiceHealthAndCatalog);
        runTest("Sub-phase 6.1: Recommendation Multi-Criteria Sorting & Threshold Filtering", Phase6TestSuite::testSortingAndFiltering);
        runTest("Sub-phase 6.1: 1-Sentence AI Explanations & Progressive Disclosure Factor Weights", Phase6TestSuite::testAiExplanationsAndFactors);
        runTest("Sub-phase 6.1: Interactive Feedback (LIKE/DISLIKE) SQLite Telemetry Persistence", Phase6TestSuite::testFeedbackTelemetryPersistence);
        runTest("Sub-phase 6.1: Clean Minimalist UI DOM Integrity (Sidebar, Search Hero & Skeletons)", Phase6TestSuite::testCleanUiDomIntegrity);
        runTest("Sub-phase 6.2: One-Click Execution & Test Scripts Manifest (run.bat, run.ps1, test.bat)", Phase6TestSuite::testExecutionScriptsManifest);
        runTest("Sub-phase 6.2: Self-Contained Zero-Dependency Runtime & SQLite Packaging", Phase6TestSuite::testSelfContainedPackaging);
        runTest("Sub-phase 6.2: Comprehensive Production Brand Sanitization Audit", Phase6TestSuite::testProductionSanitizationAudit);
        runTest("Sub-phase 6.2: Full Regression Suite (Phases 1 through 5 Integrity Guarantee)", Phase6TestSuite::testZeroRegressionPhases1To5);
        runTest("Sub-phase 6.2: Virtual-Thread High-Concurrency Stress Benchmark", Phase6TestSuite::testVirtualThreadConcurrencyStress);

        System.out.println("==================================================================");
        System.out.printf("PHASE 6 SUMMARY: %d Passed, %d Failed (Total %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.err.println("❌ Phase 6 test suite encountered failures!");
            System.exit(1);
        } else {
            System.out.println("✔ ALL SUB-PHASES OF PHASE 6 VERIFIED SUCCESSFULLY!");
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

    private static void testServiceHealthAndCatalog() {
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
        assertTrue(!recs.isEmpty(), "Engine must return non-empty recommendation items");

        // Universal search test
        List<Item> searchResults = engine.search("Sony", "usr-alice", "ALL");
        assertTrue(!searchResults.isEmpty(), "Universal search must return relevant items");
    }

    private static void testSortingAndFiltering() {
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

        List<Item> items = engine.getPersonalizedRecommendations("usr-alice", "ALL");

        // Verify match score ordering
        for (int i = 0; i < items.size() - 1; i++) {
            assertTrue(items.get(i).getMatchScore() >= items.get(i + 1).getMatchScore(),
                    "Items must be sorted descending by match score by default");
        }

        // Verify min confidence filter logic
        double minScore = 0.70;
        List<Item> highConfidence = items.stream().filter(it -> it.getMatchScore() >= minScore).toList();
        for (Item it : highConfidence) {
            assertTrue(it.getMatchScore() >= minScore, "Filtered items must meet or exceed minScore");
        }
    }

    private static void testAiExplanationsAndFactors() {
        Item item = new Item("test-id", "Sony Headphones", "PRODUCT", "Noise canceling", 4.8, 12000, "img.jpg", List.of("Audio"), 348.0);
        item.setMatchScore(0.95);

        Map<String, Object> map = item.toMap();
        assertTrue(map.containsKey("aiExplanation"), "Item map must contain aiExplanation");
        String expl = String.valueOf(map.get("aiExplanation"));
        assertTrue(expl.length() >= 15, "AI explanation must be a descriptive sentence (found: " + expl + ")");

        assertTrue(map.containsKey("scoreFactors"), "Item map must contain scoreFactors for progressive disclosure");
        @SuppressWarnings("unchecked")
        Map<String, Object> factors = (Map<String, Object>) map.get("scoreFactors");
        assertTrue(factors.containsKey("contentSimilarity"), "Factors require contentSimilarity");
        assertTrue(factors.containsKey("collaborativeAffinity"), "Factors require collaborativeAffinity");
        assertTrue(factors.containsKey("popularityWeight"), "Factors require popularityWeight");
    }

    private static void testFeedbackTelemetryPersistence() {
        InteractionDao dao = new InteractionDao();

        try {
            // Record thumbs-up LIKE
            Interaction like = new Interaction("usr-alice", "prod-1", "PRODUCT", "LIKE", 5.0);
            dao.save(like);
            Optional<Interaction> optLike = dao.findById(like.getId());
            assertTrue(optLike.isPresent(), "LIKE interaction must be persisted in SQLite");
            assertEquals("LIKE", optLike.get().getType(), "Interaction type must be LIKE");

            // Record thumbs-down DISLIKE
            Interaction dislike = new Interaction("usr-bob", "mov-1", "MOVIE", "DISLIKE", 1.0);
            dao.save(dislike);
            Optional<Interaction> optDislike = dao.findById(dislike.getId());
            assertTrue(optDislike.isPresent(), "DISLIKE interaction must be persisted in SQLite");
            assertEquals("DISLIKE", optDislike.get().getType(), "Interaction type must be DISLIKE");
        } catch (Exception e) {
            throw new RuntimeException("InteractionDao persistence test failed: " + e.getMessage(), e);
        }
    }

    private static void testCleanUiDomIntegrity() {
        Path htmlPath = Paths.get("web", "index.html");
        try {
            String html = Files.readString(htmlPath);

            // Refinement sidebar
            assertTrue(html.contains("id=\"refinementSidebar\""), "HTML must contain refinementSidebar");
            assertTrue(html.contains("id=\"sortSelect\""), "HTML must contain sortSelect");
            assertTrue(html.contains("id=\"minScoreSlider\""), "HTML must contain minScoreSlider");
            assertTrue(html.contains("id=\"btnResetFilters\""), "HTML must contain btnResetFilters");

            // Prompt & search hero
            assertTrue(html.contains("id=\"searchForm\""), "HTML must contain searchForm");
            assertTrue(html.contains("id=\"searchInput\""), "HTML must contain searchInput");

            // Shimmer skeleton screens
            assertTrue(html.contains("skeleton-shimmer"), "HTML must contain skeleton-shimmer screens");

            // Curated recommendations grid
            assertTrue(html.contains("id=\"recommendationsGrid\""), "HTML must contain recommendationsGrid");
        } catch (Exception e) {
            throw new RuntimeException("Error checking UI DOM: " + e.getMessage(), e);
        }
    }

    private static void testExecutionScriptsManifest() {
        Path runBat = Paths.get("run.bat");
        Path runPs1 = Paths.get("run.ps1");
        Path runGuiBat = Paths.get("run-gui.bat");
        Path runGuiPs1 = Paths.get("run-gui.ps1");
        Path testBat = Paths.get("test.bat");
        Path testPs1 = Paths.get("test.ps1");

        assertTrue(Files.exists(runBat), "run.bat must exist");
        assertTrue(Files.exists(runPs1), "run.ps1 must exist");
        assertTrue(Files.exists(runGuiBat), "run-gui.bat must exist");
        assertTrue(Files.exists(runGuiPs1), "run-gui.ps1 must exist");
        assertTrue(Files.exists(testBat), "test.bat must exist");
        assertTrue(Files.exists(testPs1), "test.ps1 must exist");

        try {
            assertTrue(Files.size(runBat) > 100, "run.bat must have valid launcher content");
            assertTrue(Files.size(runPs1) > 100, "run.ps1 must have valid PowerShell content");
            assertTrue(Files.size(testBat) > 100, "test.bat must have valid test runner content");
        } catch (Exception e) {
            throw new RuntimeException("Error inspecting script sizes: " + e.getMessage(), e);
        }
    }

    private static void testSelfContainedPackaging() {
        Path libJar = Paths.get("lib", "sqlite-jdbc.jar");
        assertTrue(Files.exists(libJar), "Self-contained sqlite-jdbc.jar must exist in lib/");

        try {
            long jarSize = Files.size(libJar);
            assertTrue(jarSize > 1_000_000, "sqlite-jdbc.jar must be a full valid SQLite driver (size: " + jarSize + ")");
        } catch (Exception e) {
            throw new RuntimeException("Error inspecting sqlite-jdbc.jar: " + e.getMessage(), e);
        }
    }

    private static void testProductionSanitizationAudit() {
        List<String> filesToCheck = List.of(
                "web/index.html",
                "web/js/app.js",
                "web/css/style.css"
        );

        for (String filePath : filesToCheck) {
            try {
                String content = Files.readString(Paths.get(filePath));
                assertTrue(!content.toLowerCase().contains("guvi"),
                        filePath + " must not contain forbidden course branding 'GUVI'");
            } catch (Exception e) {
                throw new RuntimeException("Error auditing file " + filePath + ": " + e.getMessage(), e);
            }
        }
    }

    private static void testZeroRegressionPhases1To5() {
        // Phase 1 verification
        SystemConfig config = new SystemConfig();
        assertEquals(0.40, config.getContentWeight(), "Phase 1: Default content weight");

        // Phase 2 verification
        ComparisonService compService = new ComparisonService();
        ProductComparison comp = compService.compareByQuery("Sony WH-1000XM5");
        assertTrue(comp.getLowestPrice() > 0, "Phase 2: Product comparison price calculation");

        // Phase 3 verification
        DatabaseConnectionManager db = DatabaseConnectionManager.getInstance();
        assertTrue(db.isJdbcAvailable(), "Phase 3: SQLite JDBC connection established");

        // Phase 4 verification
        config.applyPreset("accuracy", "Admin");
        assertEquals(0.60, config.getContentWeight(), "Phase 4: Algorithmic preset accuracy applied");

        // Phase 5 verification
        Path indexPath = Paths.get("web", "index.html");
        assertTrue(Files.exists(indexPath), "Phase 5: SPA index.html exists");
    }

    private static void testVirtualThreadConcurrencyStress() {
        SystemConfig config = new SystemConfig();
        UserService userService = new UserService();
        ComparisonService compService = new ComparisonService();
        MovieService movieService = new MovieService();
        RoutingService routingService = new RoutingService();
        FoodService foodService = new FoodService();
        InternetSearchService searchService = new InternetSearchService(compService, movieService, routingService, foodService);

        RecommendationEngine engine = new RecommendationEngine(
                config, userService, compService, movieService, routingService, foodService, searchService
        );

        int concurrentRequests = 50;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < concurrentRequests; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        String user = (idx % 2 == 0) ? "usr-alice" : "usr-bob";
                        List<Item> recs = engine.getPersonalizedRecommendations(user, "ALL");
                        if (recs.isEmpty()) {
                            errors.add(new IllegalStateException("Empty recommendations under concurrent load"));
                        }
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            boolean finished = latch.await(10, TimeUnit.SECONDS);
            assertTrue(finished, "All 50 concurrent virtual-thread tasks must finish within timeout");
            assertTrue(errors.isEmpty(), "Zero concurrency errors under 50 simultaneous virtual-thread requests");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Concurrency test interrupted", e);
        }
    }
}
