package org.bookyourshows.service;

import org.bookyourshows.dto.booking.*;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.refund.RefundCreateRequest;
import org.bookyourshows.dto.show.ShowSeating;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.*;
import org.bookyourshows.repository.*;
import org.bookyourshows.repository.cache.show.ShowSeatCacheRepository;
import org.bookyourshows.utils.PaymentUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final TheatreRepository theatreRepository;
    private final ShowSeatCacheRepository showSeatCacheRepository;

    public BookingService() {
        this.bookingRepository = new BookingRepository();
        this.showRepository = new ShowRepository();
        this.paymentRepository = new PaymentRepository();
        this.refundRepository = new RefundRepository();
        this.theatreRepository = new TheatreRepository();
        this.showSeatCacheRepository = new ShowSeatCacheRepository();
    }

    public Optional<BookingDetails> getBookingById(int bookingId, UserContext userContext) throws SQLException, CustomException {
        hasAccessToResource(bookingId, userContext);
        return bookingRepository.getBookingById(bookingId);
    }

    public List<BookingSummary> getAllBookings() throws SQLException {
        return bookingRepository.getAllBookings();
    }

    public List<BookingSummary> getBookingsByUserId(Integer userId, UserContext userContext) throws SQLException, CustomException {
        if (!Objects.equals(userId, userContext.getUserId())) {
            throw new ForbiddenException("Access denied");
        }
        return bookingRepository.getBookingsByUserId(userId);
    }

    public List<BookingSummary> getBookingsByTheatreId(int theatreId, UserContext userContext) throws SQLException, CustomException {
        Optional<TheatreDetails> theatre = theatreRepository.getTheatreById(theatreId);

        if (theatre.isEmpty()) {
            throw new ResourceNotFoundException("No theatre found");
        }
        if (!Objects.equals(theatre.get().getTheatre().getOwnerId(), userContext.getUserId())) {
            throw new ForbiddenException("Access denied");

        }
        return bookingRepository.getBookingsByTheatreId(theatreId);
    }

    public int createBooking(Integer userId, BookingCreateRequest request) throws SQLException, CustomException {
        if (request.getShowId() <= 0) {
            throw new BookingCreationException("show_id is required");
        }
        if (request.getShowSeatIds() == null || request.getShowSeatIds().isEmpty()) {
            throw new BookingCreationException("At least one seat is required");
        }


        Map<Integer, ShowSeating> showSeating = showRepository.getShowSeatsByShowId(request.getShowId());
        Double totalAmount = (double) 0;


        for (Integer showSeatId : request.getShowSeatIds()) {
            if (showSeating.containsKey(showSeatId)) {
                totalAmount += showSeating.get(showSeatId).getFinalPrice();
            } else {
                throw new BookingCreationException("no show seat found");
            }
        }


        if (request.getClientTotalAmount() == null) {
            throw new BookingCreationException("total amount is required");
        } else if (!totalAmount.equals(request.getClientTotalAmount())) {
            throw new BookingCreationException("calculated total amount is not equal to the requested total amount");
        }

        int bookingId = bookingRepository.createBookingWithSeats(userId, request, totalAmount);
        List<Integer> showSeatIdToBeBooked = request.getShowSeatIds();
        this.showSeatCacheRepository.lockShowSeats(showSeatIdToBeBooked, request.getShowId(), userId);

        return bookingId;
    }

    public Integer cancelBooking(Integer bookingId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToResource(bookingId, userContext);

        Optional<BookingDetails> bookingDetails = bookingRepository.getBookingById(bookingId);
        Optional<PaymentDetails> paymentDetailsOptional = paymentRepository.getPaymentDetailsByBookingId(bookingId);

        if (paymentDetailsOptional.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found");
        }

        if (!paymentDetailsOptional.get().getStatus().equals("SUCCESS")) {
            throw new ResourceConflictException("Only successful payments can be refunded");
        }

        if (paymentDetailsOptional.get().getStatus().equals("REFUNDED")) {
            throw new ResourceConflictException("Already refunded");
        }

        if (bookingDetails.isEmpty()) {
            throw new ResourceNotFoundException("Booking not found");
        }

        LocalDate showDate = LocalDate.parse(bookingDetails.get().getShow().getShowDate());
        LocalTime startTime = LocalTime.parse(bookingDetails.get().getShow().getStartTime());

        LocalDateTime showDateTime = LocalDateTime.of(showDate, startTime);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime cutoffTime = showDateTime.minusHours(5);

        if (now.isAfter(cutoffTime)) {
            throw new ResourceConflictException("Cancellation not allowed within 5 hours of show time");
        }


        String paymentStatus = "REFUNDED";
        String bookingStatus = "CANCELLED";
        Integer paymentTransactionId = paymentDetailsOptional.get().getTransactionId();

        bookingRepository.updateBookingStatus(bookingId, bookingStatus, paymentStatus, paymentTransactionId);

        RefundCreateRequest refundCreateRequest = new RefundCreateRequest();
        refundCreateRequest.setGatewayRefundId(PaymentUtils.generateGateWayTransactionId());
        refundCreateRequest.setReason("Booking Cancelled");
        refundCreateRequest.setAmount(paymentDetailsOptional.get().getAmount());
        refundCreateRequest.setTransactionId(paymentTransactionId);

        return refundRepository.createRefund(refundCreateRequest);
    }

    private void hasAccessToResource(Integer bookingId, UserContext userContext) throws SQLException, ForbiddenException {

        if (!userContext.getUserRole().equals("ADMIN")) {
            Optional<BookingSummary> bookings = bookingRepository.getBookingsByUserIdBookingId(userContext.getUserId(), bookingId);
            if (bookings.isEmpty()) {
                throw new ForbiddenException("Booking not found");
            }
        }
    }
}

