package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class BadRequestException extends CustomException {


    public BadRequestException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
