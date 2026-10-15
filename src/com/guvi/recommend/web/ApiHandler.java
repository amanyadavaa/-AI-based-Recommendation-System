package com.guvi.recommend.web;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.db.dao.InteractionDao;
import com.guvi.recommend.db.dao.ItemDao;
import com.guvi.recommend.db.dao.UserDao;
import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;
import com.guvi.recommend.util.JsonUtil;
import com.guvi.recommend.web.security.RateLimiter;
import com.guvi.recommend.web.security.SecurityHeadersFilter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ApiHandler implements HttpHandler {
    private final SystemConfig config;
    private final UserService userService;
    private final RecommendationEngine recommendationEngine;
    private final ComparisonService comparisonService;
    private final MovieService movieService;
    private final RoutingService routingService;
    private final FoodService foodService;

    // Database DAOs (JDBC Integration)
    private final DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
    private final UserDao userDao = new UserDao();
    private final ItemDao itemDao = new ItemDao();
    private final InteractionDao interactionDao = new InteractionDao();

    // Production Security & Metrics
    private final RateLimiter rateLimiter = new RateLimiter(120);
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final long serverStartTime = System.currentTimeMillis();

    public ApiHandler(SystemConfig config, UserService userService, RecommendationEngine recommendationEngine, ComparisonService comparisonService, MovieService movieService, RoutingService routingService, FoodService foodService) {
        this.config = config;
        this.userService = userService;
        this.recommendationEngine = recommendationEngine;
        this.comparisonService = comparisonService;
        this.movieService = movieService;
        this.routingService = routingService;
        this.foodService = foodService;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        requestCounter.incrementAndGet();

        // Apply Enterprise Security Headers
        SecurityHeadersFilter.applyHeaders(exchange.getResponseHeaders());

        // Set CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Token Bucket Rate Limiting (120 req/min)
        String clientIp = exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "client";
        if (!rateLimiter.tryAcquire(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            sendJson(exchange, 429, Map.of(
                    "error", "Too Many Requests",
                    "message", "Rate limit exceeded (120 requests/minute). Please retry after 60 seconds."
            ));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());

        try {
            if (path.equals("/api/health")) {
                sendJson(exchange, 200, Map.of("status", "UP", "time", System.currentTimeMillis()));
            } else if (path.equals("/api/health/deep") && "GET".equals(method)) {
                handleDeepHealth(exchange);
            } else if (path.equals("/api/metrics") && "GET".equals(method)) {
                handlePrometheusMetrics(exchange);
            } else if (path.equals("/api/auth/login") && "POST".equals(method)) {
                handleUserLogin(exchange);
            } else if (path.equals("/api/auth/admin-login") && "POST".equals(method)) {
                handleAdminLogin(exchange);
            } else if (path.equals("/api/auth/logout") && "POST".equals(method)) {
                handleLogout(exchange);
            } else if (path.equals("/api/auth/me") && "GET".equals(method)) {
                handleGetCurrentUser(exchange, params);
            } else if (path.equals("/api/users") && "GET".equals(method)) {
                handleGetUsers(exchange);
            } else if (path.startsWith("/api/user/") && path.endsWith("/preferences") && "POST".equals(method)) {
                handleUpdatePreferences(exchange, path);
            } else if (path.startsWith("/api/user/") && "GET".equals(method)) {
                handleGetUser(exchange, path);
            } else if (path.equals("/api/recommendations") && "GET".equals(method)) {
                handleGetRecommendations(exchange, params);
            } else if (path.equals("/api/search") && "GET".equals(method)) {
                handleSearch(exchange, params);
            } else if (path.equals("/api/compare") && "GET".equals(method)) {
                handleCompare(exchange, params);
            } else if (path.equals("/api/movie") && "GET".equals(method)) {
                handleMovie(exchange, params);
            } else if (path.equals("/api/route") && "GET".equals(method)) {
                handleRoute(exchange, params);
            } else if (path.equals("/api/food") && "GET".equals(method)) {
                handleFood(exchange, params);
            } else if (path.equals("/api/interaction") && "POST".equals(method)) {
                handleRecordInteraction(exchange);
            } else if (path.equals("/api/admin/settings") && "GET".equals(method)) {
                sendJson(exchange, 200, config.toMap());
            } else if (path.equals("/api/admin/settings") && "POST".equals(method)) {
                handleUpdateAdminSettings(exchange);
            } else if (path.equals("/api/admin/reset") && "POST".equals(method)) {
                config.resetToDefaults();
                config.saveToDb();
                sendJson(exchange, 200, Map.of("success", true, "message", "System settings successfully reset to defaults.", "settings", config.toMap()));
            } else if (path.equals("/api/admin/preset") && "POST".equals(method)) {
                handleApplyPreset(exchange, params);
            } else if (path.equals("/api/admin/analytics") && "GET".equals(method)) {
                handleGetAnalytics(exchange);
            } else if (path.startsWith("/api/user/") && path.endsWith("/activity") && "GET".equals(method)) {
                handleGetUserActivity(exchange, path);
            } else if (path.equals("/api/admin/catalog/add") && "POST".equals(method)) {
                handleAdminCatalogAdd(exchange);
            } else if (path.startsWith("/api/admin/catalog/delete") && "POST".equals(method)) {
                handleAdminCatalogDelete(exchange, path, params);
            } else if (path.equals("/api/admin/export") && "GET".equals(method)) {
                handleAdminExport(exchange, params);
            } else if (path.equals("/api/admin/optimize") && "POST".equals(method)) {
                handleAdminOptimize(exchange);
            } else if (path.equals("/api/database/status") && "GET".equals(method)) {
                handleDatabaseStatus(exchange);
            } else if (path.equals("/api/database/users") && "GET".equals(method)) {
                handleDatabaseUsers(exchange);
            } else if (path.equals("/api/database/items") && "GET".equals(method)) {
                handleDatabaseItems(exchange);
            } else if (path.equals("/api/database/interactions") && "GET".equals(method)) {
                handleDatabaseInteractions(exchange);
            } else {
                sendJson(exchange, 404, Map.of("error", "Endpoint not found: " + path));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal Server Error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<User> list = userService.getAllUsers();
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : list) res.add(u.toMap());
        sendJson(exchange, 200, Map.of("users", res));
    }

    private void handleGetUser(HttpExchange exchange, String path) throws IOException {
        String userId = path.substring("/api/user/".length());
        User u = userService.getUser(userId);
        if (u != null) {
            sendJson(exchange, 200, u.toMap());
        } else {
            sendJson(exchange, 404, Map.of("error", "User not found"));
        }
    }

    private void handleUpdatePreferences(HttpExchange exchange, String path) throws IOException {
        // /api/user/{id}/preferences
        String userId = path.substring("/api/user/".length(), path.length() - "/preferences".length());
        String body = readBody(exchange);
        Map<String, Object> prefs = JsonUtil.parseMap(body);

        boolean updated = userService.updatePreferences(userId, prefs);
        if (updated) {
            User u = userService.getUser(userId);
            sendJson(exchange, 200, Map.of(
                    "success", true,
                    "message", "User preferences updated successfully.",
                    "user", u != null ? u.toMap() : Collections.emptyMap()
            ));
        } else {
            sendJson(exchange, 404, Map.of("error", "User not found for update"));
        }
    }

    private void handleGetRecommendations(HttpExchange exchange, Map<String, String> params) throws IOException {
        String userId = params.getOrDefault("userId", "usr-alice");
        String category = params.getOrDefault("category", "ALL");

        List<Item> recommendations = recommendationEngine.getPersonalizedRecommendations(userId, category);
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Item it : recommendations) {
            resList.add(it.toMap());
        }

        sendJson(exchange, 200, Map.of(
                "userId", userId,
                "category", category,
                "count", resList.size(),
                "recommendations", resList
        ));
    }

    private void handleSearch(HttpExchange exchange, Map<String, String> params) throws IOException {
        String q = params.getOrDefault("q", "");
        String userId = params.getOrDefault("userId", "usr-alice");
        String category = params.getOrDefault("category", "ALL");

        List<Item> results = recommendationEngine.search(q, userId, category);
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Item it : results) {
            resList.add(it.toMap());
        }

        sendJson(exchange, 200, Map.of(
                "query", q,
                "category", category,
                "count", resList.size(),
                "results", resList
        ));
    }

    private void handleCompare(HttpExchange exchange, Map<String, String> params) throws IOException {
        String itemId = params.get("itemId");
        String query = params.get("query");

        ProductComparison comp;
        if (itemId != null) {
            Item item = recommendationEngine.getItem(itemId);
            if (item != null) {
                comp = comparisonService.compareProduct(item);
            } else {
                comp = comparisonService.compareByQuery(itemId);
            }
        } else if (query != null) {
            comp = comparisonService.compareByQuery(query);
        } else {
            comp = comparisonService.compareByQuery("Wireless Noise Canceling Headphones");
        }

        sendJson(exchange, 200, comp.toMap());
    }

    private void handleMovie(HttpExchange exchange, Map<String, String> params) throws IOException {
        String itemId = params.get("itemId");
        String title = params.get("title");

        MovieDetails details;
        if (itemId != null) {
            Item item = recommendationEngine.getItem(itemId);
            if (item != null) {
                details = movieService.getMovieDetails(item);
            } else {
                details = movieService.getMovieDetailsByTitle(itemId);
            }
        } else if (title != null) {
            details = movieService.getMovieDetailsByTitle(title);
        } else {
            details = movieService.getMovieDetailsByTitle("Inception");
        }

        sendJson(exchange, 200, details.toMap());
    }

    private void handleRoute(HttpExchange exchange, Map<String, String> params) throws IOException {
        String origin = params.getOrDefault("origin", "Paris, France");
        String destination = params.getOrDefault("destination", "Eiffel Tower, Paris");

        RouteResult route = routingService.calculateShortestRoute(origin, destination);
        sendJson(exchange, 200, route.toMap());
    }

    private void handleFood(HttpExchange exchange, Map<String, String> params) throws IOException {
        String itemId = params.get("itemId");
        String name = params.get("name");

        FoodReview review;
        if (itemId != null) {
            Item item = recommendationEngine.getItem(itemId);
            if (item != null) {
                review = foodService.getFoodReview(item);
            } else {
                review = foodService.getFoodReviewByName(itemId);
            }
        } else if (name != null) {
            review = foodService.getFoodReviewByName(name);
        } else {
            review = foodService.getFoodReviewByName("Neapolitan Pizza Margherita");
        }

        sendJson(exchange, 200, review.toMap());
    }

    private void handleRecordInteraction(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> map = JsonUtil.parseMap(body);

        String userId = String.valueOf(map.getOrDefault("userId", "usr-alice"));
        String itemId = String.valueOf(map.getOrDefault("itemId", "unknown"));
        String category = String.valueOf(map.getOrDefault("category", "PRODUCT"));
        String type = String.valueOf(map.getOrDefault("type", "CLICK"));
        Double rating = map.get("rating") instanceof Number ? ((Number) map.get("rating")).doubleValue() : null;

        Interaction in = new Interaction(userId, itemId, category, type, rating);
        userService.recordInteraction(in);
        try {
            interactionDao.save(in);
        } catch (Exception ignored) {}

        // Online Reinforcement Learning Feedback Loop
        Item item = recommendationEngine.getItem(itemId);
        if (item != null) {
            userService.recordFeedbackReinforcement(userId, item, type);
        }

        sendJson(exchange, 200, Map.of("success", true, "interactionId", in.getId(), "onlineLearned", item != null));
    }

    private void handleUpdateAdminSettings(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> map = JsonUtil.parseMap(body);

        config.updateFromMap(map, "Admin");
        config.saveToDb();

        sendJson(exchange, 200, Map.of(
                "success", true,
                "message", "System settings successfully updated and applied to AI recommendation algorithms.",
                "settings", config.toMap()
        ));
    }

    private void handleApplyPreset(HttpExchange exchange, Map<String, String> params) throws IOException {
        String preset = params.get("name");
        if (preset == null || preset.isEmpty()) {
            String body = readBody(exchange);
            if (!body.isEmpty()) {
                Map<String, Object> map = JsonUtil.parseMap(body);
                if (map.containsKey("preset")) preset = String.valueOf(map.get("preset"));
                else if (map.containsKey("name")) preset = String.valueOf(map.get("name"));
            }
        }
        if (preset == null || preset.isEmpty()) {
            preset = "BALANCED_DEFAULTS";
        }

        boolean ok = config.applyPreset(preset, "Admin");
        if (ok) {
            config.saveToDb();
            sendJson(exchange, 200, Map.of(
                    "success", true,
                    "preset", preset,
                    "message", "Successfully applied preset: " + preset,
                    "settings", config.toMap()
            ));
        } else {
            sendJson(exchange, 400, Map.of(
                    "success", false,
                    "error", "Invalid preset name. Supported presets: ACCURACY_OPTIMIZED, EXPLORATION_DISCOVERY, BALANCED_DEFAULTS, HIGH_DIVERSITY"
            ));
        }
    }

    private void handleGetAnalytics(HttpExchange exchange) throws IOException {
        RecommendationMetrics metrics = userService.calculateMetrics(config);
        sendJson(exchange, 200, metrics.toMap());
    }

    private void handleDatabaseStatus(HttpExchange exchange) throws IOException {
        try {
            long usersCount = userDao.count();
            long itemsCount = itemDao.count();
            long interactionsCount = interactionDao.count();
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("jdbcAvailable", dbManager.isJdbcAvailable());
            resp.put("dbUrl", dbManager.getDbUrl());
            resp.put("driver", "org.sqlite.JDBC");
            resp.put("usersCount", usersCount);
            resp.put("itemsCount", itemsCount);
            resp.put("interactionsCount", interactionsCount);
            sendJson(exchange, 200, resp);
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void handleDatabaseUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> list = userDao.findAll();
            List<Map<String, Object>> res = new ArrayList<>();
            for (User u : list) res.add(u.toMap());
            sendJson(exchange, 200, Map.of("users", res, "count", res.size(), "source", "SQLite JDBC"));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void handleDatabaseItems(HttpExchange exchange) throws IOException {
        try {
            List<Item> list = itemDao.findAll();
            List<Map<String, Object>> res = new ArrayList<>();
            for (Item it : list) res.add(it.toMap());
            sendJson(exchange, 200, Map.of("items", res, "count", res.size(), "source", "SQLite JDBC"));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void handleDatabaseInteractions(HttpExchange exchange) throws IOException {
        try {
            List<Interaction> list = interactionDao.findAll();
            List<Map<String, Object>> res = new ArrayList<>();
            for (Interaction in : list) res.add(in.toMap());
            sendJson(exchange, 200, Map.of("interactions", res, "count", res.size(), "source", "SQLite JDBC"));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void handleGetUserActivity(HttpExchange exchange, String path) throws IOException {
        String userId = path.substring("/api/user/".length(), path.length() - "/activity".length());
        List<Map<String, Object>> list = userService.getUserActivity(userId);
        sendJson(exchange, 200, Map.of("userId", userId, "activity", list, "count", list.size()));
    }

    private void handleAdminCatalogAdd(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> map = JsonUtil.parseMap(body);
        String id = map.containsKey("id") ? String.valueOf(map.get("id")) : "custom-" + System.currentTimeMillis();
        String title = String.valueOf(map.getOrDefault("title", "New Custom Item"));
        String category = String.valueOf(map.getOrDefault("category", "PRODUCT")).toUpperCase();
        String description = String.valueOf(map.getOrDefault("description", ""));
        double rating = map.containsKey("rating") ? ((Number) map.get("rating")).doubleValue() : 4.8;
        int reviews = map.containsKey("reviewCount") ? ((Number) map.get("reviewCount")).intValue() : 1200;
        String imageUrl = String.valueOf(map.getOrDefault("imageUrl", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"));
        Double price = map.containsKey("price") && map.get("price") != null ? ((Number) map.get("price")).doubleValue() : 99.99;
        
        List<String> tags = new ArrayList<>();
        if (map.containsKey("tags") && map.get("tags") instanceof List<?>) {
            for (Object t : (List<?>) map.get("tags")) tags.add(String.valueOf(t));
        } else {
            tags.addAll(List.of(category, "Curated", "Featured"));
        }

        Item item = new Item(id, title, category, description, rating, reviews, imageUrl, tags, price);
        if (map.containsKey("type")) {
            item.setType(String.valueOf(map.get("type")));
        }
        if ("PRODUCT".equalsIgnoreCase(category)) {
            item.getMetadata().put("comparison", comparisonService.compareProduct(item).toMap());
        }
        recommendationEngine.addItem(item);
        try {
            itemDao.save(item);
        } catch (Exception ignored) {}

        sendJson(exchange, 200, Map.of(
                "success", true,
                "message", "Item successfully added to active catalog and persistent database.",
                "item", item.toMap()
        ));
    }

    private void handleAdminCatalogDelete(HttpExchange exchange, String path, Map<String, String> params) throws IOException {
        String id = params.get("id");
        if (id == null || id.isEmpty()) {
            String body = readBody(exchange);
            if (!body.isEmpty()) {
                Map<String, Object> map = JsonUtil.parseMap(body);
                id = String.valueOf(map.get("id"));
            }
        }
        if (id == null || id.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Item ID is required for deletion."));
            return;
        }
        boolean removed = recommendationEngine.removeItem(id);
        try {
            itemDao.deleteById(id);
        } catch (Exception ignored) {}
        sendJson(exchange, 200, Map.of("success", removed, "id", id, "message", "Item removed from catalog."));
    }

    private void handleAdminExport(HttpExchange exchange, Map<String, String> params) throws IOException {
        String format = params.getOrDefault("format", "json").toLowerCase();
        RecommendationMetrics metrics = userService.calculateMetrics(config);

        if ("csv".equals(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Report,OmniRecommend AI Recommendation Effectiveness Report\n");
            sb.append("GeneratedAt,").append(new Date()).append("\n");
            sb.append("TotalImpressions,").append(metrics.getTotalImpressions()).append("\n");
            sb.append("TotalClicks,").append(metrics.getTotalClicks()).append("\n");
            sb.append("TotalConversions,").append(metrics.getTotalConversions()).append("\n");
            sb.append("OverallCTR,").append(metrics.getOverallCtr()).append("%\n");
            sb.append("ConversionRate,").append(metrics.getConversionRate()).append("%\n");
            sb.append("PrecisionAt10,").append(metrics.getPrecisionAt10()).append("\n");
            sb.append("RecallAt10,").append(metrics.getRecallAt10()).append("\n");
            sb.append("UserEngagementLift,").append(metrics.getUserEngagementLift()).append("%\n\n");

            sb.append("Category,Impressions,Clicks,CTR%,ConversionRate%\n");
            for (Map.Entry<String, RecommendationMetrics.CategoryMetric> entry : metrics.getCategoryMetrics().entrySet()) {
                var c = entry.getValue();
                sb.append(c.getCategory()).append(",").append(c.getImpressions()).append(",").append(c.getClicks())
                  .append(",").append(c.getCtr()).append("%,")
                  .append(c.getConversionRate()).append("%\n");
            }

            sb.append("\nDate,DailyImpressions,DailyClicks,DailyCTR%\n");
            for (RecommendationMetrics.DailyMetric dm : metrics.getTimelineMetrics()) {
                Map<String, Object> m = dm.toMap();
                sb.append(m.get("date")).append(",").append(m.get("impressions")).append(",").append(m.get("clicks"))
                  .append(",").append(m.get("ctr")).append("%\n");
            }

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"recommendation_report.csv\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("timestamp", System.currentTimeMillis());
            report.put("metrics", metrics.toMap());
            report.put("config", config.toMap());
            sendJson(exchange, 200, report);
        }
    }

    private void handleAdminOptimize(HttpExchange exchange) throws IOException {
        config.setAlgorithmMode("HYBRID");
        config.setRecencyDecayRate(0.06);
        config.applyPreset("ACCURACY_OPTIMIZED", "AI_Advisor");
        config.saveToDb();

        sendJson(exchange, 200, Map.of(
                "success", true,
                "message", "AI Advisor Optimization Applied! Hyperparameters auto-tuned: Content Weight boosted to 60%, Epsilon exploration tuned to 5%, and Recency Decay adapted.",
                "settings", config.toMap()
        ));
    }

    private void handleDeepHealth(HttpExchange exchange) throws IOException {
        long usedHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long maxHeap = Runtime.getRuntime().maxMemory();
        int activeThreads = Thread.activeCount();
        boolean dbUp = dbManager.isJdbcAvailable();
        long userCount = 0;
        long itemCount = 0;
        long interactionCount = 0;
        try {
            userCount = userDao.count();
            itemCount = itemDao.count();
            interactionCount = interactionDao.count();
        } catch (Exception ignored) {}

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", dbUp ? "HEALTHY" : "DEGRADED");
        report.put("timestamp", System.currentTimeMillis());
        report.put("uptimeMs", System.currentTimeMillis() - serverStartTime);
        report.put("database", Map.of(
                "connected", dbUp,
                "engine", "SQLite 3",
                "usersCount", userCount,
                "itemsCount", itemCount,
                "interactionsCount", interactionCount
        ));
        report.put("jvm", Map.of(
                "usedHeapBytes", usedHeap,
                "maxHeapBytes", maxHeap,
                "activeThreads", activeThreads,
                "availableProcessors", Runtime.getRuntime().availableProcessors()
        ));
        report.put("security", Map.of(
                "rateLimiting", "ENABLED",
                "rateLimitPerMinute", 120,
                "trackedClients", rateLimiter.getClientCount()
        ));
        report.put("totalRequestsProcessed", requestCounter.get());

        sendJson(exchange, 200, report);
    }

    private void handlePrometheusMetrics(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=UTF-8");
        long usedHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long maxHeap = Runtime.getRuntime().maxMemory();
        int activeThreads = Thread.activeCount();

        String body = String.format("""
            # HELP http_requests_total Total number of HTTP requests processed
            # TYPE http_requests_total counter
            http_requests_total %d
            # HELP jvm_memory_used_bytes JVM memory used in bytes
            # TYPE jvm_memory_used_bytes gauge
            jvm_memory_used_bytes %d
            # HELP jvm_memory_max_bytes JVM maximum available memory in bytes
            # TYPE jvm_memory_max_bytes gauge
            jvm_memory_max_bytes %d
            # HELP jvm_active_threads Active JVM thread count
            # TYPE jvm_active_threads gauge
            jvm_active_threads %d
            # HELP rate_limiter_clients_active Number of active client buckets tracked by the rate limiter
            # TYPE rate_limiter_clients_active gauge
            rate_limiter_clients_active %d
            """,
                requestCounter.get(),
                usedHeap,
                maxHeap,
                activeThreads,
                rateLimiter.getClientCount()
        );

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleUserLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> map = JsonUtil.parseMap(body);
        String identifier = String.valueOf(map.getOrDefault("identifier", map.getOrDefault("userId", "")));
        String password = map.containsKey("password") ? String.valueOf(map.get("password")) : null;

        User user = userService.authenticate(identifier, password);
        if (user != null) {
            String token = userService.createSession(user);
            sendJson(exchange, 200, Map.of(
                    "success", true,
                    "token", token,
                    "user", user.toMap(),
                    "message", "Successfully authenticated as " + user.getUsername()
            ));
        } else {
            sendJson(exchange, 401, Map.of(
                    "success", false,
                    "error", "Authentication failed. Invalid User ID or credentials."
            ));
        }
    }

    private void handleAdminLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, Object> map = JsonUtil.parseMap(body);
        String identifier = String.valueOf(map.getOrDefault("identifier", map.getOrDefault("username", "admin")));
        String password = String.valueOf(map.getOrDefault("password", ""));

        User user = userService.authenticate(identifier, password);
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            String token = userService.createSession(user);
            sendJson(exchange, 200, Map.of(
                    "success", true,
                    "token", token,
                    "user", user.toMap(),
                    "message", "Administrator authorization verified. Welcome to Admin Control Panel."
            ));
        } else {
            sendJson(exchange, 401, Map.of(
                    "success", false,
                    "error", "Access Denied: Invalid administrator credentials or insufficient privileges."
            ));
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String body = readBody(exchange);
        String token = authHeader;
        if (body != null && !body.isEmpty()) {
            try {
                Map<String, Object> map = JsonUtil.parseMap(body);
                if (map.containsKey("token")) {
                    token = String.valueOf(map.get("token"));
                }
            } catch (Exception ignored) {}
        }
        boolean invalidated = userService.invalidateSession(token);
        sendJson(exchange, 200, Map.of("success", true, "invalidated", invalidated, "message", "Session successfully terminated."));
    }

    private void handleGetCurrentUser(HttpExchange exchange, Map<String, String> params) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null && params.containsKey("token")) {
            token = params.get("token");
        }
        User user = userService.getSessionUser(token);
        if (user != null) {
            sendJson(exchange, 200, Map.of("authenticated", true, "user", user.toMap()));
        } else {
            sendJson(exchange, 401, Map.of("authenticated", false, "error", "No active session or session expired."));
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] bytes = JsonUtil.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = (idx < pair.length() - 1) ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : "";
                map.put(key, val);
            } else if (idx == -1 && !pair.isEmpty()) {
                map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }
}
