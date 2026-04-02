package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingDetails;
import org.bookyourshows.dto.booking.BookingSeatInfo;
import org.bookyourshows.dto.booking.BookingSummary;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.payment.PaymentInitiateRequest;
import org.bookyourshows.dto.payment.PaymentInitiateResponse;
import org.bookyourshows.dto.payment.PaymentWebhookPayload;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ForbiddenException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.PaymentRepository;
import org.bookyourshows.repository.cache.show.ShowSeatCacheRepository;
import org.bookyourshows.utils.PaymentUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ShowSeatCacheRepository showSeatCacheRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.bookingRepository = new BookingRepository();
        this.showSeatCacheRepository = new ShowSeatCacheRepository();
    }

    public PaymentInitiateResponse initiatePayment(Integer bookingId, PaymentInitiateRequest request, UserContext userContext) throws SQLException, CustomException {

        hasAccessToResource(bookingId, userContext);

        Optional<BookingDetails> bookingDetails = bookingRepository.getBookingById(bookingId);
        if (bookingDetails.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        if (bookingDetails.get().getBooking().getTotalAmount() != request.getAmount()) {
            throw new RuntimeException("Amount doesn't match");
        }

        request.setPaymentGateway("FluxPay");
        request.setPaymentGatewayTransactionId(PaymentUtils.generateGateWayTransactionId());
        Integer transactionId = this.paymentRepository.createPayment(bookingId, request);

        if (transactionId == null) {
            throw new RuntimeException("Payment initiation failed");
        }

        PaymentInitiateResponse paymentInitiateResponse = new PaymentInitiateResponse();
        paymentInitiateResponse.setTransactionId(transactionId);
        paymentInitiateResponse.setBookingId(bookingId);
        paymentInitiateResponse.setAmount(request.getAmount());
        paymentInitiateResponse.setPaymentUrl("http://localhost:8080/api/fluxpay/" + request.getPaymentGatewayTransactionId());

        return paymentInitiateResponse;
    }

    public void processPaymentWebhook(String paymentGatewayTransactionId, PaymentWebhookPayload request) throws SQLException {


        Optional<PaymentDetails> paymentDetailsOptional = paymentRepository.getPaymentDetailsByGatewayTransactionId(paymentGatewayTransactionId);

        if (paymentDetailsOptional.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        if (paymentDetailsOptional.get().getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Payment already completed.");
        }

        Integer bookingId = paymentDetailsOptional.get().getBookingId();
        Optional<BookingDetails> bookingDetails = bookingRepository.getBookingById(bookingId);


        if (bookingDetails.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        if (bookingDetails.get().getBooking().getTotalAmount() != request.getAmount()) {
            throw new RuntimeException("Amount doesn't match");
        }

        String paymentStatus = request.getStatus();
        String bookingStatus = "PENDING";
        if (paymentStatus.equals("FAILED")) {
            bookingStatus = "FAILED";
        } else if (paymentStatus.equals("SUCCESS")) {
            bookingStatus = "CONFIRMED";
        }
        Integer transactionId = paymentDetailsOptional.get().getTransactionId();
        Integer showId = bookingDetails.get().getShow().getShowId();
        Integer userId = bookingDetails.get().getBooking().getUserId();
        List<Integer> showSeatIdToBeBooked = new ArrayList<>();

        for (BookingSeatInfo bookingSeatInfo : bookingDetails.get().getSeats())
            showSeatIdToBeBooked.add(bookingSeatInfo.getShowSeatId());


        if (paymentStatus.equals("SUCCESS")) {
            bookingRepository.updateBookingStatus(bookingId, bookingStatus, paymentStatus, transactionId);
            this.showSeatCacheRepository.updateBookingStatus(showId, showSeatIdToBeBooked, "BOOKED", userId);
        }
    }

    public List<PaymentDetails> getPayments(
            Integer year,
            Integer month,
            Integer bookingId,
            String status) throws SQLException {

        return paymentRepository.getPayments(year, month, bookingId, status);
    }

    private void hasAccessToResource(Integer bookingId, UserContext userContext) throws SQLException, CustomException {

        if (bookingRepository.getBookingById(bookingId).isEmpty()) {
            throw new ResourceNotFoundException("Booking not found");
        }

        if (!userContext.getUserRole().equals("ADMIN")) {
            Optional<BookingSummary> bookings = bookingRepository.getBookingsByUserIdBookingId(userContext.getUserId(), bookingId);
            if (bookings.isEmpty()) {
                throw new ForbiddenException("Access Denied");
            }
        }
    }
}
