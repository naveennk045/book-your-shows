package org.bookyourshows.mapper;

import org.bookyourshows.dto.refund.RefundDetails;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RefundMapper {

    public static RefundDetails mapRowToRefundDetails(ResultSet resultSet) throws SQLException {

        RefundDetails refundDetails = new RefundDetails();

        refundDetails.setRefundId(resultSet.getInt("refund_id"));
        refundDetails.setTransactionId(resultSet.getInt("transaction_id"));
        refundDetails.setAmount(resultSet.getDouble("amount"));
        refundDetails.setReason(resultSet.getString("reason"));
        refundDetails.setStatus(resultSet.getString("status"));
        refundDetails.setGatewayRefundId(resultSet.getString("gateway_refund_id"));
        refundDetails.setCreatedAt(resultSet.getTimestamp("created_at"));
        refundDetails.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return refundDetails;
    }
}