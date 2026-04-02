package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ActionFailedException extends CustomException {


    public ActionFailedException(String message) {
        super(message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
