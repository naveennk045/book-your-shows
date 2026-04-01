package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ForbiddenException extends CustomException {
    public ForbiddenException(String message) {
        super(message, HttpServletResponse.SC_FORBIDDEN);
    }

}
