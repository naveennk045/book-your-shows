package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingDetails;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.payment.PaymentInitiateRequest;
import org.bookyourshows.dto.payment.PaymentInitiateResponse;
import org.bookyourshows.dto.payment.PaymentWebhookPayload;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.PaymentRepository;
import org.bookyourshows.utils.PaymentUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.bookingRepository = new BookingRepository();
    }

    public PaymentInitiateResponse initiatePayment(Integer bookingId, PaymentInitiateRequest request) throws SQLException {

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
            throw new RuntimeException("Payment already completed failed");
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

        bookingRepository.updateBookingStatus(bookingId, bookingStatus, paymentStatus, transactionId);
    }

    public List<PaymentDetails> getPayments(
            Integer year,
            Integer month,
            Integer bookingId,
            String status) throws SQLException {

        return paymentRepository.getPayments(year, month, bookingId, status);
    }
}
