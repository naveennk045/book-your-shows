package org.bookyourshows.utils;

import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.exceptions.CreationException;

public class TheatreUtils {

    public static void validateTheatreRequest(TheatreCreateRequest request) throws CreationException {

        if (request.getTotalScreens() <= 0) {
            throw new CreationException("totalScreens must be greater than 0");
        }
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new CreationException("addressLine1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new CreationException("city is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new CreationException("state is required");
        }
        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new CreationException("pincode is required");
        }
    }


    public static void validateTheatreName(String name) throws CreationException {
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

    public static void validateEmail(String email) throws CreationException {
        if (email == null || email.isBlank()) {
            throw new CreationException("email is required");
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(regex)) {
            throw new CreationException("Invalid email format");
        }
    }

    public static void validateContactNumber(String number) throws CreationException {
        if (number == null || number.isBlank()) {
            throw new CreationException("contactNumber is required");
        }

        number = number.trim();

        if (!number.matches("^(\\+91)?[6-9]\\d{9}$")) {
            throw new CreationException("Invalid contact number");
        }
    }

    public static void validateTheatreAddress(Address request) throws CreationException {
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new CreationException("address_line1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new CreationException("city is required");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new CreationException("latitude and longitude are required");
        }
    }

    public static void validateTheatreUpdateRequest(TheatreUpdateRequest request) throws CreationException {
        if (request.getTotalScreens() <= 0) throw new CreationException("totalScreens must be > 0");
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank())
            throw new CreationException("addressLine1 is required");
        if (request.getCity() == null || request.getCity().isBlank())
            throw new CreationException("city is required");
        if (request.getState() == null || request.getState().isBlank())
            throw new CreationException("state is required");
        if (request.getPincode() == null || request.getPincode().isBlank())
            throw new CreationException("pincode is required");
    }
}