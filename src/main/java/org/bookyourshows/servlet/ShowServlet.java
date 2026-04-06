package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.show.*;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.ShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShowServlet extends HttpServlet {

    private final ShowService showService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(ShowServlet.class);


    public ShowServlet() {
        this.showService = new ShowService();
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

        try {
            // /shows/{showId}/seats
            if (parts.length == 4 && "seats".equals(parts[3])) {
                handleGetShowSeats(parts[2], response);
                return;
            }

            // /shows/{showId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleGetShowById(parts[2], response);
                return;
            }

            // /shows?theatre_id=&show_date=&movie_id=&location=
            handleListShows(request, response);

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/screens/{screenId}/shows
            if (parts.length == 6 && "theatres".equals(parts[1])
                    && "screens".equals(parts[3]) && "shows".equals(parts[5])) {
                handleCreateShow(parts[2], parts[4], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /shows/{showId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleUpdateShow(parts[2], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "show_id is required");

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleGetShowSeats(String showIdStr, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int showId = parseId(showIdStr, "Invalid show_id", response);
        if (showId == -1) return;

        List<ShowSeatingResponse> seats = showService.getShowSeats(showId);

        if (seats.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), seats);
    }

    private void handleGetShowById(String showIdStr, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int showId = parseId(showIdStr, "Invalid show_id", response);
        if (showId == -1) return;

        Optional<ShowDetails> show = showService.getShowById(showId);

        if (show.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Show not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), show.get());
    }

    private void handleListShows(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        String theatreIdParam = request.getParameter("theatre_id");
        String dateParam = request.getParameter("show_date");
        String movieIdParam = request.getParameter("movie_id");
        String locationParam = request.getParameter("location");

        if (dateParam == null || movieIdParam == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Required parameters are missing");
            return;
        }

        Integer theatreId = null;
        if (theatreIdParam != null) {
            int parsed = parseId(theatreIdParam, "Invalid theatre_id", response);
            if (parsed == -1) return;
            theatreId = parsed;
        }

        Date date;
        try {
            date = Date.valueOf(dateParam);
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid show_date format. Expected yyyy-MM-dd");
            return;
        }

        int movieId = parseId(movieIdParam, "Invalid movie_id", response);
        if (movieId == -1) return;

        List<TheatreShowsResponse> shows = showService.getShows(theatreId, locationParam, date, movieId);

        if (shows.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                shows.isEmpty() ? Map.of("message", "No live shows found") : shows);
    }

    private void handleCreateShow(String theatreIdStr, String screenIdStr,
                                  HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        UserContext userContext = getUserContext(request);
        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        int screenId = parseId(screenIdStr, "Invalid screen_id", response);
        if (screenId == -1) return;

        ShowCreateRequest showCreateRequest;
        try {
            showCreateRequest = objectMapper.readValue(request.getReader(), ShowCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        showCreateRequest.setTheatreId(theatreId);
        showCreateRequest.setScreenId(screenId);

        int showId = showService.createShow(showCreateRequest, userContext);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Show created successfully", "show_id", showId));
    }

    private void handleUpdateShow(String showIdStr, HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int showId = parseId(showIdStr, "Invalid show_id", response);
        if (showId == -1) return;

        ShowUpdateRequest showUpdateRequest;
        try {
            showUpdateRequest = objectMapper.readValue(request.getReader(), ShowUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        boolean updated = showService.updateShow(showId, showUpdateRequest, getUserContext(request));

        if (!updated) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Show not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Show updated successfully", "show_id", showId));
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