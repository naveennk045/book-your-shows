package org.bookyourshows.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class FeedBackCreationException extends CustomException {

    public FeedBackCreationException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }
}
