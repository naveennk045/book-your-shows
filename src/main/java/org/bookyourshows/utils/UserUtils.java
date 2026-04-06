package org.bookyourshows.utils;


import org.bookyourshows.dto.address.Address;
import org.bookyourshows.exceptions.CreationException;
import org.bookyourshows.exceptions.CustomException;

public class UserUtils {

    public static void validateEmail(String email) throws CreationException {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new CreationException("Invalid email");
        }
    }

    public static void validateMobile(String mobile) throws CreationException {
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new CreationException("Invalid mobile number");
        }
    }

    public static void validateName(String name, String field) throws CreationException {
        if (name == null || name.isBlank()) {
            throw new CreationException(field + " is required");
        }
    }

    public static void validatePassword(String password) throws CreationException {
        if (password == null || password.length() < 6) {
            throw new CreationException("Password must be at least 6 characters");
        }
    }

    public static void validateAddress(Address request) throws CustomException {
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new CreationException("address_line1 is required");
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
}