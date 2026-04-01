package org.bookyourshows.utils;


import org.bookyourshows.exceptions.UserCreationException;

public class UserUtils {

    public static void validateEmail(String email) throws UserCreationException {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new UserCreationException("Invalid email");
        }
    }

    public static void validateMobile(String mobile) throws UserCreationException {
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new UserCreationException("Invalid mobile number");
        }
    }

    public static void validateName(String name, String field) throws UserCreationException {
        if (name == null || name.isBlank()) {
            throw new UserCreationException(field + " is required");
        }
    }

    public static void validatePassword(String password) throws UserCreationException {
        if (password == null || password.length() < 6) {
            throw new UserCreationException("Password must be at least 6 characters");
        }
    }
}