package org.bookyourshows.mapper;

import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.payment.PaymentInitiateResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentMapper {

    public static PaymentInitiateResponse mapRowToPaymentInitiateResponse(ResultSet resultSet) throws SQLException {

        PaymentInitiateResponse paymentInitiateResponse = new PaymentInitiateResponse();

        paymentInitiateResponse.setBookingId(resultSet.getInt("booking_id"));
        paymentInitiateResponse.setAmount(resultSet.getDouble("amount"));
        paymentInitiateResponse.setPaymentUrl(resultSet.getString("payment_url"));
        paymentInitiateResponse.setTransactionId(resultSet.getInt("transaction_id"));

        return paymentInitiateResponse;
    }

    public static PaymentDetails mapRowToPayementDetails(ResultSet resultSet) throws SQLException {

        PaymentDetails paymentDetails = new PaymentDetails();

        paymentDetails.setTransactionId(resultSet.getInt("transaction_id"));
        paymentDetails.setBookingId(resultSet.getInt("booking_id"));
        paymentDetails.setAmount(resultSet.getDouble("amount"));
        paymentDetails.setPaymentGateWay(resultSet.getString("payment_gateway"));
        paymentDetails.setGatewayTransactionId(resultSet.getString("gateway_transaction_id"));
        paymentDetails.setStatus(resultSet.getString("status"));
        return paymentDetails;


    }

}
