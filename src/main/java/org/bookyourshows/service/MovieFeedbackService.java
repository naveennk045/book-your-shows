package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingMovieInfo;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ResourceConflictException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.MovieFeedbackRepository;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.MovieFeedBackUtils.validateCreateRequest;
import static org.bookyourshows.utils.MovieFeedBackUtils.validateUpdateRequest;

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
            throws SQLException, CustomException {

        validateCreateRequest(request);

        if (userRepository.getUserByUserId(request.getUserId()).isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }


        BookingMovieInfo bookingInfo =
                bookingRepository.findBookingWithMovieAndUser(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (bookingInfo.getMovieId() != movieId) {
            throw new ResourceConflictException("Booking does not belong to this movie");
        }

        if (bookingInfo.getUserId() != request.getUserId()) {
            throw new ResourceConflictException("Booking does not belong to this user");
        }

        int id = movieFeedbackRepository.createFeedback(movieId, request);

        return movieFeedbackRepository.getFeedbackById(movieId, id)
                .orElseThrow(() -> new RuntimeException("Feedback created but not found"));
    }

    public void updateFeedback(int movieId,
                               int ratingId,
                               MovieFeedbackUpdateRequest request)
            throws SQLException, CustomException {

        Optional<MovieFeedbackResponse> existing =
                movieFeedbackRepository.getFeedbackById(movieId, ratingId);

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = movieFeedbackRepository.updateFeedback(movieId, ratingId, request);

        if (!updated) {
            throw new SQLException("Failed to update feedback");
        }

    }

    public boolean deleteFeedback(int movieId, int ratingId) throws SQLException, CustomException {

        Optional<MovieFeedbackResponse> existing =
                movieFeedbackRepository.getFeedbackById(movieId, ratingId);

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Feedback not found");
        }

        return movieFeedbackRepository.deleteFeedback(movieId, ratingId);
    }
}