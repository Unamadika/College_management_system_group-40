package com.college.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> requirements;

        public ValidationResult(boolean valid, List<String> requirements) {
            this.valid = valid;
            this.requirements = requirements;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getRequirements() {
            return requirements;
        }
    }

    public static ValidationResult validate(String password) {
        List<String> requirements = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            requirements.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (!UPPERCASE.matcher(password).find()) {
            requirements.add("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE.matcher(password).find()) {
            requirements.add("Password must contain at least one lowercase letter");
        }

        if (!DIGIT.matcher(password).find()) {
            requirements.add("Password must contain at least one digit");
        }

        if (!SPECIAL.matcher(password).find()) {
            requirements.add("Password must contain at least one special character");
        }

        return new ValidationResult(requirements.isEmpty(), requirements);
    }

    public static String hashPassword(String password) {
        // In a real application, use a proper hashing algorithm like BCrypt
        // This is just a placeholder for demonstration
        return String.valueOf(password.hashCode());
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        // In a real application, use proper password verification
        // This is just a placeholder for demonstration
        return String.valueOf(password.hashCode()).equals(hashedPassword);
    }
}
