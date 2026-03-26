package org.bookyourshows.utils;

public class UserUtils {

    public static void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    public static void validateMobile(String mobile) {
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid mobile number");
        }
    }

    public static void validateName(String name, String field) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
}