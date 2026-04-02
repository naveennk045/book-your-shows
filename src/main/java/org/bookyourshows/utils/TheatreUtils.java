package org.bookyourshows.utils;

import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.exceptions.TheatreCreationException;

public class TheatreUtils {

    public static void validateTheatreRequest(TheatreCreateRequest request) throws TheatreCreationException {

        if (request.getTotalScreens() <= 0) {
            throw new TheatreCreationException("totalScreens must be greater than 0");
        }
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new TheatreCreationException("addressLine1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new TheatreCreationException("city is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new TheatreCreationException("state is required");
        }
        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new TheatreCreationException("pincode is required");
        }
    }


    public static void validateTheatreName(String name) throws TheatreCreationException {
        if (name == null || name.isBlank()) {
            throw new TheatreCreationException("theatreName is required");
        }

        if (name.length() > 150) {
            throw new TheatreCreationException("theatreName too long");
        }

        if (name.matches("\\d+")) {
            throw new TheatreCreationException("theatreName cannot be only numbers");
        }

        if (!name.matches("^[a-zA-Z0-9\\s&().-]+$")) {
            throw new TheatreCreationException("Invalid characters in theatreName");
        }
    }

    public static void validateEmail(String email) throws TheatreCreationException {
        if (email == null || email.isBlank()) {
            throw new TheatreCreationException("email is required");
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(regex)) {
            throw new TheatreCreationException("Invalid email format");
        }
    }

    public static void validateContactNumber(String number) throws TheatreCreationException {
        if (number == null || number.isBlank()) {
            throw new TheatreCreationException("contactNumber is required");
        }

        number = number.trim();

        if (!number.matches("^(\\+91)?[6-9]\\d{9}$")) {
            throw new TheatreCreationException("Invalid contact number");
        }
    }

    public static void validateTheatreAddress(Address request) throws TheatreCreationException {
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new TheatreCreationException("address_line1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new TheatreCreationException("city is required");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new TheatreCreationException("latitude and longitude are required");
        }
    }

    public static void validateTheatreUpdateRequest(TheatreUpdateRequest request) throws TheatreCreationException {
        if (request.getTotalScreens() <= 0) throw new TheatreCreationException("totalScreens must be > 0");
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank())
            throw new TheatreCreationException("addressLine1 is required");
        if (request.getCity() == null || request.getCity().isBlank())
            throw new TheatreCreationException("city is required");
        if (request.getState() == null || request.getState().isBlank())
            throw new TheatreCreationException("state is required");
        if (request.getPincode() == null || request.getPincode().isBlank())
            throw new TheatreCreationException("pincode is required");
    }
}