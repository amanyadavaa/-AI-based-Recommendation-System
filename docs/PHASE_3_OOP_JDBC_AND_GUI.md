# Phase 3: OOP Architecture, JDBC Database Operations, and Swing Desktop GUI

## 1. Overview & Academic Alignment

Phase 3 implements the comprehensive academic and engineering requirements specified in the GUVI Assignment #69 syllabus:
- **OOP Implementation**: Polymorphism, Inheritance, Custom Exception Handling, Interfaces.
- **Collections & Generics**: Generic Data Access Object pattern (`Repository<T, ID>`), Type-safe collections, Top-K heuristics.
- **Multithreading & Synchronization**: Thread-safe Singleton Connection Manager, `ReentrantLock` concurrency, `SwingWorker` non-blocking UI background threads.
- **Database Operations & JDBC Connectivity**: Native SQLite JDBC driver integration, automated schema generation, parameterized `PreparedStatement` queries, DAO architectural pattern.
- **Java GUI-Based Project**: Full-featured Swing desktop application with 8 operational tabs, custom `Graphics2D` rendering components (`SentimentBarComponent`, `AnalyticsChartPanel`).

---

## 2. Object-Oriented Programming (OOP) Architecture

```
                          «interface»
                          Identifiable
                               ▲
                               │
                           BaseEntity
                               ▲
                               │
                      AbstractCatalogItem
                    [calculateCategoryAffinity()]
                               ▲
        ┌──────────────┬───────┴────────┬──────────────┐
        │              │                │              │
   ProductItem     MovieItem        PlaceItem       FoodItem
```

### 2.1 Interfaces & Inheritance
1. **`Identifiable` Interface** (`com.guvi.recommend.model.Identifiable`):
   - Contract defining unique string identity `String getId()` and `void setId(String id)`.
2. **`BaseEntity`** (`com.guvi.recommend.model.BaseEntity`):
   - Abstract root entity encapsulating UUID generation, timestamp tracking (`createdAt`), and identity implementation.
3. **`AbstractCatalogItem`** (`com.guvi.recommend.model.AbstractCatalogItem`):
   - Abstract catalog model unifying `title`, `category`, `description`, `rating`, `reviewCount`, `imageUrl`, `tags`, `price`, `metadata`, and `matchScore`.
   - Implements `Comparable<AbstractCatalogItem>` for automatic descending ranking sort.

### 2.2 Polymorphism
The abstract method `public abstract double calculateCategoryAffinity(User user)` is defined in `AbstractCatalogItem` and overridden with domain-specific algorithms across concrete subclasses:
- **`ProductItem`**: Matches user budget preferences (`LOW`: `<$50`, `MEDIUM`: `$50-$400`, `HIGH`: `>$400`) and category affinities.
- **`MovieItem`**: Evaluates intersection between user's preferred movie genres and movie tags.
- **`PlaceItem`**: Incorporates user travel mode and exploration preferences.
- **`FoodItem`**: Incorporates dietary preferences (`Vegan`, `Vegetarian`, `Keto`, `Halal`, `Gluten-Free`).

### 2.3 Exception Hierarchy
All application exceptions derive from `RecommendationException`:
- `RecommendationException`: Base checked exception.
- `ItemNotFoundException`: Thrown when querying nonexistent catalog entities.
- `UserNotFoundException`: Thrown when an unknown user profile is referenced.
- `DatabaseOperationException`: Wraps JDBC SQL errors and database connectivity failures.
- `InvalidParameterException`: Validates geocoordinates, search queries, and filter parameters.

---

## 3. Collections, Generics, and Multithreading

### 3.1 Generic DAO Interface (`Repository<T, ID>`)
```java
public interface Repository<T extends Identifiable, ID> {
    T save(T entity) throws DatabaseOperationException;
    Optional<T> findById(ID id) throws DatabaseOperationException;
    List<T> findAll() throws DatabaseOperationException;
    boolean deleteById(ID id) throws DatabaseOperationException;
    long count() throws DatabaseOperationException;
}
```

### 3.2 Thread-Safe Concurrency & Synchronization
1. **Double-Checked Locking Singleton** (`DatabaseConnectionManager`):
   - Utilizes `volatile` field instance and `ReentrantLock` for thread safety under high concurrency.
2. **Concurrent Caches**:
   - `ConcurrentHashMap` and `CopyOnWriteArrayList` guarantee lock-free read performance.
3. **`SwingWorker` Async UI Execution**:
   - Heavy network searches and routing API queries run off the Event Dispatch Thread (EDT) to eliminate UI freezes.

---

## 4. JDBC Database Operations (SQLite)

### 4.1 Schema Definition
When `DatabaseConnectionManager` starts, it automatically initializes the SQLite tables:
1. `users`: Stores user ID, username, email, role, JSON preferences, and creation timestamp.
2. `items`: Stores catalog ID, title, category, description, rating, review count, price, tags, and JSON metadata.
3. `interactions`: Stores user interaction telemetry (`CLICK`, `SEARCH`, `RATE`, `PURCHASE`), timestamp, and ratings.
4. `settings`: Stores dynamic recommendation engine hyperparameters.

### 4.2 Data Access Objects (DAOs)
- `UserDao`: Handles user retrieval, saving, and cache synchronization.
- `ItemDao`: Stores polymorphic items, provides `findByCategory(category)` filtering.
- `InteractionDao`: Records telemetry events with user activity lookups.

### 4.3 Web Integration & Database REST Endpoints (Servlets & Web Integration)
The web server directly integrates with the SQLite JDBC database via the DAO architecture, exposing:
- `GET /api/database/status`: Database health, driver status (`org.sqlite.JDBC`), and active table record counts.
- `GET /api/database/users`: Directly queries all registered users from SQLite.
- `GET /api/database/items`: Directly queries all catalog items across all categories from SQLite.
- `GET /api/database/interactions`: Queries real-time interaction event logs from SQLite.
- `POST /api/interaction`: Automatically saves recorded interaction telemetry into SQLite.

---

## 5. Java Desktop GUI Application (Swing)

The desktop application is located at `com.guvi.recommend.gui.OmniRecommendDesktopApp`.

### 5.1 Tab Layout
| Tab # | Name | Description |
|---|---|---|
| **Tab 1** | **Recommendations** | Personalized Top-K items with live score badges and preview cards |
| **Tab 2** | **Price Comparison** | Multi-store retailer grid (Amazon, Walmart, Best Buy, etc.) with savings |
| **Tab 3** | **Movies & Downloads** | Direct download links (Archive.org MP4, Torrent) & streaming platforms |
| **Tab 4** | **Routing & Navigation**| Shortest path calculator, turn-by-turn directions & Maps navigation |
| **Tab 5** | **Food & Reviews** | Review aggregation with tri-color sentiment bar and signature dishes |
| **Tab 6** | **AI Engine Tuning** | Dynamic parameter adjustment sliders (Content, Collab, Explore) |
| **Tab 7** | **Analytics Dashboard**| Live KPI metrics and custom 2D Graphics bar chart |
| **Tab 8** | **SQLite Inspector** | Direct database table viewer with row counts and reload |

### 5.2 Custom Graphical Components
- **`SentimentBarComponent`**: Custom `JComponent` utilizing `Graphics2D` antialiasing to render proportional positive (emerald), neutral (amber), and negative (rose) sentiment bars.
- **`AnalyticsChartPanel`**: Custom `JPanel` rendering bar charts of category click-through rates and performance metrics.

---

## 6. Execution & Verification

### Running the Desktop GUI:
- **Batch**: `run-gui.bat`
- **PowerShell**: `.\run-gui.ps1`

### Running the Full Test Suite:
- **Batch**: `test.bat`
- **PowerShell**: `.\test.ps1`

### Phase 3 Test Suite Results:
```
==================================================================
    RUNNING PHASE 3 OOP, JDBC & DESKTOP GUI TEST SUITE            
==================================================================
  [PASS] Sub-phase 3.1: Polymorphic Affinity Scoring (AbstractCatalogItem)
  [PASS] Sub-phase 3.1: Inheritance Hierarchy & Identifiable Interface Contract
  [PASS] Sub-phase 3.1: Custom Exception Hierarchy & Error Propagation
  [PASS] Sub-phase 3.2: Generics & Collections Repository Pattern (Repository<T, ID>)
  [PASS] Sub-phase 3.2: Multithreading & Thread-Safe Singleton Concurrency
  [PASS] Sub-phase 3.2: SQLite JDBC Connection & Schema Initialization
  [PASS] Sub-phase 3.2: JDBC UserDao CRUD & SQLite Persistence
  [PASS] Sub-phase 3.2: JDBC ItemDao & Polymorphic Catalog Persistence
  [PASS] Sub-phase 3.2: JDBC InteractionDao Telemetry & Persistence
  [PASS] Sub-phase 3.3: Swing SentimentBar & Analytics Chart Calculations
  [PASS] Sub-phase 3.3: Desktop App Instantiation & Headless UI Safety
  [PASS] Sub-phase 3.3: Web Integration & JDBC End-to-End Data Persistence
==================================================================
PHASE 3 SUMMARY: 12 Passed, 0 Failed (Total 12)
==================================================================
✔ ALL SUB-PHASES OF PHASE 3 VERIFIED SUCCESSFULLY!
```
