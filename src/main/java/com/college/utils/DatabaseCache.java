package com.college.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.time.Duration;
import java.time.Instant;

public class DatabaseCache {
    private static final DatabaseCache instance = new DatabaseCache();
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private DatabaseCache() {}

    public static DatabaseCache getInstance() {
        return instance;
    }

    private static class CacheEntry<T> {
        private final T value;
        private final Instant expiry;

        CacheEntry(T value, Duration ttl) {
            this.value = value;
            this.expiry = Instant.now().plus(ttl);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }

        T getValue() {
            return value;
        }
    }

    public <T> T get(String key, Supplier<T> supplier) {
        return get(key, supplier, DEFAULT_TTL);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> supplier, Duration ttl) {
        cleanExpiredEntries();
        
        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
        if (entry == null || entry.isExpired()) {
            T value = supplier.get();
            cache.put(key, new CacheEntry<>(value, ttl));
            return value;
        }
        
        return entry.getValue();
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void invalidateAll() {
        cache.clear();
    }

    private void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // Helper method to generate cache keys
    public static String generateKey(String... parts) {
        return String.join(":", parts);
    }
}
