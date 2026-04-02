package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingInfo;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.*;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.TheatreFeedbackRepository;
import org.bookyourshows.repository.UserRepository;
import org.bookyourshows.utils.TheatreFeedBackUtils.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.TheatreFeedBackUtils.validateCreateRequest;
import static org.bookyourshows.utils.TheatreFeedBackUtils.validateUpdateRequest;


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
            throws SQLException, CustomException {

        validateCreateRequest(request);

        // 1. user_id exists
        if (userRepository.getUserByUserId(request.getUserId()).isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        // 2. booking exists and belongs to this theatre + this user
        BookingInfo bookingInfo =
                bookingRepository.findBookingWithTheatreAndUser(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (bookingInfo.getTheatreId() != theatreId) {
            throw new ResourceConflictException("Booking does not belong to this theatre");
        }

        if (bookingInfo.getUserId() != request.getUserId()) {
            throw new ResourceConflictException("Booking does not belong to this user");
        }

        // 3. Insert feedback
        int id = theatreFeedbackRepository.createFeedback(theatreId, request);

        return theatreFeedbackRepository.getFeedbackByTheatreIdRatingId(theatreId, id)
                .orElseThrow(() -> new PartialUpdateException("Feedback created but not found"));
    }

    public void updateFeedback(int theatreId,
                               int ratingId,
                               TheatreFeedbackUpdateRequest request)
            throws SQLException, CustomException {

        // 1. rating_id exists for this theatre
        Optional<TheatreFeedbackResponse> existing =
                theatreFeedbackRepository.getFeedbackByTheatreIdRatingId(theatreId, ratingId);

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Feedback not found");
        }

        validateUpdateRequest(request);

        boolean updated = theatreFeedbackRepository.updateFeedback(theatreId, ratingId, request);

        if (!updated) {
            throw new SQLException("Failed to update feedback");
        }
    }

    public boolean deleteFeedback(int theatreId, int ratingId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToResource(ratingId, userContext);

        // 1. rating_id exists for this theatre
        Optional<TheatreFeedbackResponse> existing =
                theatreFeedbackRepository.getFeedbackByTheatreIdRatingId(theatreId, ratingId);

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Feedback not found");
        }

        return theatreFeedbackRepository.deleteFeedback(theatreId, ratingId);
    }


    private void hasAccessToResource(Integer ratingId, UserContext userContext) throws CustomException, SQLException {

        Optional<TheatreFeedbackResponse> feedbackResponse = theatreFeedbackRepository.getFeedbackByRatingId(ratingId);
        if (feedbackResponse.isEmpty()) {
            throw new ResourceNotFoundException("Feedback not found");
        }
        if (!userContext.getUserRole().equals("ADMIN") &&
                !feedbackResponse.get().getUserId().equals(userContext.getUserId())) {
            throw new ForbiddenException("Access denied");
        }
    }
}