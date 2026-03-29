package org.bookyourshows.repository;


import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.refund.RefundCreateRequest;
import org.bookyourshows.dto.refund.RefundDetails;
import org.bookyourshows.mapper.RefundMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefundRepository {

    public Integer createRefund(RefundCreateRequest request) throws SQLException {

        String query = """
                INSERT INTO refunds (transaction_id, amount, reason, gateway_refund_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, request.getTransactionId());
            preparedStatement.setDouble(2, request.getAmount());
            preparedStatement.setString(3, request.getReason());
            preparedStatement.setString(4, request.getGatewayRefundId());

            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) return resultSet.getInt(1);
            }
        }

        throw new RuntimeException("Failed to create refund");
    }


    public Optional<RefundDetails> getRefundById(int refundId) throws SQLException {

        String query = """
                SELECT *
                FROM refunds
                WHERE refund_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, refundId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(RefundMapper.mapRowToRefundDetails(resultSet));
            }
        }

        return Optional.empty();
    }

    public List<RefundDetails> getRefunds(
            Integer year,
            Integer paymentId,
            String status) throws SQLException {

        StringBuilder query = new StringBuilder("""
        SELECT * FROM refunds
        WHERE 1=1
    """);

        if (year != null) {
            query.append(" AND YEAR(created_at) = ?");
        }

        if (paymentId != null) {
            query.append(" AND transaction_id = ?");
        }

        if (status != null && !status.isBlank()) {
            query.append(" AND status = ?");
        }

        query.append(" ORDER BY created_at DESC");

        List<RefundDetails> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query.toString())) {

            int i = 1;

            if (year != null) ps.setInt(i++, year);
            if (paymentId != null) ps.setInt(i++, paymentId);
            if (status != null && !status.isBlank()) ps.setString(i++, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(RefundMapper.mapRowToRefundDetails(rs));
            }
        }

        return list;
    }


    public List<RefundDetails> getRefundsByTransactionId(int transactionId) throws SQLException {

        String query = """
                SELECT *
                FROM refunds
                WHERE transaction_id = ?
                ORDER BY created_at DESC
                """;

        List<RefundDetails> refunds = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, transactionId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                refunds.add(RefundMapper.mapRowToRefundDetails(resultSet));
            }
        }

        return refunds;
    }


    public boolean updateRefundStatus(int refundId, String status) throws SQLException {

        String query = """
                UPDATE refunds
                SET status = ?
                WHERE refund_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, refundId);

            return preparedStatement.executeUpdate() > 0;
        }
    }


    public boolean updateRefund(int refundId, String status, String reason) throws SQLException {

        String query = """
                UPDATE refunds
                SET status = ?, reason = ?
                WHERE refund_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, status);
            preparedStatement.setString(2, reason);
            preparedStatement.setInt(3, refundId);

            return preparedStatement.executeUpdate() > 0;
        }

    }
}
