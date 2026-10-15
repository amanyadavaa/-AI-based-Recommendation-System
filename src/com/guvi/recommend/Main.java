package com.guvi.recommend;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.db.dao.InteractionDao;
import com.guvi.recommend.db.dao.ItemDao;
import com.guvi.recommend.db.dao.UserDao;
import com.guvi.recommend.model.Interaction;
import com.guvi.recommend.model.Item;
import com.guvi.recommend.model.User;
import com.guvi.recommend.service.*;
import com.guvi.recommend.web.ApiHandler;
import com.guvi.recommend.web.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (Exception ignored) {}
        }
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ignored) {}
        }

        try {
            System.out.println("==================================================================");
            System.out.println("   OMNIRECOMMEND AI: UNIVERSAL RECOMMENDATION SYSTEM             ");
            System.out.println("==================================================================");
            System.out.println("Initializing system modules & services...");

            SystemConfig config = new SystemConfig();
            UserService userService = new UserService();
            ComparisonService comparisonService = new ComparisonService();
            MovieService movieService = new MovieService();
            RoutingService routingService = new RoutingService();
            FoodService foodService = new FoodService();

            InternetSearchService internetSearchService = new InternetSearchService(
                    comparisonService, movieService, routingService, foodService
            );

            RecommendationEngine recommendationEngine = new RecommendationEngine(
                    config, userService, comparisonService, movieService, routingService, foodService, internetSearchService
            );

            // Initialize SQLite JDBC Database & Seed Entities
            System.out.println("Initializing SQLite JDBC database connectivity...");
            DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
            UserDao userDao = new UserDao();
            ItemDao itemDao = new ItemDao();
            InteractionDao interactionDao = new InteractionDao();
            try {
                for (User u : userService.getAllUsers()) userDao.save(u);
                for (Item it : recommendationEngine.getItemCatalog().values()) itemDao.save(it);
                for (Interaction in : userService.getAllInteractions()) interactionDao.save(in);
                System.out.println("✔ Seeded SQLite JDBC database: " + userDao.count() + " users, " + itemDao.count() + " items, " + interactionDao.count() + " interactions.");
            } catch (Exception e) {
                System.out.println("ℹ Database running in memory-sync mode: " + e.getMessage());
            }

            HttpServer server;
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (Exception e) {
                // Try fallback port if 8080 is occupied
                port = 8085;
                server = HttpServer.create(new InetSocketAddress(port), 0);
            }

            // Register handlers
            ApiHandler apiHandler = new ApiHandler(
                    config, userService, recommendationEngine,
                    comparisonService, movieService, routingService, foodService
            );
            server.createContext("/api/", apiHandler);

            StaticFileHandler staticHandler = new StaticFileHandler("web");
            server.createContext("/", staticHandler);

            // Multithreaded executor for high concurrency
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();

            System.out.println("✔ System Server Started Successfully!");
            System.out.println("------------------------------------------------------------------");
            System.out.println("🌐 Web Dashboard Application: http://localhost:" + port + "/");
            System.out.println("📡 REST API Endpoints:        http://localhost:" + port + "/api/");
            System.out.println("👤 Pre-configured Accounts:");
            System.out.println("   • Admin:  admin (Admin Dashboard, System Settings, Effectiveness Analysis)");
            System.out.println("   • User 1: Alice (Personalized Feed, Product & Movie Preference)");
            System.out.println("   • User 2: Bob   (Travel Places, Food & Restaurant Enthusiast)");
            System.out.println("------------------------------------------------------------------");
            System.out.println("🌟 Features Ready:");
            System.out.println("   [1] Products: Multi-Site E-Commerce Price & Spec Comparison");
            System.out.println("   [2] Movies: Direct Download Links & Streaming Provider Finder");
            System.out.println("   [3] Places: Shortest Path Routing, Travel Time & Interactive Map");
            System.out.println("   [4] Food: Internet Review Aggregation & Sentiment Analysis");
            System.out.println("   [5] Internet Search: Live Omnichannel Fallback Engine");
            System.out.println("==================================================================");
            System.out.println("Press Ctrl+C to terminate the server.");

        } catch (Exception e) {
            System.err.println("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
