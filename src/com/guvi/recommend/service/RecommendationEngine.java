package com.guvi.recommend.service;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecommendationEngine {
    private final SystemConfig config;
    private final UserService userService;
    private final ComparisonService comparisonService;
    private final MovieService movieService;
    private final RoutingService routingService;
    private final FoodService foodService;
    private final InternetSearchService internetSearchService;

    private final Map<String, Item> itemCatalog = new ConcurrentHashMap<>();

    public RecommendationEngine(SystemConfig config, UserService userService, ComparisonService comparisonService, MovieService movieService, RoutingService routingService, FoodService foodService, InternetSearchService internetSearchService) {
        this.config = config;
        this.userService = userService;
        this.comparisonService = comparisonService;
        this.movieService = movieService;
        this.routingService = routingService;
        this.foodService = foodService;
        this.internetSearchService = internetSearchService;
        seedCatalog();
    }

    private void seedCatalog() {
        // --- 1. PRODUCTS ---
        Item p1 = new Item("prod-1", "Sony WH-1000XM5 Noise Canceling Headphones", "PRODUCT",
                "Industry-leading noise cancellation with two processors and 8 microphones. Exceptional sound quality with Hi-Res Audio wireless.",
                4.8, 24100,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
                List.of("Audio", "Wireless", "Headphones", "Noise Canceling", "Sony"), 399.99);
        p1.getMetadata().put("comparison", comparisonService.compareProduct(p1).toMap());
        itemCatalog.put(p1.getId(), p1);

        Item p2 = new Item("prod-2", "Apple MacBook Air 15-inch M3 Chip", "PRODUCT",
                "Strikingly thin design with blazing-fast Apple M3 chip, Liquid Retina display, and up to 18 hours of battery life.",
                4.9, 18500,
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80",
                List.of("Laptop", "Apple", "M3", "MacBook", "Computer"), 1299.00);
        p2.getMetadata().put("comparison", comparisonService.compareProduct(p2).toMap());
        itemCatalog.put(p2.getId(), p2);

        Item p3 = new Item("prod-3", "Samsung Galaxy S24 Ultra 512GB AI Smartphone", "PRODUCT",
                "Titanium frame with Galaxy AI live translation, 200MP camera with Quad Telephoto system, and built-in S Pen.",
                4.7, 31200,
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80",
                List.of("Smartphone", "Samsung", "Galaxy", "5G", "AI Camera"), 1199.99);
        p3.getMetadata().put("comparison", comparisonService.compareProduct(p3).toMap());
        itemCatalog.put(p3.getId(), p3);

        Item p4 = new Item("prod-4", "Nike Air Zoom Alphafly 3 Marathon Racing Shoes", "PRODUCT",
                "Fine-tuned for marathon speed with dual Air Zoom units, full-length carbon fiber Flyplate, and ultra-lightweight ZoomX foam.",
                4.6, 9400,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                List.of("Shoes", "Running", "Nike", "Sneakers", "Athletic"), 285.00);
        p4.getMetadata().put("comparison", comparisonService.compareProduct(p4).toMap());
        itemCatalog.put(p4.getId(), p4);

        // --- 2. MOVIES ---
        Item m1 = new Item("mov-1", "Inception (2010)", "MOVIE",
                "A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
                8.8, 2520000,
                "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80",
                List.of("Sci-Fi", "Action", "Thriller", "Mind-Bending", "Christopher Nolan"), null);
        m1.getMetadata().put("movieDetails", movieService.getMovieDetails(m1).toMap());
        itemCatalog.put(m1.getId(), m1);

        Item m2 = new Item("mov-2", "Interstellar (2014)", "MOVIE",
                "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked to pilot a spacecraft along with a team of researchers through a wormhole.",
                8.7, 2010000,
                "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=600&q=80",
                List.of("Sci-Fi", "Drama", "Adventure", "Space", "Hans Zimmer"), null);
        m2.getMetadata().put("movieDetails", movieService.getMovieDetails(m2).toMap());
        itemCatalog.put(m2.getId(), m2);

        Item m3 = new Item("mov-3", "The Dark Knight (2008)", "MOVIE",
                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.",
                9.0, 2890000,
                "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80",
                List.of("Action", "Crime", "Drama", "Superhero", "Heath Ledger"), null);
        m3.getMetadata().put("movieDetails", movieService.getMovieDetails(m3).toMap());
        itemCatalog.put(m3.getId(), m3);

        Item m4 = new Item("mov-4", "Oppenheimer (2023)", "MOVIE",
                "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
                8.9, 780000,
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80",
                List.of("Biography", "Drama", "History", "Oscar Winner", "IMAX"), null);
        m4.getMetadata().put("movieDetails", movieService.getMovieDetails(m4).toMap());
        itemCatalog.put(m4.getId(), m4);

        // --- 3. PLACES ---
        Item pl1 = new Item("plc-1", "Eiffel Tower & Champ de Mars, Paris", "PLACE",
                "Iconic 330-meter wrought-iron lattice tower on the Champ de Mars in Paris, offering panoramic vistas across the City of Light.",
                4.9, 142000,
                "https://images.unsplash.com/photo-1511739001486-6bfe10ce785f?auto=format&fit=crop&w=600&q=80",
                List.of("Landmark", "Paris", "Architecture", "Panoramic View", "Monument"), null);
        var r1 = routingService.calculateShortestRoute("Notre Dame, Paris", "Eiffel Tower, Paris");
        pl1.getMetadata().put("route", r1.toMap());
        itemCatalog.put(pl1.getId(), pl1);

        Item pl2 = new Item("plc-2", "Louvre Museum & Glass Pyramid, Paris", "PLACE",
                "The world's most-visited museum and historic monument housing masterpiece treasures including Mona Lisa and Venus de Milo.",
                4.8, 115000,
                "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=600&q=80",
                List.of("Museum", "Art", "Culture", "History", "Paris"), null);
        var r2 = routingService.calculateShortestRoute("Eiffel Tower, Paris", "Louvre Museum, Paris");
        pl2.getMetadata().put("route", r2.toMap());
        itemCatalog.put(pl2.getId(), pl2);

        Item pl3 = new Item("plc-3", "Taj Mahal Heritage Monument, Agra", "PLACE",
                "Immense mausoleum of white marble, built in Agra by Mughal emperor Shah Jahan in memory of his beloved wife, a UNESCO World Wonder.",
                4.9, 98000,
                "https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=600&q=80",
                List.of("World Wonder", "Monument", "Heritage", "UNESCO", "India"), null);
        var r3 = routingService.calculateShortestRoute("Delhi", "Taj Mahal, Agra");
        pl3.getMetadata().put("route", r3.toMap());
        itemCatalog.put(pl3.getId(), pl3);

        // --- 4. FOOD ---
        Item f1 = new Item("food-1", "L'Antica Pizzeria da Michele - Margherita Special", "FOOD",
                "World-renowned authentic wood-fired Neapolitan pizza with San Marzano tomatoes, fresh fior di latte mozzarella, and aromatic basil.",
                4.9, 28400,
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80",
                List.of("Italian", "Pizza", "Neapolitan", "Wood-Fired", "Vegetarian"), 16.50);
        f1.getMetadata().put("foodReview", foodService.getFoodReview(f1).toMap());
        itemCatalog.put(f1.getId(), f1);

        Item f2 = new Item("food-2", "Sukiyabashi Artisan Sushi & Bluefin Omakase", "FOOD",
                "Legendary Ginza style omakase featuring aged Bluefin tuna, Hokkaido sea urchin, and vinegared shari rice crafted with precision.",
                4.9, 16200,
                "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=600&q=80",
                List.of("Japanese", "Sushi", "Omakase", "Michelin Guide", "Seafood"), 120.00);
        f2.getMetadata().put("foodReview", foodService.getFoodReview(f2).toMap());
        itemCatalog.put(f2.getId(), f2);

        Item f3 = new Item("food-3", "Truffle Bacon Jam Smash Burger & Rosemary Fries", "FOOD",
                "Double dry-aged prime beef smashed with crisp lacy edges, melted aged white cheddar, bourbon bacon onion jam, and black truffle aioli.",
                4.8, 14800,
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80",
                List.of("American", "Burger", "Craft Beer", "Comfort Food", "Gourmet"), 18.00);
        f3.getMetadata().put("foodReview", foodService.getFoodReview(f3).toMap());
        itemCatalog.put(f3.getId(), f3);

        Item f4 = new Item("food-4", "Awadhi Dum Gosht Biryani & Burani Raita", "FOOD",
                "Fragrant long-grain basmati rice layered with tender saffron-marinated meat, slow-cooked in a sealed clay handi over charcoal embers.",
                4.9, 21900,
                "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80",
                List.of("Indian", "Biryani", "Awadhi", "Halal", "Royal Mughlai"), 22.00);
        f4.getMetadata().put("foodReview", foodService.getFoodReview(f4).toMap());
        itemCatalog.put(f4.getId(), f4);
    }

    public List<Item> getPersonalizedRecommendations(String userId, String categoryFilter) {
        User user = userService.getUser(userId);
        if (user == null) {
            user = userService.getUser("usr-alice"); // fallback default
        }

        List<Item> candidates = new ArrayList<>();
        for (Item item : itemCatalog.values()) {
            if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
                if (!item.getCategory().equalsIgnoreCase(categoryFilter)) {
                    continue;
                }
            }
            candidates.add(item);
        }

        List<Interaction> userHistory = userService.getUserInteractions(user.getId());
        Set<String> interactedIds = new HashSet<>();
        for (Interaction in : userHistory) {
            interactedIds.add(in.getItemId());
        }

        Random rng = new Random();
        double explorationRate = config.getExplorationRate();

        for (Item item : candidates) {
            // 1. Content-based similarity score
            double contentScore = computeContentScore(user, item);

            // 2. Collaborative filtering score
            double collabScore = computeCollaborativeScore(user, item);

            // 3. Popularity & Trend score
            double popScore = Math.min(1.0, (Math.log10(Math.max(10, item.getReviewCount())) / 6.0) * (item.getRating() / 5.0));

            // Hybrid score weighted by Admin settings
            double hybridScore = (config.getContentWeight() * contentScore)
                    + (config.getCollaborativeWeight() * collabScore)
                    + (config.getPopularityWeight() * popScore);

            // Epsilon-greedy exploration injection
            if (rng.nextDouble() < explorationRate && !interactedIds.contains(item.getId())) {
                hybridScore = Math.min(0.99, hybridScore + 0.15); // Serendipity boost
            }

            // Normalization
            hybridScore = Math.max(0.1, Math.min(0.99, hybridScore));
            item.setMatchScore(hybridScore);

            // Log VIEW interaction for active user
            userService.recordInteraction(new Interaction(user.getId(), item.getId(), item.getCategory(), "VIEW", null));
        }

        // Filter by admin min confidence threshold
        double threshold = config.getMinConfidenceThreshold();
        List<Item> filtered = new ArrayList<>();
        for (Item item : candidates) {
            if (item.getMatchScore() >= threshold) {
                filtered.add(item);
            }
        }

        // Sort descending by match score
        filtered.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // Limit results to maxRecommendationsPerQuery
        int limit = Math.min(config.getMaxRecommendationsPerQuery(), filtered.size());
        return filtered.subList(0, limit);
    }

    public List<Item> search(String query, String userId, String categoryFilter) {
        if (query == null || query.trim().isEmpty()) {
            return getPersonalizedRecommendations(userId, categoryFilter);
        }
        query = query.trim().toLowerCase();

        List<Item> localMatches = new ArrayList<>();
        for (Item item : itemCatalog.values()) {
            if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
                if (!item.getCategory().equalsIgnoreCase(categoryFilter)) {
                    continue;
                }
            }

            boolean matches = item.getTitle().toLowerCase().contains(query)
                    || item.getDescription().toLowerCase().contains(query)
                    || item.getCategory().toLowerCase().contains(query);

            if (!matches && item.getTags() != null) {
                for (String tag : item.getTags()) {
                    if (tag.toLowerCase().contains(query)) {
                        matches = true;
                        break;
                    }
                }
            }

            if (matches) {
                item.setMatchScore(0.95);
                localMatches.add(item);
            }
        }

        // Record search interaction
        if (userId != null) {
            userService.recordInteraction(new Interaction(userId, "search-" + query, "SEARCH", "SEARCH", null));
        }

        // If no local matches or live web search enabled, query the universal internet search service
        if (config.isEnableLiveWebSearch()) {
            User user = userService.getUser(userId);
            String origin = (user != null && user.getPreferences().containsKey("currentLocation"))
                    ? String.valueOf(user.getPreferences().get("currentLocation"))
                    : "Current Location";

            List<Item> internetResults = internetSearchService.searchInternet(query, origin);
            for (Item netItem : internetResults) {
                netItem.setMatchScore(0.92);
                // add to temporary catalog so it can be referenced
                itemCatalog.put(netItem.getId(), netItem);
                localMatches.add(netItem);
            }
        }

        return localMatches;
    }

    private double computeContentScore(User user, Item item) {
        double score = 0.5; // baseline
        Map<String, Object> prefs = user.getPreferences();

        // Category match
        if (prefs.containsKey("preferredCategories")) {
            @SuppressWarnings("unchecked")
            List<String> prefCats = (List<String>) prefs.get("preferredCategories");
            if (prefCats != null && prefCats.contains(item.getCategory())) {
                score += 0.3;
            }
        }

        // Genre / Tag match
        if (prefs.containsKey("movieGenres") && "MOVIE".equals(item.getCategory())) {
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) prefs.get("movieGenres");
            if (genres != null && item.getTags() != null) {
                for (String g : genres) {
                    if (item.getTags().contains(g)) {
                        score += 0.2;
                        break;
                    }
                }
            }
        }

        // Dietary match
        if (prefs.containsKey("dietaryPreference") && "FOOD".equals(item.getCategory())) {
            String diet = String.valueOf(prefs.get("dietaryPreference"));
            if (!"All".equalsIgnoreCase(diet) && item.getTags() != null) {
                if (item.getTags().contains(diet)) {
                    score += 0.25;
                }
            }
        }

        // Price preference match
        if (prefs.containsKey("pricePreference") && item.getPrice() != null) {
            String tier = String.valueOf(prefs.get("pricePreference"));
            double price = item.getPrice();
            if ("LOW".equalsIgnoreCase(tier) && price < 50) score += 0.15;
            else if ("MEDIUM".equalsIgnoreCase(tier) && price >= 50 && price <= 400) score += 0.15;
            else if ("HIGH".equalsIgnoreCase(tier) && price > 400) score += 0.15;
        }

        // Online Reinforcement Learning: Tag affinities learned dynamically from feedback
        if (prefs.containsKey("tagAffinities") && item.getTags() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Double> affinities = (Map<String, Double>) prefs.get("tagAffinities");
            if (affinities != null && !affinities.isEmpty()) {
                double tagBonus = 0.0;
                for (String tag : item.getTags()) {
                    tagBonus += affinities.getOrDefault(tag, 0.0);
                }
                score += Math.max(-0.4, Math.min(0.4, tagBonus));
            }
        }

        return Math.max(0.05, Math.min(1.0, score));
    }

    private double computeCollaborativeScore(User user, Item item) {
        // Collaborative filtering: check how frequently other users interacted with this item
        List<Interaction> all = userService.getAllInteractions();
        long itemInteractions = 0;
        long totalInteractions = Math.max(1, all.size());

        for (Interaction in : all) {
            if (item.getId().equals(in.getItemId()) && !in.getUserId().equals(user.getId())) {
                itemInteractions++;
            }
        }

        double ratio = (double) itemInteractions / totalInteractions;
        return Math.min(1.0, 0.4 + (ratio * 15.0));
    }

    public Item getItem(String id) {
        return itemCatalog.get(id);
    }

    public Map<String, Item> getItemCatalog() {
        return itemCatalog;
    }
}
