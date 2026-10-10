package com.guvi.recommend.db;

import com.guvi.recommend.exception.DatabaseOperationException;

import java.sql.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Singleton Connection Manager handling JDBC database connectivity.
 */
public class DatabaseConnectionManager {
    private static volatile DatabaseConnectionManager instance;
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:recommendation.db";
    private static final ReentrantLock lock = new ReentrantLock();

    private final String dbUrl;
    private boolean isJdbcAvailable = false;

    private DatabaseConnectionManager(String dbUrl) {
        this.dbUrl = dbUrl;
        initDriverAndTables();
    }

    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new DatabaseConnectionManager(DEFAULT_DB_URL);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    private void initDriverAndTables() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.isJdbcAvailable = true;
            createTables();
            System.out.println("✔ JDBC Database Initialized: Connected to SQLite at " + dbUrl);
        } catch (ClassNotFoundException e) {
            System.out.println("ℹ SQLite JDBC Driver not loaded in current classpath. Database running in memory-sync mode.");
            this.isJdbcAvailable = false;
        } catch (SQLException e) {
            System.err.println("⚠ Failed to initialize database schema: " + e.getMessage());
            this.isJdbcAvailable = false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    email TEXT,
                    role TEXT,
                    preferences TEXT,
                    created_at INTEGER
                );
            """);

            // 2. Items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    rating REAL,
                    review_count INTEGER,
                    image_url TEXT,
                    tags TEXT,
                    price REAL,
                    metadata TEXT
                );
            """);

            // 3. Interactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS interactions (
                    id TEXT PRIMARY KEY,
                    user_id TEXT NOT NULL,
                    item_id TEXT NOT NULL,
                    item_category TEXT,
                    type TEXT NOT NULL,
                    rating_value REAL,
                    timestamp INTEGER
                );
            """);

            // 4. System Settings table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL,
                    updated_at INTEGER
                );
            """);
        }
    }

    public Connection getConnection() throws SQLException {
        if (!isJdbcAvailable) {
            throw new SQLException("JDBC Driver not active");
        }
        return DriverManager.getConnection(dbUrl);
    }

    public boolean isJdbcAvailable() {
        return isJdbcAvailable;
    }

    public String getDbUrl() {
        return dbUrl;
    }
}
