package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.refund.RefundDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.RefundService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RefundServlet extends HttpServlet {

    private final RefundService refundService;
    private final ObjectMapper objectMapper;

    public RefundServlet() {
        this.refundService = new RefundService();
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

        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // /refunds
            if (parts.length == 2 && "refunds".equals(parts[1])) {
                handleListRefunds(request, response, userContext);
                return;
            }

            // /refunds/{refundId}
            if (parts.length == 3 && "refunds".equals(parts[1])) {
                handleGetRefundById(parts[2], response, userContext);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    private void handleListRefunds(HttpServletRequest request, HttpServletResponse response,
                                   UserContext userContext)
            throws IOException, SQLException, CustomException {

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        Integer year = parseNullableInt(request.getParameter("year"));
        Integer paymentId = parseNullableInt(request.getParameter("payment_id"));
        String status = request.getParameter("status");

        List<RefundDetails> refunds = refundService.getRefunds(year, paymentId, status);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), refunds);
    }

    private void handleGetRefundById(String refundIdStr, HttpServletResponse response,
                                     UserContext userContext)
            throws IOException, SQLException, CustomException {

        int refundId = parseId(refundIdStr, "Invalid refund id", response);
        if (refundId == -1) return;

        Optional<RefundDetails> refundDetails = refundService.getRefundById(refundId, userContext);

        if (refundDetails.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Refund not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), refundDetails.get());
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