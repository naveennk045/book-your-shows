package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class SeatCreationException extends CustomException {

    public SeatCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);

    }
}
