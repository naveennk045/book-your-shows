package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class ShowCreationException extends CustomException {

    public ShowCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }


}
