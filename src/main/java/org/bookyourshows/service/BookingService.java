package org.bookyourshows.service;

import org.bookyourshows.dto.booking.*;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.refund.RefundCreateRequest;
import org.bookyourshows.dto.show.ShowSeating;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.PaymentRepository;
import org.bookyourshows.repository.RefundRepository;
import org.bookyourshows.repository.ShowRepository;
import org.bookyourshows.utils.PaymentUtils;

import java.sql.SQLException;
import java.util.*;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public BookingService() {
        this.bookingRepository = new BookingRepository();
        this.showRepository = new ShowRepository();
        this.paymentRepository = new PaymentRepository();
        this.refundRepository = new RefundRepository();
    }

    public Optional<BookingDetails> getBookingById(int bookingId) throws SQLException {
        return bookingRepository.getBookingById(bookingId);
    }

    public int createBooking(int userId, BookingCreateRequest request) throws SQLException {
        if (request.getShowId() <= 0) {
            throw new IllegalArgumentException("show_id is required");
        }
        if (request.getShowSeatIds() == null || request.getShowSeatIds().isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }


        Map<Integer, ShowSeating> showSeating = showRepository.getShowSeatsByShowId(request.getShowId());
        Double totalAmount = (double) 0;


        for (Integer showSeatId : request.getShowSeatIds()) {
            if (showSeating.containsKey(showSeatId)) {
                totalAmount += showSeating.get(showSeatId).getFinalPrice();
            } else {
                throw new IllegalArgumentException("no show seat found");
            }
        }


        if (request.getClientTotalAmount() == null) {
            throw new IllegalArgumentException("total amount is required");
        } else if (!totalAmount.equals(request.getClientTotalAmount())) {
            throw new IllegalArgumentException("calculated total amount is not equal to the requested total amount");
        }

        return bookingRepository.createBookingWithSeats(userId, request, totalAmount);
    }

    public Integer cancelBooking(Integer bookingId) throws SQLException {


        Optional<BookingDetails> bookingDetails = bookingRepository.getBookingById(bookingId);

        Optional<PaymentDetails> paymentDetailsOptional = paymentRepository.getPaymentDetailsByBookingId(bookingId);

        if (paymentDetailsOptional.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        if (!paymentDetailsOptional.get().getStatus().equals("SUCCESS")) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        if (paymentDetailsOptional.get().getStatus().equals("REFUNDED")) {
            throw new RuntimeException("Already refunded");
        }

        if (bookingDetails.isEmpty()) {
            throw new RuntimeException("Booking not found");
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
}
