package org.bookyourshows.service;

import org.bookyourshows.dto.refund.RefundDetails;
import org.bookyourshows.repository.RefundRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RefundService {

    private final RefundRepository refundRepository;

    public RefundService() {
        this.refundRepository = new RefundRepository();
    }


    public Optional<RefundDetails> getRefundById(int refundId) throws SQLException {

        if (refundId <= 0) {
            throw new IllegalArgumentException("Invalid refund_id");
        }

        return refundRepository.getRefundById(refundId);
    }

    public List<RefundDetails> getRefunds(
            Integer year,
            Integer paymentId,
            String status) throws SQLException {

        return refundRepository.getRefunds(year, paymentId, status);
    }
}