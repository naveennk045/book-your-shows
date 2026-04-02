package org.bookyourshows.service;

import org.bookyourshows.dto.booking.BookingDetails;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.refund.RefundDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ForbiddenException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.PaymentRepository;
import org.bookyourshows.repository.RefundRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public RefundService() {

        this.refundRepository = new RefundRepository();
        this.paymentRepository = new PaymentRepository();
        this.bookingRepository = new BookingRepository();

    }


    public Optional<RefundDetails> getRefundById(int refundId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToResource(refundId,userContext);

        return refundRepository.getRefundById(refundId);
    }

    public List<RefundDetails> getRefunds(
            Integer year,
            Integer paymentId,
            String status) throws SQLException {

        return refundRepository.getRefunds(year, paymentId, status);
    }

    private void hasAccessToResource(Integer refundId, UserContext userContext) throws SQLException, CustomException {

        Optional<RefundDetails> refundDetails = this.refundRepository.getRefundById(refundId);

        if (refundDetails.isEmpty()) {
            throw new ResourceNotFoundException("Refund not found");
        }

        if (!userContext.getUserRole().equals("ADMIN")) {

            Optional<PaymentDetails> paymentDetails = this.paymentRepository.getPaymentDetailsByTransactionId(refundDetails.get().getTransactionId());
            if (paymentDetails.isEmpty()) {
                throw new ResourceNotFoundException("Payment not found");
            }
            Optional<BookingDetails> bookingDetails = this.bookingRepository.getBookingById(paymentDetails.get().getBookingId());

            if (bookingDetails.isPresent() && !userContext.getUserRole().equals("ADMIN") && !userContext.getUserId().equals(bookingDetails.get().getBooking().getUserId())) {
                throw new ForbiddenException("Access denied");
            }
        }
    }
}