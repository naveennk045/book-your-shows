package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class UserCreationException extends CustomException {

    public UserCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }


}
