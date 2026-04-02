package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ScreenCreationException extends CustomException {

    public ScreenCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
