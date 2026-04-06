package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.seat.SeatCreateRequest;
import org.bookyourshows.dto.seat.SeatRowResponse;
import org.bookyourshows.dto.seat.SeatUpdateRequest;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.SeatService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SeatServlet extends HttpServlet {

    private final SeatService seatService;
    private final ObjectMapper objectMapper;

    public SeatServlet() {
        this.seatService = new SeatService();
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

        try {
            // /theatres/{theatreId}/screens/{screenId}/seats
            if (parts.length == 6 && "theatres".equals(parts[1])
                    && "screens".equals(parts[3]) && "seats".equals(parts[5])) {
                handleListSeats(parts[2], parts[4], response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

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
            // /theatres/{theatreId}/screens/{screenId}/seats
            if (parts.length == 6 && "theatres".equals(parts[1])
                    && "screens".equals(parts[3]) && "seats".equals(parts[5])) {
                handleCreateSeats(parts[2], parts[4], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /seats/{seatId}
            if (parts.length == 3 && "seats".equals(parts[1])) {
                handleUpdateSeat(parts[2], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "seat_id is required");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }


    private void handleListSeats(String theatreIdStr, String screenIdStr,
                                 HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        int screenId = parseId(screenIdStr, "Invalid screen_id", response);
        if (screenId == -1) return;

        List<SeatRowResponse> seatLayout = seatService.getSeatsByScreenId(screenId, theatreId);

        if (seatLayout.isEmpty()) {
            writeError(response, HttpServletResponse.SC_OK, "Seats not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), seatLayout);
    }

    private void handleCreateSeats(String theatreIdStr, String screenIdStr,
                                   HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        int screenId = parseId(screenIdStr, "Invalid screen_id", response);
        if (screenId == -1) return;

        List<SeatCreateRequest> seats;
        try {
            seats = objectMapper.readValue(request.getReader(), new TypeReference<List<SeatCreateRequest>>() {});
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        UserContext userContext = getUserContext(request);
        seatService.createSeat(seats, screenId, theatreId, userContext);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "Seats created successfully"));
    }

    private void handleUpdateSeat(String seatIdStr, HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int seatId = parseId(seatIdStr, "Invalid seat_id", response);
        if (seatId == -1) return;

        SeatUpdateRequest seatUpdateRequest;
        try {
            seatUpdateRequest = objectMapper.readValue(request.getReader(), SeatUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        UserContext userContext = getUserContext(request);
        boolean updated = seatService.updateSeat(seatId, seatUpdateRequest, userContext);

        if (!updated) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Seat not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "Seat updated successfully"));
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

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }
}