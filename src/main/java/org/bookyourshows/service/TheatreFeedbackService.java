package org.bookyourshows.service;

import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.repository.TheatreFeedbackRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TheatreFeedbackService {

    private final TheatreFeedbackRepository repository;

    public TheatreFeedbackService() {
        this.repository = new TheatreFeedbackRepository();
    }

    public List<TheatreFeedbackResponse> getFeedbacksForTheatre(int theatreId, Integer limit, Integer offset)
            throws SQLException {

        return repository.getFeedbacksByTheatre(theatreId, limit, offset);
    }

    public TheatreFeedbackResponse createFeedback(int theatreId, TheatreFeedbackCreateRequest request)
            throws SQLException {

        validateCreateRequest(request);

        int id = repository.createFeedback(theatreId, request);

        return repository.getFeedbackById(theatreId, id)
                .orElseThrow(() -> new RuntimeException("Feedback created but not found"));
    }

    public boolean updateFeedback(int theatreId, int ratingId, TheatreFeedbackUpdateRequest request)
            throws SQLException {

        Optional<TheatreFeedbackResponse> existing = repository.getFeedbackById(theatreId, ratingId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = repository.updateFeedback(theatreId, ratingId, request);
        if (!updated) {
            throw new RuntimeException("Failed to update feedback");
        }

        return true;
    }

    public boolean deleteFeedback(int theatreId, int ratingId) throws SQLException {
        Optional<TheatreFeedbackResponse> existing = repository.getFeedbackById(theatreId, ratingId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        return repository.deleteFeedback(theatreId, ratingId);
    }

    private void validateCreateRequest(TheatreFeedbackCreateRequest request) {
        if (request.getBookingId() == null || request.getBookingId() <= 0) {
            throw new IllegalArgumentException("Invalid booking_id");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user_id");
        }
        if (request.getRatings() == null || request.getRatings() < 1 || request.getRatings() > 5) {
            throw new IllegalArgumentException("ratings must be between 1 and 5");
        }
    }

    private void validateUpdateRequest(TheatreFeedbackUpdateRequest request) {
        if (request.getRatings() != null &&
                (request.getRatings() < 1 || request.getRatings() > 5)) {
            throw new IllegalArgumentException("ratings must be between 1 and 5");
        }
    }
}