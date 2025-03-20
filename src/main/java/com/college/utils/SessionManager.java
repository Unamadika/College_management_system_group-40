package com.college.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private final Map<String, Session> sessions = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public static class Session {
        private final String username;
        private final String role;
        private LocalDateTime lastAccess;

        public Session(String username, String role) {
            this.username = username;
            this.role = role;
            this.lastAccess = LocalDateTime.now();
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public LocalDateTime getLastAccess() {
            return lastAccess;
        }

        public void updateLastAccess() {
            this.lastAccess = LocalDateTime.now();
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(lastAccess.plusMinutes(SESSION_TIMEOUT_MINUTES));
        }
    }

    public String createSession(String username, String role) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new Session(username, role));
        return sessionId;
    }

    public Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            if (session.isExpired()) {
                invalidateSession(sessionId);
                return null;
            }
            session.updateLastAccess();
        }
        return session;
    }

    public void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public void invalidateAllSessions() {
        sessions.clear();
    }

    public boolean isValidSession(String sessionId) {
        return getSession(sessionId) != null;
    }

    public void cleanExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
