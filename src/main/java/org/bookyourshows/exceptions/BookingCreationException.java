package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class BookingCreationException extends CustomException {

    public BookingCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
