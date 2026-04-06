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
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.PaymentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PaymentServlet extends HttpServlet {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentServlet() {
        this.paymentService = new PaymentService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        UserContext userContext = getUserContext(request);

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            handleListPayments(request, response);
        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /bookings/{bookingId}/payments
            if (parts.length == 4 && "bookings".equals(parts[1]) && "payments".equals(parts[3])) {
                handlePaymentInitiation(parts[2], request, response);
                return;
            }

            // /fluxpay/{transactionId}
            if (parts.length == 3 && "fluxpay".equals(parts[1])) {
                handlePaymentWebhook(parts[2], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }


    private void handleListPayments(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        Integer year = parseNullableInt(request.getParameter("year"));
        Integer month = parseNullableInt(request.getParameter("month"));
        Integer bookingId = parseNullableInt(request.getParameter("booking_id"));
        String status = request.getParameter("status");

        List<PaymentDetails> payments = paymentService.getPayments(year, month, bookingId, status);

        if (payments.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), payments);
    }

    private void handlePaymentInitiation(String bookingIdStr, HttpServletRequest request,
                                         HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int bookingId = parseId(bookingIdStr, "Invalid booking id", response);
        if (bookingId == -1) return;

        PaymentInitiateRequest paymentInitiateRequest;
        try {
            paymentInitiateRequest = objectMapper.readValue(request.getReader(), PaymentInitiateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        UserContext userContext = getUserContext(request);
        PaymentInitiateResponse paymentInitiateResponse =
                paymentService.initiatePayment(bookingId, paymentInitiateRequest, userContext);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), paymentInitiateResponse);
    }

    private void handlePaymentWebhook(String gatewayTransactionId, HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        PaymentWebhookPayload payload;
        try {
            payload = objectMapper.readValue(request.getReader(), PaymentWebhookPayload.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        paymentService.processPaymentWebhook(gatewayTransactionId, payload);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "Webhook received"));
    }


    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }

    private UserContext getUserContext(HttpServletRequest request) {
        return (UserContext) request.getAttribute("userContext");
    }

    private int parseId(String idStr, String errorMessage, HttpServletResponse response)
            throws IOException {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            return -1;
        }
    }

    private Integer parseNullableInt(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }
}