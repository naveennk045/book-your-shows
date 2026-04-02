package org.bookyourshows.utils;

import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.FeedBackCreationException;

public class MovieFeedBackUtils {

    public static void validateCreateRequest(MovieFeedbackCreateRequest request) throws CustomException {
        if (request.getBookingId() == null || request.getBookingId() <= 0) {
            throw new FeedBackCreationException("Invalid booking_id");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new FeedBackCreationException("Invalid user_id");
        }
        if (request.getRatings() == null ||
                request.getRatings() < 1 || request.getRatings() > 5) {
            throw new FeedBackCreationException("ratings must be between 1 and 5");
        }
    }

    public static void validateUpdateRequest(MovieFeedbackUpdateRequest request) throws CustomException {
        if (request.getRatings() != null &&
                (request.getRatings() < 1 || request.getRatings() > 5)) {
            throw new FeedBackCreationException("ratings must be between 1 and 5");
        }
    }
}
