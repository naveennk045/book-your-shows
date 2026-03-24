package org.bookyourshows.utils;

public class ScreenUtils {

    public static void validateScreenName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("theatreName is required");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException("theatreName too long");
        }

        if (name.matches("\\d+")) {
            throw new IllegalArgumentException("theatreName cannot be only numbers");
        }

        if (!name.matches("^[a-zA-Z0-9\\s&().-]+$")) {
            throw new IllegalArgumentException("Invalid characters in theatreName");
        }
    }
}
