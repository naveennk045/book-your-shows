package org.bookyourshows.dto.payment;

public class PaymentDetails {

    private Integer transactionId;
    private Integer bookingId;
    private String paymentGateWay;
    private String gatewayTransactionId;
    private Double amount;
    private String status;


    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentGateWay() {
        return paymentGateWay;
    }

    public void setPaymentGateWay(String paymentGateWay) {
        this.paymentGateWay = paymentGateWay;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
