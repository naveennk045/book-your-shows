package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ResourceConflictException extends CustomException {

    public ResourceConflictException(String message) {
        super(message, HttpServletResponse.SC_CONFLICT);
    }
}
