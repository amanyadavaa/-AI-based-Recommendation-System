# 🌟 OmniRecommend AI — Universal AI-Based Recommendation System
> **GUVI Final Project #69: AI-based Recommendation System**  
> *An Omnichannel Recommendation, Multi-Store Price Comparison, Movie Download Link Generator, Shortest Path Travel Navigator & Internet Review Sentiment Synthesis Engine.*

---

## 📌 Executive Summary & Project Overview

**OmniRecommend AI** is a full-stack, enterprise-grade AI Recommendation and Internet Comparison System built with **Pure Java 25 LTS** and a modern **Responsive Web Application UI**. 

The system implements a **Hybrid AI Recommendation Engine** combining:
1. **Content-Based Filtering** (TF-IDF & Preference Vector Cosine Similarity)
2. **Collaborative Filtering** (User-to-User interaction and co-occurrence correlation)
3. **Popularity & Trend Scoring** (Bayesian average ratings & logarithmic review volume)
4. **$\epsilon$-Greedy Serendipity / Exploration Engine** (Combats cold-start and filter bubble bias)
5. **Real-time Live Internet Synthesis** (Fetches, enriches, and structures dynamic data for *anything* queried on the web)

---

## 🎯 Complete Fulfillment of GUVI Assignment Specifications

| Specification Area | Project Requirement | OmniRecommend AI Implementation |
| :--- | :--- | :--- |
| **User Types** | **Admin** | Manages system settings, algorithm weights, and analyzes recommendation effectiveness reports. |
| | **User** | Receives personalized feeds, searches anything on the internet, and updates personal preferences. |
| **Admin Functionalities** | **System Settings Management** | Interactive form with live sliders to adjust $W_{content}$, $W_{collab}$, $W_{pop}$, exploration $\epsilon$, and min confidence threshold. Provides instant save confirmation banner and preset profiles. |
| | **Recommendation Effectiveness Analysis** | Real-time analytics dashboard with KPI metrics (CTR, Conversion Rate, Precision@5, Recall@5, Diversity Score), Chart.js category performance bar charts, 7-day timeline graphs, and automated AI algorithmic improvement suggestions. |
| **User Functionalities** | **Personalized Recommendations** | Dynamic feed personalized to the active user's behavioral history, clicks, views, ratings, and explicit category preferences. |
| | **Preference Management** | Interactive preferences form allowing users to select categories, price tolerance (Low, Medium, High), dietary preferences (Veg, Vegan, Halal), and travel location anchors with instant update confirmation. |
| **Dashboards** | **Admin Dashboard** | System Settings Form + Recommendation Analysis Graphs + User Profile Management Table. |
| | **User Dashboard** | Personalized Recommendation Feed + Universal Omni-Search Bar + Preference Settings Drawer. |

---

## 🌐 Specialized Domain Engines (User's Custom Requirements)

### 1. 🛍️ Products: Multi-Site E-Commerce Price Comparison
- Searches across **6 major internet retailers**: Amazon, Walmart, Best Buy, eBay, Flipkart, and Target.
- Automatically calculates:
  - **Lowest Price** found on the web & highlighted store.
  - **Maximum Price Savings** (dollars & percentage).
  - **Average Web Price** and price spread across retailers.
  - Direct, verified **"Visit Store"** outbound links to retailers.
  - Stock availability and delivery terms (e.g. "Free 1-Day Prime Delivery", "Store Pickup in 2 Hours").

### 2. 🎬 Movies & TV: Direct Download Links & Streaming Finder
- Direct links to **verified, high-speed download mirrors**:
  - Internet Archive Open Media Vault (Direct MP4).
  - Verified Public Magnet / Torrent Links (4K UHD HDR HEVC).
  - OpenFlix Mobile Optimized Direct Mirror (720p MP4).
- Official streaming provider links: **Netflix, Amazon Prime Video, Apple TV+, and Disney+ / Hulu** with pricing and availability tags.
- Verified IMDb score, Rotten Tomatoes percentage, synopsis, director, and full cast details.

### 3. 📍 Places & Travel: Shortest Path Navigation & Map
- Geocodes origin and destination using the **OpenStreetMap Nominatim Geocoding API**.
- Calculates the optimal shortest driving route using the **OSRM (Open Source Routing Machine) Engine** (with intelligent mathematical Haversine/Dijkstra fallback if offline).
- Displays:
  - **Shortest Driving Distance** in kilometers.
  - **Estimated Travel Duration** in hours and minutes.
  - **Step-by-Step Turn Directions** (e.g., street names, turn maneuvers, distance per segment).
  - **Interactive Leaflet.js Map** plotting route polylines and start/destination pins.
  - Direct 1-click **Google Maps GPS Navigation** link.

### 4. 🍔 Food & Dining: Internet Review Aggregation & Sentiment Breakdown
- Aggregates verified customer reviews from **Google Reviews, Yelp, and TripAdvisor**.
- Computes **NLP Sentiment Breakdown**:
  - Positive Sentiment % (e.g. 95% Positive).
  - Neutral Sentiment % (e.g. 3% Neutral).
  - Critical / Negative Sentiment % (e.g. 2% Critical).
- Identifies signature **Top Recommended Dishes** (e.g., Truffle Burrata Margherita, Wood-fired Quattro Formaggi).
- Highlights **Internet Review Consensus** (Pros & Cons, dietary tags, price tier).

### 5. 🌐 Universal Live Internet Search Fallback
- Enter **any arbitrary query** into the search bar (e.g., *"Mechanical keyboard"*, *"Tokyo Tower"*, *"Christopher Nolan sci-fi"*, *"Authentic Ramen"*).
- The system automatically detects query intent, queries live web APIs, structures metadata, and serves complete recommendation cards with comparisons, download links, route navigation, or reviews!

---

## 🏗️ Architecture & Technical Stack

```
d:\Project Java\AI based recomendation\
├── src\
│   └── com\
│       └── guvi\
│           └── recommend\
│               ├── Main.java                      # Virtual-Thread HTTP Server & Bootstrapper
│               ├── config\
│               │   └── SystemConfig.java          # Admin Algorithm Parameters & Weight Normalizer
│               ├── model\
│               │   ├── User.java                  # User Profiles & Preferences
│               │   ├── Item.java                  # Unified Catalog Item Entity
│               │   ├── Interaction.java           # Behavioral Analytics Logger (Clicks, Views)
│               │   ├── ProductComparison.java     # Multi-store Comparative Pricing
│               │   ├── MovieDetails.java          # Streaming Platforms & Download Links
│               │   ├── RouteResult.java           # Shortest Path & GeoJSON Coordinates
│               │   ├── FoodReview.java            # Review Sentiment & Top Dishes
│               │   └── RecommendationMetrics.java # CTR, Precision, Recall & KPI Analytics
│               ├── service\
│               │   ├── RecommendationEngine.java  # Hybrid AI Recommendation Pipeline
│               │   ├── ComparisonService.java     # E-Commerce Scraper & Comparison Engine
│               │   ├── MovieService.java          # Movie Provider & Download Link Dispatcher
│               │   ├── RoutingService.java        # OSRM Geodesic Routing & Geocoding
│               │   ├── FoodService.java           # Review Aggregator & Sentiment Analyzer
│               │   ├── InternetSearchService.java # Omnichannel Live Web Synthesis
│               │   └── UserService.java           # User Profiles & Analytics Engine
│               ├── web\
│               │   ├── ApiHandler.java            # REST API Controller & Router
│               │   └── StaticFileHandler.java     # Fast MIME-aware Static File Server
│               └── util\
│                   └── JsonUtil.java              # Zero-Dependency Pure Java JSON Engine
├── web\
│   ├── index.html                                 # Single Page Application Web Dashboard
│   ├── css\
│   │   └── style.css                              # Glassmorphic Modern Dark Theme
│   └── js\
│       └── app.js                                 # Client-side Logic, Chart.js & Leaflet Maps
├── run.bat                                        # 1-Click Windows Batch Runner
├── run.ps1                                        # PowerShell Execution Script
└── README.md                                      # Documentation & Assignment Specifications
```

- **Backend**: Pure Java 25 LTS Standard Library (`jdk.httpserver`, `java.net.http.HttpClient`, Java Virtual Threads via `Executors.newVirtualThreadPerTaskExecutor()`). Zero external Maven/Gradle jar dependencies required!
- **Frontend**: Responsive Single Page App (Tailwind CSS, Font Awesome 6, Chart.js, Leaflet.js OpenStreetMap).
- **Embedded REST API**: 14 REST endpoints serving JSON.

---

## 🚀 How to Run the Application

### Option 1: 1-Click Windows Batch File (Recommended)
Simply double-click or run from command prompt:
```bat
run.bat
```
*This automatically compiles all Java source files, launches the server, and opens your default browser at `http://localhost:8080/`.*

### Option 2: PowerShell Script
```powershell
.\run.ps1
```

### Option 3: Manual Compilation & Execution
```powershell
# 1. Compile Java files into bin directory
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)

# 2. Start the embedded server on port 8080
java -cp bin com.guvi.recommend.Main 8080
```
Open **[http://localhost:8080/](http://localhost:8080/)** in any web browser.

---

## 👥 Default User Profiles for Testing

| Profile | Role | Stated Interests | Location Anchor |
| :--- | :--- | :--- | :--- |
| **Alice** | `USER` | Tech Products, Sci-Fi Movies & Thrillers | Paris, France |
| **Bob** | `USER` | World Wonders, Travel Destinations, Vegetarian Food | Louvre Museum, Paris |
| **Charlie** | `USER` | Electronics Bargains, Comfort Food, Movies | New York, USA |
| **Admin** | `ADMIN` | Full System Access (Settings Configuration & Analytics) | Global System |

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/health` | Healthcheck and timestamp |
| `GET` | `/api/recommendations?userId={id}&category={cat}` | Fetches personalized AI recommendations |
| `GET` | `/api/search?q={query}&userId={id}` | Universal search across products, movies, places, food & web |
| `GET` | `/api/compare?itemId={id}` | Multi-store product price comparison |
| `GET` | `/api/movie?itemId={id}` | Direct download mirrors & streaming platform links |
| `GET` | `/api/route?origin={orig}&destination={dest}` | Shortest path route, duration, distance & coordinates |
| `GET` | `/api/food?itemId={id}` | Aggregated internet reviews, sentiment scores & top dishes |
| `POST` | `/api/interaction` | Logs user interaction (CLICK, VIEW, PURCHASE, RATE) |
| `GET` | `/api/user/{id}` | Fetches user profile and preferences |
| `POST` | `/api/user/{id}/preferences` | Updates user preferences and invalidates cache |
| `GET` | `/api/admin/settings` | Retrieves current algorithm parameters |
| `POST` | `/api/admin/settings` | Updates recommendation weights and criteria |
| `POST` | `/api/admin/reset` | Resets algorithm parameters to default |
| `GET` | `/api/admin/analytics` | Generates effectiveness reports, CTR, Precision & AI advice |

---

## 🏆 Project Rubric & Guvi Evaluation Compliance Checklist

- [x] **System Settings Management Form**: Allows admin to adjust recommendation criteria & algorithm parameters with instant confirmation.
- [x] **Recommendation Effectiveness Analysis**: Performance reports, KPI statistics, interactive charts (CTR by category, timeline), and algorithmic improvement advice.
- [x] **Personalized User Feed**: Uses hybrid collaborative and content-based AI matching.
- [x] **Preference Management Form**: Allows users to manage preferences for enhanced personalization.
- [x] **Product Cross-Site Comparison**: Multi-store comparative pricing with direct links.
- [x] **Direct Movie Download Links**: Direct mirrors and official streaming platforms.
- [x] **Shortest Path Place Routing**: Real-time turn-by-turn routing with distance, time, and interactive map.
- [x] **Food Review & Sentiment Aggregator**: Sentiment analysis percentages and signature dishes.
- [x] **Omnichannel Internet Search**: Real-time web synthesis for any query.
