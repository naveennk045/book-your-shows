package org.bookyourshows.dto.booking;

public class PaymentInitiateRequest {
    private String paymentGateway;
    private Double amount;


    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
