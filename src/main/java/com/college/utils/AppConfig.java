package com.college.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {
    private static final AppConfig instance = new AppConfig();
    private final Properties properties;
    private final Path configPath;

    private static final String DEFAULT_CONFIG = """
        # Database Configuration
        db.host=127.0.0.1
        db.port=3306
        db.name=college_db
        db.user=root
        db.password=Pass456@
        
        # Application Settings
        app.session.timeout=30
        app.theme=light
        app.language=en
        
        # Cache Settings
        cache.enabled=true
        cache.ttl.minutes=5
        
        # Security Settings
        security.password.minLength=8
        security.password.requireSpecial=true
        security.password.requireNumbers=true
        security.password.requireUppercase=true
        security.password.requireLowercase=true
        """;

    private AppConfig() {
        properties = new Properties();
        configPath = Paths.get(System.getProperty("user.home"), ".college", "config.properties");
        
        // Create config directory if it doesn't exist
        configPath.getParent().toFile().mkdirs();
        
        // Load or create default configuration
        if (configPath.toFile().exists()) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Failed to load configuration: " + e.getMessage());
                loadDefaults();
            }
        } else {
            loadDefaults();
            saveConfig();
        }
    }

    private void loadDefaults() {
        try {
            properties.load(new java.io.StringReader(DEFAULT_CONFIG));
        } catch (IOException e) {
            System.err.println("Failed to load default configuration: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
            properties.store(fos, "College Management System Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    public static AppConfig getInstance() {
        return instance;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }

    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    public void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    // Database configuration getters
    public String getDbHost() {
        return get("db.host", "127.0.0.1");
    }

    public int getDbPort() {
        return getInt("db.port", 3306);
    }

    public String getDbName() {
        return get("db.name", "college_db");
    }

    public String getDbUser() {
        return get("db.user", "root");
    }

    public String getDbPassword() {
        return get("db.password", "Pass456@");
    }

    // Application settings getters
    public int getSessionTimeout() {
        return getInt("app.session.timeout", 30);
    }

    public String getTheme() {
        return get("app.theme", "light");
    }

    public String getLanguage() {
        return get("app.language", "en");
    }

    // Cache settings getters
    public boolean isCacheEnabled() {
        return getBoolean("cache.enabled", true);
    }

    public int getCacheTTLMinutes() {
        return getInt("cache.ttl.minutes", 5);
    }

    // Security settings getters
    public int getPasswordMinLength() {
        return getInt("security.password.minLength", 8);
    }

    public boolean isPasswordSpecialRequired() {
        return getBoolean("security.password.requireSpecial", true);
    }

    public boolean isPasswordNumbersRequired() {
        return getBoolean("security.password.requireNumbers", true);
    }

    public boolean isPasswordUppercaseRequired() {
        return getBoolean("security.password.requireUppercase", true);
    }

    public boolean isPasswordLowercaseRequired() {
        return getBoolean("security.password.requireLowercase", true);
    }
}
