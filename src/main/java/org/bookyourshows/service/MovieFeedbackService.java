package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingMovieInfo;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.MovieFeedbackRepository;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MovieFeedbackService {

    private final MovieFeedbackRepository movieFeedbackRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public MovieFeedbackService() {
        this.movieFeedbackRepository = new MovieFeedbackRepository();
        this.userRepository = new UserRepository();
        this.bookingRepository = new BookingRepository();
    }

    public List<MovieFeedbackResponse> getFeedbacksForMovie(int movieId,
                                                            Integer limit,
                                                            Integer offset)
            throws SQLException {
        return movieFeedbackRepository.getFeedbacksByMovie(movieId, limit, offset);
    }

    public MovieFeedbackResponse createFeedback(int movieId,
                                                MovieFeedbackCreateRequest request)
            throws SQLException {

        validateCreateRequest(request);

        // 1. user_id exists
        if (userRepository.getUserByUserId(request.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }


        BookingMovieInfo bookingInfo =
                bookingRepository.findBookingWithMovieAndUser(request.getBookingId())
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (bookingInfo.getMovieId() != movieId) {
            throw new IllegalArgumentException("Booking does not belong to this movie");
        }

        if (bookingInfo.getUserId() != request.getUserId()) {
            throw new IllegalArgumentException("Booking does not belong to this user");
        }

        // 3. Insert feedback
        int id = movieFeedbackRepository.createFeedback(movieId, request);

        return movieFeedbackRepository.getFeedbackById(movieId, id)
                .orElseThrow(() -> new RuntimeException("Feedback created but not found"));
    }

    public boolean updateFeedback(int movieId,
                                  int ratingId,
                                  MovieFeedbackUpdateRequest request)
            throws SQLException {

        // 1. rating_id exists for this movie
        Optional<MovieFeedbackResponse> existing =
                movieFeedbackRepository.getFeedbackById(movieId, ratingId);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = movieFeedbackRepository.updateFeedback(movieId, ratingId, request);

        if (!updated) {
            throw new RuntimeException("Failed to update feedback");
        }

        return true;
    }

    public boolean deleteFeedback(int movieId, int ratingId) throws SQLException {

        // 1. rating_id exists for this movie
        Optional<MovieFeedbackResponse> existing =
                movieFeedbackRepository.getFeedbackById(movieId, ratingId);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        return movieFeedbackRepository.deleteFeedback(movieId, ratingId);
    }

    private void validateCreateRequest(MovieFeedbackCreateRequest request) {
        if (request.getBookingId() == null || request.getBookingId() <= 0) {
            throw new IllegalArgumentException("Invalid booking_id");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user_id");
        }
        if (request.getRatings() == null ||
                request.getRatings() < 1 || request.getRatings() > 5) {
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