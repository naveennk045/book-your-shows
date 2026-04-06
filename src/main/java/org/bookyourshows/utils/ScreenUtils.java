package org.bookyourshows.utils;

import org.bookyourshows.exceptions.CreationException;

public class ScreenUtils {

    public static void validateScreenName(String name) throws CreationException {
        if (name == null || name.isBlank()) {
            throw new CreationException("theatreName is required");
        }

        if (name.length() > 150) {
            throw new CreationException("theatreName too long");
        }

        if (name.matches("\\d+")) {
            throw new CreationException("theatreName cannot be only numbers");
        }

        if (!name.matches("^[a-zA-Z0-9\\s&().-]+$")) {
            throw new CreationException("Invalid characters in theatreName");
        }
    }
}
