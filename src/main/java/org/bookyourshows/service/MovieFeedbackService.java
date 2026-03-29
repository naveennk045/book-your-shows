package org.bookyourshows.service;

import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.repository.MovieFeedbackRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MovieFeedbackService {

    private final MovieFeedbackRepository repository;

    public MovieFeedbackService() {
        this.repository = new MovieFeedbackRepository();
    }

    public List<MovieFeedbackResponse> getFeedbacksForMovie(int movieId, Integer limit, Integer offset)
            throws SQLException {

        return repository.getFeedbacksByMovie(movieId, limit, offset);
    }

    public MovieFeedbackResponse createFeedback(int movieId, MovieFeedbackCreateRequest request)
            throws SQLException {

        validateCreateRequest(request);

        int id = repository.createFeedback(movieId, request);

        return repository.getFeedbackById(movieId, id)
                .orElseThrow(() -> new RuntimeException("Feedback created but not found"));
    }

    public boolean updateFeedback(int movieId, int ratingId, MovieFeedbackUpdateRequest request)
            throws SQLException {

        Optional<MovieFeedbackResponse> existing = repository.getFeedbackById(movieId, ratingId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = repository.updateFeedback(movieId, ratingId, request);
        if (!updated) {
            throw new RuntimeException("Failed to update feedback");
        }

        return true;
    }

    public boolean deleteFeedback(int movieId, int ratingId) throws SQLException {
        Optional<MovieFeedbackResponse> existing = repository.getFeedbackById(movieId, ratingId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        return repository.deleteFeedback(movieId, ratingId);
    }

    private void validateCreateRequest(MovieFeedbackCreateRequest request) {
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

    private void validateUpdateRequest(MovieFeedbackUpdateRequest request) {
        if (request.getRatings() != null &&
                (request.getRatings() < 1 || request.getRatings() > 5)) {
            throw new IllegalArgumentException("ratings must be between 1 and 5");
        }
    }
}