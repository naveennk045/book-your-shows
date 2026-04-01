package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class PartialUpdateException extends CustomException {

    public PartialUpdateException(String message) {
        super(message, HttpServletResponse.SC_CREATED);
    }

}
