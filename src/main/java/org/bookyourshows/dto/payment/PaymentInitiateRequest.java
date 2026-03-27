package org.bookyourshows.dto.payment;

public class PaymentInitiateRequest {
    private Double amount;
    private String paymentGateway;
    private String paymentGatewayTransactionId;

    public Double getAmount() {
        return amount;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentGatewayTransactionId() {
        return paymentGatewayTransactionId;
    }

    public void setPaymentGatewayTransactionId(String paymentGatewayTransactionId) {
        this.paymentGatewayTransactionId = paymentGatewayTransactionId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
