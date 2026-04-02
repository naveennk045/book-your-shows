package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.MovieFeedbackService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MovieFeedbackServlet extends HttpServlet {

    private final MovieFeedbackService movieFeedbackService;
    private final ObjectMapper objectMapper;

    public MovieFeedbackServlet() {
        this.movieFeedbackService = new MovieFeedbackService();
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
            // /movies/{movieId}/feedbacks
            if (parts.length == 4 && "feedbacks".equals(parts[3])) {
                handleListFeedbacks(parts[2], request, response);
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
            // /movies/{movieId}/feedbacks
            if (parts.length == 4 && "feedbacks".equals(parts[3])) {
                handleCreateFeedback(parts[2], request, response);
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
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /movies/{movieId}/feedbacks/{ratingId}
            if (parts.length == 5 && "feedbacks".equals(parts[3])) {
                handleUpdateFeedback(parts[2], parts[4], request, response);
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /movies/{movieId}/feedbacks/{ratingId}
            if (parts.length == 5 && "feedbacks".equals(parts[3])) {
                handleDeleteFeedback(parts[2], parts[4], response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    private void handleListFeedbacks(String movieIdStr, HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id", response);
        if (movieId == -1) return;

        int limit = parseIntOrDefault(request.getParameter("limit"), 20);
        int offset = parseIntOrDefault(request.getParameter("offset"), 0);

        if (limit > 100 || limit < 0 || offset < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pagination values");
            return;
        }

        List<MovieFeedbackResponse> list = movieFeedbackService.getFeedbacksForMovie(movieId, limit, offset);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                list.isEmpty() ? Map.of("message", "No feedbacks") : list);
    }

    private void handleCreateFeedback(String movieIdStr, HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id", response);
        if (movieId == -1) return;

        MovieFeedbackCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), MovieFeedbackCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        MovieFeedbackResponse created = movieFeedbackService.createFeedback(movieId, createReq);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Feedback created successfully", "rating_id", created.getRatingId()));
    }

    private void handleUpdateFeedback(String movieIdStr, String ratingIdStr,
                                      HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id or rating_id", response);
        if (movieId == -1) return;

        int ratingId = parseId(ratingIdStr, "Invalid movie_id or rating_id", response);
        if (ratingId == -1) return;

        MovieFeedbackUpdateRequest updateReq;
        try {
            updateReq = objectMapper.readValue(request.getReader(), MovieFeedbackUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        movieFeedbackService.updateFeedback(movieId, ratingId, updateReq);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Feedback updated successfully", "rating_id", ratingId));
    }

    private void handleDeleteFeedback(String movieIdStr, String ratingIdStr,
                                      HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id or rating_id", response);
        if (movieId == -1) return;

        int ratingId = parseId(ratingIdStr, "Invalid movie_id or rating_id", response);
        if (ratingId == -1) return;

        boolean deleted = movieFeedbackService.deleteFeedback(movieId, ratingId);

        if (!deleted) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Feedback not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "Feedback deleted successfully"));
    }


    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
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

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}