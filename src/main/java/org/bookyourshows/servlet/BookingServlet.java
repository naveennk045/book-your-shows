package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.booking.*;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.BookingService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class BookingServlet extends HttpServlet {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    public BookingServlet() {
        this.bookingService = new BookingService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // /bookings  or  /bookings/
            if (parts.length == 2 && parts[1].equals("bookings")) {
                handleListAllBookings(response, userContext);
                return;
            }

            // /bookings/{id}  or  /bookings/{id}/status
            if (parts.length >= 3 && parts[1].equals("bookings")) {
                handleGetBooking(parts, response, userContext);
                return;
            }

            // /users/{userId}/bookings
            if (parts.length == 4 && parts[1].equals("users") && parts[3].equals("bookings")) {
                handleGetBookingsByUser(parts[2], response, userContext);
                return;
            }

            // /theatres/{theatreId}/bookings
            if (parts.length == 4 && parts[1].equals("theatres") && parts[3].equals("bookings")) {
                handleGetBookingsByTheatre(parts[2], response, userContext);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
            // /bookings/{id}/cancel
            if (parts.length == 4 && parts[1].equals("bookings") && parts[3].equals("cancel")) {
                handleCancelBooking(parts[2], request, response);
                return;
            }

            // /bookings
            if (parts.length == 2 && parts[1].equals("bookings")) {
                handleCreateBooking(request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    private void handleListAllBookings(HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), bookingService.getAllBookings());
    }

    private void handleGetBooking(String[] parts, HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int bookingId;
        try {
            bookingId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid booking id: " + parts[2]);
            return;
        }

        Optional<BookingDetails> details = bookingService.getBookingById(bookingId, userContext);

        if (details.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Booking not found");
            return;
        }

        // /bookings/{id}/status
        if (parts.length == 4 && "status".equals(parts[3])) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("status", details.get().getBooking().getBookingStatus()));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), details.get());
    }

    private void handleGetBookingsByUser(String userIdStr, HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {


        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user id");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), bookingService.getBookingsByUserId(userId, userContext));
    }

    private void handleGetBookingsByTheatre(String theatreIdStr, HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int theatreId;
        try {
            theatreId = Integer.parseInt(theatreIdStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), bookingService.getBookingsByTheatreId(theatreId, userContext));
    }

    private void handleCancelBooking(String bookingIdStr, HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        UserContext userContext = getUserContext(request);

        int bookingId;
        try {
            bookingId = Integer.parseInt(bookingIdStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid booking id");
            return;
        }

        Integer refundId = bookingService.cancelBooking(bookingId, userContext);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("refund_id", refundId));
    }

    private void handleCreateBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        BookingCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), BookingCreateRequest.class);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        UserContext userContext = getUserContext(request);
        int userId = userContext.getUserId();
        int bookingId = bookingService.createBooking(userId, createReq);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Booking created successfully", "booking_id", bookingId));
    }


    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }

    private UserContext getUserContext(HttpServletRequest request) {
        return (UserContext) request.getAttribute("userContext");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }
}