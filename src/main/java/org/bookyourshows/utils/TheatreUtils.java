package org.bookyourshows.utils;

public class TheatreUtils {


    public static void validateTheatreName(String name) {
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

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(regex)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validateContactNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("contactNumber is required");
        }

        number = number.trim();

        if (!number.matches("^(\\+91)?[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Invalid contact number");
        }
    }


}
