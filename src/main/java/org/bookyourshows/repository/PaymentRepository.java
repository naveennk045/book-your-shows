package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.payment.PaymentInitiateRequest;
import org.bookyourshows.mapper.PaymentMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {


    public PaymentRepository() {

    }

    public Optional<PaymentDetails> getPaymentDetailsByGatewayTransactionId(String gatewayTransactionId) throws SQLException {
        String query = "SELECT * FROM payments WHERE gateway_transaction_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, gatewayTransactionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(PaymentMapper.mapRowToPaymentDetails(resultSet));
            }
        }
        return Optional.empty();
    }

    public Optional<PaymentDetails> getPaymentDetailsByBookingId(Integer bookingId) throws SQLException {
        String query = "SELECT * FROM payments WHERE booking_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, bookingId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(PaymentMapper.mapRowToPaymentDetails(resultSet));
            }
        }
        return Optional.empty();
    }

    public List<PaymentDetails> getPayments(
            Integer year,
            Integer month,
            Integer bookingId,
            String status) throws SQLException {

        StringBuilder query = new StringBuilder("""
        SELECT * FROM payments
        WHERE 1=1
    """);

        if (year != null) {
            query.append(" AND YEAR(created_at) = ?");
        }

        if (month != null) {
            query.append(" AND MONTH(created_at) = ?");
        }

        if (bookingId != null) {
            query.append(" AND booking_id = ?");
        }

        if (status != null && !status.isBlank()) {
            query.append(" AND status = ?");
        }

        query.append(" ORDER BY created_at DESC");

        List<PaymentDetails> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query.toString())) {

            int i = 1;

            if (year != null) ps.setInt(i++, year);
            if (month != null) ps.setInt(i++, month);
            if (bookingId != null) ps.setInt(i++, bookingId);
            if (status != null && !status.isBlank()) ps.setString(i++, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(PaymentMapper.mapRowToPaymentDetails(rs));
            }
        }

        return list;
    }


    public Integer createPayment(Integer bookingId, PaymentInitiateRequest paymentInitiateRequest) throws SQLException {

        String query = """
                INSERT INTO payments(booking_id, amount,payment_gateway,gateway_transaction_id)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, bookingId);
            preparedStatement.setDouble(2, paymentInitiateRequest.getAmount());
            preparedStatement.setString(3, paymentInitiateRequest.getPaymentGateway());
            preparedStatement.setString(4, paymentInitiateRequest.getPaymentGatewayTransactionId());

            int affected = preparedStatement.executeUpdate();
            if (affected == 0) throw new RuntimeException("Payment initiation failed");


            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }

        throw new RuntimeException("Payment initiation failed");
    }
/*

    public void updatePayment(Connection connection, PaymentWebhookPayload paymentWebhookPayload, String gatewayTransactionId) throws SQLException {

        String query = """
                        UPDATE payments
                        SET
                        payment_gateway = ?,
                        status = ?
                        WHERE gateway_transaction_id = ?;
                """;
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, paymentWebhookPayload.getPaymentMode() + "flux");
        preparedStatement.setString(2, paymentWebhookPayload.getStatus());
        preparedStatement.setString(3, gatewayTransactionId);

        int affected = preparedStatement.executeUpdate();

        if (affected == 0) {
            connection.rollback();
            throw new RuntimeException("Payment record failed");

        }
    }
*/

    public void updatePaymentStatus(Connection connection, Integer transactionId, String paymentStatus) throws SQLException {

        String query = """
                        UPDATE payments
                        SET
                        status = ?
                        WHERE transaction_id = ?;
                """;
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, paymentStatus);
        preparedStatement.setInt(2, transactionId);

        int affected = preparedStatement.executeUpdate();

        if (affected == 0) {
            connection.rollback();
            throw new RuntimeException("Payment refund update failed");

        }
    }
}

