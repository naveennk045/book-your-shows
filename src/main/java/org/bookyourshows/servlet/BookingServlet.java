package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.booking.*;
import org.bookyourshows.service.BookingService;

import java.io.IOException;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
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

        String path = request.getPathInfo();
        if (path == null || !path.startsWith("/bookings")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Not found"));
            return;
        }

        String remainder = path.substring("/bookings".length());
        if (remainder.isEmpty() || "/".equals(remainder)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "booking_id is required in path"));
            return;
        }

        //  /bookings/{booking_id}

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


        try {
            Optional<BookingDetails> details = bookingService.getBookingById(bookingId);
            System.out.println(Arrays.toString(parts));
            if (details.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Booking not found"));
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);

            if (parts.length == 3 && Objects.equals(parts[2], "status")) {
                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        Map.of(
                                "status", details.get().getBooking().getBookingStatus()
                        ));
                return;
            }

            objectMapper.writeValue(response.getWriter(), details.get());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
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
            Integer bookingId = Integer.parseInt(parts[2]);



        }


        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }
        // /bookings
        if (path == null || !path.startsWith("/bookings")) {
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

        int userId = request.getIntHeader("user_id");
        if (userId == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "user_id header is required"));
            return;
        }

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
        }
    }
}

