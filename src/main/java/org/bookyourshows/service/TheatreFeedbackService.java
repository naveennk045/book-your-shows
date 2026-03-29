package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingInfo;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.TheatreFeedbackRepository;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TheatreFeedbackService {

    private final TheatreFeedbackRepository theatreFeedbackRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public TheatreFeedbackService() {
        this.theatreFeedbackRepository = new TheatreFeedbackRepository();
        this.userRepository = new UserRepository();
        this.bookingRepository = new BookingRepository();
    }

    public List<TheatreFeedbackResponse> getFeedbacksForTheatre(int theatreId,
                                                                Integer limit,
                                                                Integer offset)
            throws SQLException {
        return theatreFeedbackRepository.getFeedbacksByTheatre(theatreId, limit, offset);
    }

    public TheatreFeedbackResponse createFeedback(int theatreId,
                                                  TheatreFeedbackCreateRequest request)
            throws SQLException {

        validateCreateRequest(request);

        // 1. user_id exists
        if (userRepository.getUserByUserId(request.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        // 2. booking exists and belongs to this theatre + this user
        BookingInfo bookingInfo =
                bookingRepository.findBookingWithTheatreAndUser(request.getBookingId())
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (bookingInfo.getTheatreId() != theatreId) {
            throw new IllegalArgumentException("Booking does not belong to this theatre");
        }

        if (bookingInfo.getUserId() != request.getUserId()) {
            throw new IllegalArgumentException("Booking does not belong to this user");
        }

        // 3. Insert feedback
        int id = theatreFeedbackRepository.createFeedback(theatreId, request);

        return theatreFeedbackRepository.getFeedbackById(theatreId, id)
                .orElseThrow(() -> new RuntimeException("Feedback created but not found"));
    }

    public boolean updateFeedback(int theatreId,
                                  int ratingId,
                                  TheatreFeedbackUpdateRequest request)
            throws SQLException {

        // 1. rating_id exists for this theatre
        Optional<TheatreFeedbackResponse> existing =
                theatreFeedbackRepository.getFeedbackById(theatreId, ratingId);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = theatreFeedbackRepository.updateFeedback(theatreId, ratingId, request);

        if (!updated) {
            throw new RuntimeException("Failed to update feedback");
        }

        return true;
    }

    public boolean deleteFeedback(int theatreId, int ratingId) throws SQLException {

        // 1. rating_id exists for this theatre
        Optional<TheatreFeedbackResponse> existing =
                theatreFeedbackRepository.getFeedbackById(theatreId, ratingId);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Feedback not found");
        }

        return theatreFeedbackRepository.deleteFeedback(theatreId, ratingId);
    }

    private void validateCreateRequest(TheatreFeedbackCreateRequest request) {
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

    private void validateUpdateRequest(TheatreFeedbackUpdateRequest request) {
        if (request.getRatings() != null &&
                (request.getRatings() < 1 || request.getRatings() > 5)) {
            throw new IllegalArgumentException("ratings must be between 1 and 5");
        }
    }
}