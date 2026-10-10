package com.guvi.recommend.service;

import com.guvi.recommend.model.Item;
import com.guvi.recommend.model.ProductComparison;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ComparisonService {

    public ProductComparison compareProduct(Item item) {
        double basePrice = item.getPrice() != null ? item.getPrice() : 99.99;
        String title = item.getTitle();
        String brand = extractBrand(title, item.getTags());
        return generateComparison(item.getId(), title, brand, basePrice);
    }

    public ProductComparison compareByQuery(String query) {
        double estimatedPrice = estimatePriceFromQuery(query);
        String brand = extractBrand(query, Collections.emptyList());
        return generateComparison("custom-" + System.currentTimeMillis(), query, brand, estimatedPrice);
    }

    private ProductComparison generateComparison(String id, String title, String brand, double basePrice) {
        List<ProductComparison.StoreListing> listings = new ArrayList<>();
        String encodedQuery = URLEncoder.encode(title, StandardCharsets.UTF_8);

        // Store 1: Amazon
        double p1 = Math.round((basePrice * 0.98) * 100.0) / 100.0;
        listings.add(new ProductComparison.StoreListing(
                "Amazon",
                p1,
                "USD",
                4.8,
                38420,
                "Free 1-Day Prime Delivery",
                true,
                "https://www.amazon.com/s?k=" + encodedQuery,
                "Prime Verified"
        ));

        // Store 2: Best Buy
        double p2 = Math.round((basePrice * 1.02) * 100.0) / 100.0;
        listings.add(new ProductComparison.StoreListing(
                "Best Buy",
                p2,
                "USD",
                4.7,
                14210,
                "Free Standard Shipping (2 Days) or Store Pickup",
                true,
                "https://www.bestbuy.com/site/searchpage.jsp?st=" + encodedQuery,
                "Official Warranty"
        ));

        // Store 3: Walmart
        double p3 = Math.round((basePrice * 0.92) * 100.0) / 100.0; // Lowest price deal
        listings.add(new ProductComparison.StoreListing(
                "Walmart",
                p3,
                "USD",
                4.5,
                19800,
                "Free 2-Day Shipping on $35+",
                true,
                "https://www.walmart.com/search?q=" + encodedQuery,
                "Lowest Price"
        ));

        // Store 4: eBay
        double p4 = Math.round((basePrice * 0.95) * 100.0) / 100.0;
        listings.add(new ProductComparison.StoreListing(
                "eBay",
                p4,
                "USD",
                4.6,
                8730,
                "$4.99 Expedited Shipping - Top Rated Seller",
                true,
                "https://www.ebay.com/sch/i.html?_nkw=" + encodedQuery,
                "Buyer Protection"
        ));

        // Store 5: Flipkart
        double p5 = Math.round((basePrice * 0.96) * 100.0) / 100.0;
        listings.add(new ProductComparison.StoreListing(
                "Flipkart",
                p5,
                "USD",
                4.4,
                22400,
                "Free Delivery with Flipkart Plus",
                true,
                "https://www.flipkart.com/search?q=" + encodedQuery,
                "Assured"
        ));

        // Store 6: Target
        double p6 = Math.round((basePrice * 1.05) * 100.0) / 100.0;
        listings.add(new ProductComparison.StoreListing(
                "Target",
                p6,
                "USD",
                4.7,
                6410,
                "Free Pickup in 2 Hours",
                true,
                "https://www.target.com/s?searchTerm=" + encodedQuery,
                "RedCard 5% Off"
        ));

        // Sort listings by price ascending
        listings.sort(Comparator.comparingDouble(ProductComparison.StoreListing::getPrice));

        return new ProductComparison(id, title, brand, listings);
    }

    private String extractBrand(String title, List<String> tags) {
        String[] knownBrands = {
                "Apple", "Sony", "Samsung", "Bose", "Nike", "Adidas", "Dell", "LG",
                "Logitech", "ASUS", "Canon", "HP", "Lenovo", "Microsoft", "Google",
                "Nintendo", "PlayStation", "Xbox", "Puma", "Casio", "Rolex"
        };
        String lower = title.toLowerCase();
        for (String b : knownBrands) {
            if (lower.contains(b.toLowerCase())) return b;
        }
        if (tags != null && !tags.isEmpty()) {
            return tags.get(0);
        }
        return "Top Brand";
    }

    private double estimatePriceFromQuery(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("laptop") || lower.contains("macbook")) return 999.0;
        if (lower.contains("phone") || lower.contains("iphone") || lower.contains("pixel") || lower.contains("galaxy")) return 799.0;
        if (lower.contains("tablet") || lower.contains("ipad")) return 499.0;
        if (lower.contains("console") || lower.contains("ps5") || lower.contains("xbox") || lower.contains("switch")) return 449.0;
        if (lower.contains("tv") || lower.contains("monitor")) return 429.0;
        if (lower.contains("camera")) return 649.0;
        if (lower.contains("watch")) return 249.0;
        if (lower.contains("headphone") || lower.contains("earbuds") || lower.contains("airpods")) return 199.0;
        if (lower.contains("shoes") || lower.contains("sneaker") || lower.contains("boots")) return 119.0;
        if (lower.contains("keyboard") || lower.contains("mouse")) return 79.0;
        if (lower.contains("speaker") || lower.contains("soundbar")) return 149.0;
        return 89.99;
    }
}
