package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class MovieCreationException extends CustomException {

    public MovieCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
