package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class CreationException extends CustomException {

    public CreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
