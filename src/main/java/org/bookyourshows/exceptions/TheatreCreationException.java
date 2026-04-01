package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class TheatreCreationException extends CustomException {
    public TheatreCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
