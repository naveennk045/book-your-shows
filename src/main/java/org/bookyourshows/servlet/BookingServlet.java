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
import java.util.Objects;
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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        String path = request.getPathInfo();

        try {

            if (path == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "No route found"));
                return;
            }

            // 1. ADMIN → /bookings
            if (path.equals("/bookings") || path.equals("/bookings/")) {

                String userRole = userContext.getUserRole();

                if (!"ADMIN".equals(userRole)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Access denied"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        bookingService.getAllBookings());
                return;
            }

            // 2. USER → /users/{user_id}/bookings
            if (path.startsWith("/users/")) {

                String[] parts = path.split("/");

                if (parts.length < 4 || !"bookings".equals(parts[3])) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "No route found"));
                    return;
                }

                int userId;
                try {
                    userId = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Invalid user id"));
                    return;
                }

                // Optional security check
                Integer loggedInUser = userContext.getUserId();
                if (loggedInUser != null && !loggedInUser.equals(userId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Access denied"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        bookingService.getBookingsByUserId(userId, userContext));
                return;
            }

            // 3. THEATRE → /theatres/{id}/bookings
            if (path.startsWith("/theatres/")) {

                String[] parts = path.split("/");

                if (parts.length < 4 || !"bookings".equals(parts[3])) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Invalid theatre bookings path"));
                    return;
                }

                int theatreId;
                try {
                    theatreId = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Invalid theatre id"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        bookingService.getBookingsByTheatreId(theatreId, userContext));
                return;
            }

            // 4.  BOOKING → /bookings/{id}
            if (path.startsWith("/bookings/")) {

                String remainder = path.substring("/bookings".length());
                String[] parts = remainder.split("/");

                if (parts.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "booking_id is required in path"));
                    return;
                }

                int bookingId;
                try {
                    bookingId = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Invalid booking id: " + parts[1]));
                    return;
                }

                Optional<BookingDetails> details = bookingService.getBookingById(bookingId, userContext);

                if (details.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Booking not found"));
                    return;
                }

                // /bookings/{id}/status
                if (parts.length == 3 && "status".equals(parts[2])) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("status",
                                    details.get().getBooking().getBookingStatus()));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), details.get());
                return;
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Not found"));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String path = request.getPathInfo();
        String[] parts = path.split("/");

        if (parts.length == 4 && Objects.equals(parts[3], "cancel")) {
            handleBookingCancellation(request, response);
            return;
        }

        // /bookings
        if (!path.startsWith("/bookings")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Not found"));
            return;
        }

        String remainder = path.substring("/bookings".length());
        if (remainder.isEmpty() || "/".equals(remainder)) {
            // POST /bookings -> create booking
            handleCreateBooking(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Not found"));
        }
    }

    private void handleBookingCancellation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UserContext userContext = (UserContext) request.getAttribute("userContext");

            String[] parts = request.getPathInfo().split("/");
            Integer bookingId = Integer.parseInt(parts[2]);
            Integer refundId = bookingService.cancelBooking(bookingId, userContext);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("refund_id", refundId));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid booking id"));
        } catch (RuntimeException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }

    }

    private void handleCreateBooking(HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {

        BookingCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), BookingCreateRequest.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }
        UserContext userContext = (UserContext) request.getAttribute("userContext");
        int userId = userContext.getUserId();

        try {
            int bookingId = bookingService.createBooking(userId, createReq);
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of(
                            "message", "Booking created successfully",
                            "booking_id", bookingId
                    ));
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }
}


