package com.college.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseConnection {
    private static final AtomicReference<Connection> connectionRef = new AtomicReference<>();
    private static final DatabaseCache cache = DatabaseCache.getInstance();
    private static final AppConfig config = AppConfig.getInstance();

    public static Connection getConnection() throws SQLException {
        Connection conn = connectionRef.get();
        
        if (conn != null && !conn.isClosed()) {
            return conn;
        }

        try {
            String url = String.format("jdbc:mysql://%s:%d/%s",
                config.getDbHost(),
                config.getDbPort(),
                config.getDbName());

            conn = DriverManager.getConnection(url, 
                config.getDbUser(), 
                config.getDbPassword());

            conn.setAutoCommit(true);
            connectionRef.set(conn);
            return conn;
        } catch (SQLException e) {
            String errorMessage = switch (e.getErrorCode()) {
                case 1045 -> "Invalid database credentials. Please check username and password.";
                case 1049 -> "Database not found. Please check database name.";
                case 1042, 1130 -> "Cannot connect to database server. Please check host and port.";
                default -> "Database error: " + e.getMessage();
            };
            throw new SQLException(errorMessage, e);
        }
    }

    public static <T> T withCaching(String key, Duration ttl, DatabaseOperation<T> operation) throws SQLException {
        if (!config.isCacheEnabled()) {
            return operation.execute();
        }

        return cache.get(key, () -> {
            try {
                return operation.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, ttl);
    }

    public static <T> T withCaching(String key, DatabaseOperation<T> operation) throws SQLException {
        return withCaching(key, Duration.ofMinutes(config.getCacheTTLMinutes()), operation);
    }

    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute() throws SQLException;
    }

    public static void closeConnection() {
        Connection conn = connectionRef.get();
        if (conn != null) {
            try {
                conn.close();
                connectionRef.set(null);
                cache.invalidateAll(); // Clear cache when connection is closed
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
