package com.guvi.recommend.gui;

import com.guvi.recommend.config.SystemConfig;
import com.guvi.recommend.db.DatabaseConnectionManager;
import com.guvi.recommend.db.dao.InteractionDao;
import com.guvi.recommend.db.dao.ItemDao;
import com.guvi.recommend.db.dao.UserDao;
import com.guvi.recommend.gui.components.AnalyticsChartPanel;
import com.guvi.recommend.gui.components.SentimentBarComponent;
import com.guvi.recommend.model.*;
import com.guvi.recommend.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * OmniRecommendDesktopApp - Full-featured Java Desktop GUI Application (Swing)
 * Demonstrating OOP, Polymorphism, Exception Handling, Collections, Multithreading & JDBC.
 */
public class OmniRecommendDesktopApp extends JFrame {

    private final SystemConfig config;
    private final UserService userService;
    private final ComparisonService comparisonService;
    private final MovieService movieService;
    private final RoutingService routingService;
    private final FoodService foodService;
    private final InternetSearchService internetSearchService;
    private final RecommendationEngine recommendationEngine;

    // JDBC DAOs
    private final DatabaseConnectionManager dbManager;
    private final UserDao userDao;
    private final ItemDao itemDao;
    private final InteractionDao interactionDao;

    // UI State
    private String activeUserId = "usr-alice";
    private JComboBox<String> userCombo;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JTabbedPane tabbedPane;

    // Tab 1: Recommendations
    private DefaultListModel<Item> recListModel;
    private JList<Item> recList;
    private JLabel previewTitleLabel;
    private JLabel previewCategoryLabel;
    private JLabel previewScoreLabel;
    private JTextArea previewDescArea;
    private JButton actionBtn1, actionBtn2;

    // Tab 2: Comparison
    private DefaultTableModel compTableModel;
    private JLabel compSummaryLabel;

    // Tab 3: Movies
    private DefaultListModel<String> downloadListModel;
    private DefaultListModel<String> streamListModel;
    private JLabel movieTitleLabel;
    private JLabel movieRatingLabel;

    // Tab 4: Routing
    private JTextField originField;
    private JTextField destField;
    private JLabel routeDistanceLabel;
    private JLabel routeDurationLabel;
    private DefaultListModel<String> routeStepsModel;
    private String currentNavUrl = "";

    // Tab 5: Food
    private JLabel foodTitleLabel;
    private SentimentBarComponent sentimentBar;
    private JLabel sentimentTextLabel;
    private DefaultListModel<String> foodDishesModel;
    private DefaultListModel<String> foodProsModel;

    // Tab 6: Admin Settings
    private JSlider contentSlider, collabSlider, popSlider, exploreSlider, thresholdSlider;
    private JLabel contentValLbl, collabValLbl, popValLbl, exploreValLbl, thresholdValLbl;

    // Tab 7: Analytics
    private JLabel kpiImpressionsLbl, kpiClicksLbl, kpiCtrLbl, kpiConvLbl;
    private AnalyticsChartPanel chartPanel;
    private JTextArea aiTipsArea;

    // Tab 8: Database (JDBC)
    private DefaultTableModel dbTableModel;
    private JLabel dbStatusLabel;
    private JComboBox<String> dbTableSelect;

    public OmniRecommendDesktopApp() {
        setTitle("OmniRecommend AI - Universal Recommendation & Comparison Platform (GUVI #69)");
        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize Services & Database
        this.config = new SystemConfig();
        this.userService = new UserService();
        this.comparisonService = new ComparisonService();
        this.movieService = new MovieService();
        this.routingService = new RoutingService();
        this.foodService = new FoodService();
        this.internetSearchService = new InternetSearchService(comparisonService, movieService, routingService, foodService);
        this.recommendationEngine = new RecommendationEngine(config, userService, comparisonService, movieService, routingService, foodService, internetSearchService);

        this.dbManager = DatabaseConnectionManager.getInstance();
        this.userDao = new UserDao();
        this.itemDao = new ItemDao();
        this.interactionDao = new InteractionDao();

        // Seed initial items into JDBC
        syncDatabase();

        initUI();
        refreshRecommendations();
        loadAnalyticsData();
        loadDbTableData("users");
    }

    private void syncDatabase() {
        try {
            for (User u : userService.getAllUsers()) userDao.save(u);
            for (Item it : recommendationEngine.getItemCatalog().values()) itemDao.save(it);
            for (Interaction in : userService.getAllInteractions()) interactionDao.save(in);
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Top Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Tabbed Navigation
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabbedPane.addTab("🌟 Feed & Omni-Search", createRecommendationsTab());
        tabbedPane.addTab("🛍️ Multi-Store Comparison", createComparisonTab());
        tabbedPane.addTab("🎬 Movies & Downloads", createMoviesTab());
        tabbedPane.addTab("📍 Travel & Shortest Path", createRoutingTab());
        tabbedPane.addTab("🍔 Food & Reviews", createFoodTab());
        tabbedPane.addTab("⚙️ Admin Settings", createAdminSettingsTab());
        tabbedPane.addTab("📊 Effectiveness Analytics", createAnalyticsTab());
        tabbedPane.addTab("💾 Database Management (JDBC)", createDatabaseTab());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        JLabel titleLbl = new JLabel("OmniRecommend AI");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JLabel badgeLbl = new JLabel(" GUVI Project #69 ");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(new Color(99, 102, 241));
        badgeLbl.setBorder(BorderFactory.createLineBorder(new Color(99, 102, 241)));

        brandPanel.add(titleLbl);
        brandPanel.add(badgeLbl);

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);

        JLabel userLbl = new JLabel("Active User:");
        userLbl.setForeground(new Color(148, 163, 184));
        userCombo = new JComboBox<>(new String[]{"Alice (Tech/Movies)", "Bob (Travel/Food)", "Charlie (Bargains)", "Admin"});
        userCombo.addActionListener(e -> {
            int idx = userCombo.getSelectedIndex();
            activeUserId = (idx == 0) ? "usr-alice" : (idx == 1 ? "usr-bob" : (idx == 2 ? "usr-charlie" : "usr-admin"));
            refreshRecommendations();
            loadAnalyticsData();
        });

        // Search Bar
        searchField = new JTextField(18);
        searchField.setText("Sony WH-1000XM5");
        categoryCombo = new JComboBox<>(new String[]{"ALL", "PRODUCT", "MOVIE", "PLACE", "FOOD"});

        JButton searchBtn = new JButton("Search Internet");
        searchBtn.setBackground(new Color(99, 102, 241));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> triggerSearch());

        controlsPanel.add(userLbl);
        controlsPanel.add(userCombo);
        controlsPanel.add(searchField);
        controlsPanel.add(categoryCombo);
        controlsPanel.add(searchBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(controlsPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createRecommendationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Split Pane: Left List, Right Preview
        recListModel = new DefaultListModel<>();
        recList = new JList<>(recListModel);
        recList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Item it) {
                    lbl.setText(String.format("[%s] %s  —  ★%.1f  (%.0f%% Match)", it.getCategory(), it.getTitle(), it.getRating(), it.getMatchScore() * 100));
                    lbl.setBorder(new EmptyBorder(6, 6, 6, 6));
                }
                return lbl;
            }
        });

        recList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Item it = recList.getSelectedValue();
                if (it != null) updatePreview(it);
            }
        });

        JScrollPane listScroll = new JScrollPane(recList);
        listScroll.setPreferredSize(new Dimension(480, 500));

        // Right Preview Card
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel metaHeader = new JPanel(new GridLayout(3, 1, 4, 4));
        previewTitleLabel = new JLabel("Select an item to view recommendation details");
        previewTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewCategoryLabel = new JLabel("Category: -");
        previewScoreLabel = new JLabel("AI Match Confidence: -");
        metaHeader.add(previewTitleLabel);
        metaHeader.add(previewCategoryLabel);
        metaHeader.add(previewScoreLabel);

        previewDescArea = new JTextArea();
        previewDescArea.setWrapStyleWord(true);
        previewDescArea.setLineWrap(true);
        previewDescArea.setEditable(false);
        previewDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionBtn1 = new JButton("Open Primary Action");
        actionBtn1.setBackground(new Color(16, 185, 129));
        actionBtn1.setForeground(Color.WHITE);
        actionBtn1.setEnabled(false);

        actionBtn2 = new JButton("View in Category Tab");
        actionBtn2.setEnabled(false);

        actionPanel.add(actionBtn1);
        actionPanel.add(actionBtn2);

        previewPanel.add(metaHeader, BorderLayout.NORTH);
        previewPanel.add(new JScrollPane(previewDescArea), BorderLayout.CENTER);
        previewPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, previewPanel);
        split.setDividerLocation(480);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void updatePreview(Item it) {
        previewTitleLabel.setText(it.getTitle());
        previewCategoryLabel.setText("Category: " + it.getCategory() + "  |  Rating: ★ " + it.getRating() + " (" + it.getReviewCount() + " reviews)");
        previewScoreLabel.setText(String.format("AI Match Confidence: %.0f%% (Hybrid Algorithm Weight)", it.getMatchScore() * 100));
        previewDescArea.setText(it.getDescription() + "\n\nTags: " + String.join(", ", it.getTags()));

        actionBtn1.setEnabled(true);
        actionBtn2.setEnabled(true);

        // Log VIEW interaction
        userService.recordInteraction(new Interaction(activeUserId, it.getId(), it.getCategory(), "VIEW", null));

        // Configure actions
        for (var al : actionBtn1.getActionListeners()) actionBtn1.removeActionListener(al);
        for (var al : actionBtn2.getActionListeners()) actionBtn2.removeActionListener(al);

        if ("PRODUCT".equals(it.getCategory())) {
            actionBtn1.setText("🛍️ Compare Across 6 Stores");
            actionBtn1.addActionListener(e -> {
                tabbedPane.setSelectedIndex(1);
                loadProductComparison(it.getTitle());
            });
        } else if ("MOVIE".equals(it.getCategory())) {
            actionBtn1.setText("🎬 Watch & Download Links");
            actionBtn1.addActionListener(e -> {
                tabbedPane.setSelectedIndex(2);
                loadMovieDetails(it.getTitle());
            });
        } else if ("PLACE".equals(it.getCategory())) {
            actionBtn1.setText("📍 Shortest Path & Route");
            actionBtn1.addActionListener(e -> {
                tabbedPane.setSelectedIndex(3);
                destField.setText(it.getTitle());
                calculateRoute();
            });
        } else if ("FOOD".equals(it.getCategory())) {
            actionBtn1.setText("🍔 Reviews & Sentiment Breakdown");
            actionBtn1.addActionListener(e -> {
                tabbedPane.setSelectedIndex(4);
                loadFoodReview(it.getTitle());
            });
        }

        actionBtn2.setText("Log Click Event");
        actionBtn2.addActionListener(e -> {
            userService.recordInteraction(new Interaction(activeUserId, it.getId(), it.getCategory(), "CLICK", null));
            JOptionPane.showMessageDialog(this, "Logged user click event for training the recommendation model!", "Interaction Recorded", JOptionPane.INFORMATION_MESSAGE);
            loadAnalyticsData();
        });
    }

    // --- Tab 2: Comparison ---
    private JPanel createComparisonTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField prodQueryField = new JTextField("Sony WH-1000XM5 Headphones", 25);
        JButton compareBtn = new JButton("Compare Across Retailers");
        compareBtn.addActionListener(e -> loadProductComparison(prodQueryField.getText().trim()));
        topBar.add(new JLabel("Product:"));
        topBar.add(prodQueryField);
        topBar.add(compareBtn);

        compSummaryLabel = new JLabel("Summary: Lowest Price: $367.99 at Walmart | Max Savings: $52.00 (12.4%)");
        compSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topBar.add(compSummaryLabel);

        compTableModel = new DefaultTableModel(new String[]{"Store / Retailer", "Price (USD)", "Rating", "Shipping & Delivery", "Stock Status", "Badge / Perk"}, 0);
        JTable table = new JTable(compTableModel);
        table.setRowHeight(24);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadProductComparison(String query) {
        ProductComparison comp = comparisonService.compareByQuery(query);
        compSummaryLabel.setText(String.format("Lowest Price: $%.2f at %s  |  Highest: $%.2f  |  Max Savings: $%.2f (%.1f%%)  |  %s",
                comp.getLowestPrice(), comp.getBestStore(), comp.getHighestPrice(), comp.getMaxSavings(), comp.getSavingsPercentage(), comp.getPriceTrend()));

        compTableModel.setRowCount(0);
        for (ProductComparison.StoreListing s : comp.getStoreListings()) {
            compTableModel.addRow(new Object[]{
                    s.getStoreName(),
                    String.format("$%.2f", s.getPrice()),
                    String.format("★ %.1f (%d reviews)", s.getRating(), s.getReviewCount()),
                    s.getShippingInfo(),
                    s.isInStock() ? "In Stock" : "Out of Stock",
                    s.getBadge()
            });
        }
    }

    // --- Tab 3: Movies ---
    private JPanel createMoviesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField movieQuery = new JTextField("Inception (2010)", 20);
        JButton loadMovieBtn = new JButton("Fetch Links");
        loadMovieBtn.addActionListener(e -> loadMovieDetails(movieQuery.getText().trim()));
        topBar.add(new JLabel("Movie Title:"));
        topBar.add(movieQuery);
        topBar.add(loadMovieBtn);

        movieTitleLabel = new JLabel("Inception (2010)");
        movieTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        movieRatingLabel = new JLabel("IMDb: 8.8  |  Rotten Tomatoes: 92%  |  Director: Christopher Nolan");

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 2, 2));
        headerBox.add(movieTitleLabel);
        headerBox.add(movieRatingLabel);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 10));

        // Download Links Panel
        downloadListModel = new DefaultListModel<>();
        JList<String> downList = new JList<>(downloadListModel);
        JPanel downPanel = new JPanel(new BorderLayout());
        downPanel.setBorder(BorderFactory.createTitledBorder("Verified Direct Download Mirrors (Legal Public Vaults)"));
        downPanel.add(new JScrollPane(downList), BorderLayout.CENTER);

        JButton launchDownloadBtn = new JButton("Open Direct Download URL");
        launchDownloadBtn.addActionListener(e -> openUrl("https://archive.org/details/feature_films"));
        downPanel.add(launchDownloadBtn, BorderLayout.SOUTH);

        // Streaming Platforms Panel
        streamListModel = new DefaultListModel<>();
        JList<String> streamList = new JList<>(streamListModel);
        JPanel streamPanel = new JPanel(new BorderLayout());
        streamPanel.setBorder(BorderFactory.createTitledBorder("Official Streaming Providers"));
        streamPanel.add(new JScrollPane(streamList), BorderLayout.CENTER);

        JButton launchStreamBtn = new JButton("Watch on Netflix / Prime");
        launchStreamBtn.addActionListener(e -> openUrl("https://www.netflix.com/"));
        streamPanel.add(launchStreamBtn, BorderLayout.SOUTH);

        content.add(downPanel);
        content.add(streamPanel);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(headerBox, BorderLayout.NORTH);
        centerPanel.add(content, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void loadMovieDetails(String query) {
        MovieDetails movie = movieService.getMovieDetailsByTitle(query);
        movieTitleLabel.setText(movie.getTitle() + " (" + movie.getYear() + ") - " + movie.getDuration());
        movieRatingLabel.setText("IMDb: " + movie.getImdbRating() + "  |  Rotten Tomatoes: " + movie.getRottenTomatoesScore() + "%  |  Director: " + movie.getDirector());

        downloadListModel.clear();
        for (var d : movie.getDownloadLinks()) {
            downloadListModel.addElement(String.format("📁 %s  [%s - %s] — Direct URL Ready", d.toMap().get("sourceName"), d.toMap().get("quality"), d.toMap().get("size")));
        }

        streamListModel.clear();
        for (var s : movie.getStreamingPlatforms()) {
            streamListModel.addElement(String.format("▶ %s  [%s]  Price: %s", s.toMap().get("platformName"), s.toMap().get("streamType"), s.toMap().get("price")));
        }
    }

    // --- Tab 4: Routing ---
    private JPanel createRoutingTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        originField = new JTextField("Notre Dame, Paris", 15);
        destField = new JTextField("Eiffel Tower, Paris", 15);
        JButton routeBtn = new JButton("Calculate Shortest Route");
        routeBtn.addActionListener(e -> calculateRoute());

        topBar.add(new JLabel("Origin:"));
        topBar.add(originField);
        topBar.add(new JLabel("Destination:"));
        topBar.add(destField);
        topBar.add(routeBtn);

        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        routeDistanceLabel = new JLabel("Distance: 5.2 km");
        routeDistanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        routeDurationLabel = new JLabel("Driving Duration: 14 min");
        routeDurationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton gmapsBtn = new JButton("Open in Google Maps GPS");
        gmapsBtn.setBackground(new Color(245, 158, 11));
        gmapsBtn.setForeground(Color.BLACK);
        gmapsBtn.addActionListener(e -> {
            if (!currentNavUrl.isEmpty()) openUrl(currentNavUrl);
            else openUrl("https://maps.google.com");
        });

        metricsPanel.add(routeDistanceLabel);
        metricsPanel.add(routeDurationLabel);
        metricsPanel.add(gmapsBtn);

        routeStepsModel = new DefaultListModel<>();
        JList<String> stepsList = new JList<>(routeStepsModel);
        JScrollPane scroll = new JScrollPane(stepsList);
        scroll.setBorder(BorderFactory.createTitledBorder("Turn-by-Turn Navigation Maneuvers (OSRM Geodesic Engine)"));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(metricsPanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void calculateRoute() {
        String orig = originField.getText().trim();
        String dest = destField.getText().trim();

        // Use SwingWorker for background multithreaded calculation
        new SwingWorker<RouteResult, Void>() {
            @Override
            protected RouteResult doInBackground() {
                return routingService.calculateShortestRoute(orig, dest);
            }

            @Override
            protected void done() {
                try {
                    RouteResult res = get();
                    routeDistanceLabel.setText(String.format("Distance: %.1f km", res.getDistanceKm()));
                    routeDurationLabel.setText("Driving Duration: " + res.getFormattedDuration());
                    currentNavUrl = res.getNavigationUrl();

                    routeStepsModel.clear();
                    for (int i = 0; i < res.getSteps().size(); i++) {
                        routeStepsModel.addElement((i + 1) + ". " + res.getSteps().get(i).toMap().get("instruction"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(OmniRecommendDesktopApp.this, "Error calculating route: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // --- Tab 5: Food ---
    private JPanel createFoodTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField foodQuery = new JTextField("L'Antica Pizzeria Margherita", 20);
        JButton loadFoodBtn = new JButton("Analyze Internet Reviews");
        loadFoodBtn.addActionListener(e -> loadFoodReview(foodQuery.getText().trim()));
        topBar.add(new JLabel("Food / Restaurant:"));
        topBar.add(foodQuery);
        topBar.add(loadFoodBtn);

        foodTitleLabel = new JLabel("Neapolitan Pizza Margherita");
        foodTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        sentimentBar = new SentimentBarComponent();
        sentimentTextLabel = new JLabel("Sentiment: 95% Positive | 2% Neutral | 3% Critical (28,000+ reviews)");

        JPanel sentimentPanel = new JPanel(new BorderLayout(5, 5));
        sentimentPanel.setBorder(BorderFactory.createTitledBorder("NLP Sentiment Analysis (Aggregated Google, Yelp, TripAdvisor)"));
        sentimentPanel.add(sentimentBar, BorderLayout.NORTH);
        sentimentPanel.add(sentimentTextLabel, BorderLayout.SOUTH);

        JPanel details = new JPanel(new GridLayout(1, 2, 10, 10));

        foodDishesModel = new DefaultListModel<>();
        JList<String> dishList = new JList<>(foodDishesModel);
        JScrollPane dishScroll = new JScrollPane(dishList);
        dishScroll.setBorder(BorderFactory.createTitledBorder("Top Recommended Signature Dishes"));

        foodProsModel = new DefaultListModel<>();
        JList<String> prosList = new JList<>(foodProsModel);
        JScrollPane prosScroll = new JScrollPane(prosList);
        prosScroll.setBorder(BorderFactory.createTitledBorder("Verified Customer Review Consensus (Pros & Perks)"));

        details.add(dishScroll);
        details.add(prosScroll);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(foodTitleLabel, BorderLayout.NORTH);
        centerPanel.add(sentimentPanel, BorderLayout.CENTER);
        centerPanel.add(details, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void loadFoodReview(String query) {
        FoodReview review = foodService.getFoodReviewByName(query);
        foodTitleLabel.setText(review.getName() + "  —  ★ " + review.getOverallRating() + " (" + review.getCuisine() + " • " + review.getPriceRange() + ")");
        sentimentBar.setPercentages(review.getPositivePercent(), review.getNeutralPercent(), review.getNegativePercent());
        sentimentTextLabel.setText(String.format("Sentiment: %d%% Positive  |  %d%% Neutral  |  %d%% Critical  (%d+ internet reviews)",
                review.getPositivePercent(), review.getNeutralPercent(), review.getNegativePercent(), review.getTotalReviews()));

        foodDishesModel.clear();
        for (String d : review.getBestRecommendedDishes()) foodDishesModel.addElement("🍽️ " + d);

        foodProsModel.clear();
        for (String p : review.getPros()) foodProsModel.addElement("✔ " + p);
    }

    // --- Tab 6: Admin Settings ---
    private JPanel createAdminSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridLayout(5, 3, 10, 15));
        form.setBorder(BorderFactory.createTitledBorder("Multi-Objective Recommendation Parameters (Admin Form)"));

        // Sliders
        contentSlider = createSlider(40);
        contentValLbl = new JLabel("0.40");
        contentSlider.addChangeListener(e -> contentValLbl.setText(String.format("%.2f", contentSlider.getValue() / 100.0)));
        form.add(new JLabel("Content-Based Weight (W_content):"));
        form.add(contentSlider);
        form.add(contentValLbl);

        collabSlider = createSlider(35);
        collabValLbl = new JLabel("0.35");
        collabSlider.addChangeListener(e -> collabValLbl.setText(String.format("%.2f", collabSlider.getValue() / 100.0)));
        form.add(new JLabel("Collaborative Filtering Weight (W_collab):"));
        form.add(collabSlider);
        form.add(collabValLbl);

        popSlider = createSlider(25);
        popValLbl = new JLabel("0.25");
        popSlider.addChangeListener(e -> popValLbl.setText(String.format("%.2f", popSlider.getValue() / 100.0)));
        form.add(new JLabel("Popularity / Trend Weight (W_pop):"));
        form.add(popSlider);
        form.add(popValLbl);

        exploreSlider = createSlider(15);
        exploreValLbl = new JLabel("0.15");
        exploreSlider.addChangeListener(e -> exploreValLbl.setText(String.format("%.2f", exploreSlider.getValue() / 100.0)));
        form.add(new JLabel("Exploration Rate (ε-greedy Serendipity):"));
        form.add(exploreSlider);
        form.add(exploreValLbl);

        thresholdSlider = createSlider(40);
        thresholdValLbl = new JLabel("0.40");
        thresholdSlider.addChangeListener(e -> thresholdValLbl.setText(String.format("%.2f", thresholdSlider.getValue() / 100.0)));
        form.add(new JLabel("Minimum Confidence Score Threshold:"));
        form.add(thresholdSlider);
        form.add(thresholdValLbl);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = new JButton("Save Settings to Database (JDBC)");
        saveBtn.setBackground(new Color(99, 102, 241));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveAdminSettings());

        JButton resetBtn = new JButton("Reset to Defaults");
        resetBtn.addActionListener(e -> {
            config.resetToDefaults();
            updateSliderValues();
            JOptionPane.showMessageDialog(this, "Reset to algorithm defaults!", "Settings Reset", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(resetBtn);
        btnPanel.add(saveBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JSlider createSlider(int initial) {
        JSlider s = new JSlider(0, 100, initial);
        s.setMajorTickSpacing(25);
        s.setPaintTicks(true);
        return s;
    }

    private void updateSliderValues() {
        contentSlider.setValue((int) (config.getContentWeight() * 100));
        collabSlider.setValue((int) (config.getCollaborativeWeight() * 100));
        popSlider.setValue((int) (config.getPopularityWeight() * 100));
        exploreSlider.setValue((int) (config.getExplorationRate() * 100));
        thresholdSlider.setValue((int) (config.getMinConfidenceThreshold() * 100));
    }

    private void saveAdminSettings() {
        Map<String, Object> map = Map.of(
                "contentWeight", contentSlider.getValue() / 100.0,
                "collaborativeWeight", collabSlider.getValue() / 100.0,
                "popularityWeight", popSlider.getValue() / 100.0,
                "explorationRate", exploreSlider.getValue() / 100.0,
                "minConfidenceThreshold", thresholdSlider.getValue() / 100.0
        );
        config.updateFromMap(map, "Admin");
        refreshRecommendations();
        loadAnalyticsData();
        JOptionPane.showMessageDialog(this, "Algorithm settings updated successfully and persisted to SQLite via JDBC!", "Admin Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Tab 7: Analytics ---
    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        kpiPanel.setBorder(BorderFactory.createTitledBorder("Key Recommendation Effectiveness Metrics"));

        kpiImpressionsLbl = new JLabel("Impressions: 150", SwingConstants.CENTER);
        kpiClicksLbl = new JLabel("Clicks: 33", SwingConstants.CENTER);
        kpiCtrLbl = new JLabel("Overall CTR: 22.0%", SwingConstants.CENTER);
        kpiConvLbl = new JLabel("Conversion: 15.2%", SwingConstants.CENTER);

        for (JLabel l : List.of(kpiImpressionsLbl, kpiClicksLbl, kpiCtrLbl, kpiConvLbl)) {
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            kpiPanel.add(l);
        }

        chartPanel = new AnalyticsChartPanel();

        aiTipsArea = new JTextArea();
        aiTipsArea.setEditable(false);
        aiTipsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aiTipsArea.setBorder(BorderFactory.createTitledBorder("AI Algorithmic Improvement Suggestions"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, new JScrollPane(aiTipsArea));
        split.setDividerLocation(260);

        panel.add(kpiPanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void loadAnalyticsData() {
        RecommendationMetrics m = userService.calculateMetrics(config);
        kpiImpressionsLbl.setText("Impressions: " + m.getTotalImpressions());
        kpiClicksLbl.setText("Clicks: " + m.getTotalClicks());
        kpiCtrLbl.setText(String.format("Overall CTR: %.1f%%", m.getOverallCtr()));
        kpiConvLbl.setText(String.format("Conversion: %.1f%%", m.getConversionRate()));

        Map<String, Double> chartData = new java.util.LinkedHashMap<>();
        for (var e : m.getCategoryMetrics().entrySet()) {
            chartData.put(e.getKey(), e.getValue().getCtr());
        }
        chartPanel.updateData(chartData);

        aiTipsArea.setText(String.join("\n\n• ", m.getAiOptimizationSuggestions()));
    }

    // --- Tab 8: Database ---
    private JPanel createDatabaseTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dbTableSelect = new JComboBox<>(new String[]{"users", "items", "interactions"});
        dbTableSelect.addActionListener(e -> loadDbTableData((String) dbTableSelect.getSelectedItem()));

        JButton refreshDbBtn = new JButton("Refresh Table via JDBC");
        refreshDbBtn.addActionListener(e -> loadDbTableData((String) dbTableSelect.getSelectedItem()));

        dbStatusLabel = new JLabel("JDBC Status: " + (dbManager.isJdbcAvailable() ? "Connected to SQLite (" + dbManager.getDbUrl() + ")" : "Memory-Sync Mode"));
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        topBar.add(new JLabel("Database Table:"));
        topBar.add(dbTableSelect);
        topBar.add(refreshDbBtn);
        topBar.add(dbStatusLabel);

        dbTableModel = new DefaultTableModel();
        JTable table = new JTable(dbTableModel);
        table.setRowHeight(22);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadDbTableData(String tableName) {
        try {
            dbTableModel.setRowCount(0);
            dbTableModel.setColumnCount(0);

            if ("users".equalsIgnoreCase(tableName)) {
                dbTableModel.addColumn("User ID");
                dbTableModel.addColumn("Username");
                dbTableModel.addColumn("Email");
                dbTableModel.addColumn("Role");
                dbTableModel.addColumn("Preferences");
                for (User u : userDao.findAll()) {
                    dbTableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.getPreferences().toString()});
                }
            } else if ("items".equalsIgnoreCase(tableName)) {
                dbTableModel.addColumn("Item ID");
                dbTableModel.addColumn("Title");
                dbTableModel.addColumn("Category");
                dbTableModel.addColumn("Rating");
                dbTableModel.addColumn("Price");
                for (Item it : itemDao.findAll()) {
                    dbTableModel.addRow(new Object[]{it.getId(), it.getTitle(), it.getCategory(), it.getRating(), it.getPrice()});
                }
            } else if ("interactions".equalsIgnoreCase(tableName)) {
                dbTableModel.addColumn("ID");
                dbTableModel.addColumn("User ID");
                dbTableModel.addColumn("Item ID");
                dbTableModel.addColumn("Category");
                dbTableModel.addColumn("Type");
                for (Interaction in : interactionDao.findAll()) {
                    dbTableModel.addRow(new Object[]{in.getId(), in.getUserId(), in.getItemId(), in.getItemCategory(), in.getType()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "JDBC Query error: " + ex.getMessage());
        }
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(15, 23, 42));
        status.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel statusLbl = new JLabel("OmniRecommend Desktop Suite • Pure Java 25 & Swing • Embedded SQLite JDBC Connection Pool Active");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(new Color(100, 116, 139));
        status.add(statusLbl, BorderLayout.WEST);
        return status;
    }

    private void triggerSearch() {
        String q = searchField.getText().trim();
        String cat = (String) categoryCombo.getSelectedItem();
        List<Item> results = recommendationEngine.search(q, activeUserId, cat);
        recListModel.clear();
        for (Item it : results) recListModel.addElement(it);
        if (!results.isEmpty()) recList.setSelectedIndex(0);
    }

    private void refreshRecommendations() {
        String cat = (String) categoryCombo.getSelectedItem();
        List<Item> items = recommendationEngine.getPersonalizedRecommendations(activeUserId, cat);
        recListModel.clear();
        for (Item it : items) recListModel.addElement(it);
        if (!items.isEmpty()) recList.setSelectedIndex(0);
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open URL: " + url + "\nError: " + ex.getMessage());
        }
    }

    public int getTabCount() {
        return tabbedPane != null ? tabbedPane.getTabCount() : 0;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            OmniRecommendDesktopApp app = new OmniRecommendDesktopApp();
            app.setVisible(true);
        });
    }
}
