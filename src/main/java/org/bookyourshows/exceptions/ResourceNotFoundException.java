package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(String message) {
        super(message, HttpServletResponse.SC_NOT_FOUND);
    }

}
