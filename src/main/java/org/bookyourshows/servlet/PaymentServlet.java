package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.payment.PaymentDetails;
import org.bookyourshows.dto.payment.PaymentInitiateRequest;
import org.bookyourshows.dto.payment.PaymentInitiateResponse;
import org.bookyourshows.dto.payment.PaymentWebhookPayload;
import org.bookyourshows.service.PaymentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaymentServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentServlet() {
        this.paymentService = new PaymentService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        try {

            String path = request.getPathInfo();
            String[] parts = path.split("/");

            // /booking/{booking_id}/payments
            if (parts.length == 4 && parts[3].equals("payments")) {
                Integer bookingId = Integer.parseInt(parts[2]);
                handlePaymentInitiation(request, response, bookingId);
            }

            // /fluxpay/{transaction_id}
            if (parts.length == 3 && parts[1].equals("fluxpay")) {
                System.out.println("flux :- " + Arrays.toString(parts));
                String gatewayTransactionId = parts[2];
                handlePaymentWebhook(request, response, gatewayTransactionId);
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid booking id format"));
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    private void handlePaymentWebhook(HttpServletRequest request, HttpServletResponse response, String gatewayTransactionId) throws IOException {

        try {
            PaymentWebhookPayload paymentWebhookPayload =
                    objectMapper.readValue(request.getReader(), PaymentWebhookPayload.class);

            this.paymentService.processPaymentWebhook(gatewayTransactionId, paymentWebhookPayload);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Webhook received"));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    private void handlePaymentInitiation(HttpServletRequest request, HttpServletResponse response, Integer bookingId) throws ServletException, IOException {

        try {
            PaymentInitiateRequest paymentInitiateRequest =
                    objectMapper.readValue(request.getReader(), PaymentInitiateRequest.class);

            PaymentInitiateResponse paymentInitiateResponse = this.paymentService.initiatePayment(bookingId, paymentInitiateRequest);
            System.out.println("Payment _ url :  " + paymentInitiateResponse.getPaymentUrl());
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), paymentInitiateResponse);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        String role = String.valueOf(request.getAttribute("user_role"));
        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Unauthorized"));
            return;
        }

        Integer year = parseInt(request.getParameter("year"));
        Integer month = parseInt(request.getParameter("month"));
        Integer bookingId = parseInt(request.getParameter("booking_id"));
        String status = request.getParameter("status");

        try {
            List<PaymentDetails> payments =
                    paymentService.getPayments(year, month, bookingId, status);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), payments);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid parameters  format"));
        }
    }

    private Integer parseInt(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
