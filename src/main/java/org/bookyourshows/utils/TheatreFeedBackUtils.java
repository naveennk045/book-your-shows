package org.bookyourshows.utils;

import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.FeedBackCreationException;

public class TheatreFeedBackUtils {


    public static void validateCreateRequest(TheatreFeedbackCreateRequest request) throws CustomException {
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

    public static void validateUpdateRequest(TheatreFeedbackUpdateRequest request) throws CustomException {
        if (request.getRatings() != null &&
                (request.getRatings() < 1 || request.getRatings() > 5)) {
            throw new FeedBackCreationException("ratings must be between 1 and 5");
        }
    }
}
