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
import org.bookyourshows.service.MovieFeedbackService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
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

        String path = request.getPathInfo(); // /movies/{movie_id}/feedback
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("MovieFeedbackServlet GET path parts: " + Arrays.toString(parts));

        // /movies/{movie_id}/feedback
        if (parts.length == 4 && "feedbacsk".equals(parts[3])) {

            int movieId;
            try {
                movieId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid movie_id"));
                return;
            }

            Integer limit = parseIntOrDefault(request.getParameter("limit"), 20);
            Integer offset = parseIntOrDefault(request.getParameter("offset"), 0);

            if (limit > 100 || limit < 0 || offset < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid pagination values"));
                return;
            }

            try {
                List<MovieFeedbackResponse> list =
                        movieFeedbackService.getFeedbacksForMovie(movieId, limit, offset);

                if (list.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "No feedbacks"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), list);

            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Database error"));
            }

            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Not found"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // /movies/{movie_id}/feedback
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("MovieFeedbackServlet POST path parts: " + Arrays.toString(parts));

        if (!(parts.length == 4 && "feedbacks".equals(parts[3]))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL for creating feedback"));
            return;
        }

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {

            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        int movieId;
        MovieFeedbackCreateRequest createReq;

        try {
            movieId = Integer.parseInt(parts[2]);
            createReq = objectMapper.readValue(request.getReader(), MovieFeedbackCreateRequest.class);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            MovieFeedbackResponse created =
                    movieFeedbackService.createFeedback(movieId, createReq);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of(
                            "message", "Feedback created successfully",
                            "rating_id", created.getRatingId()
                    ));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {

            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        String path = request.getPathInfo(); // /movies/{movie_id}/feedback/{rating_id}
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id and rating id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("MovieFeedbackServlet PUT path parts: " + Arrays.toString(parts));

        if (!(parts.length == 5 && "feedbacks".equals(parts[3]))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL for updating feedback"));
            return;
        }

        int movieId;
        int ratingId;
        MovieFeedbackUpdateRequest updateReq;

        try {
            movieId = Integer.parseInt(parts[2]);
            ratingId = Integer.parseInt(parts[4]);
            updateReq = objectMapper.readValue(request.getReader(), MovieFeedbackUpdateRequest.class);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id or rating_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            movieFeedbackService.updateFeedback(movieId, ratingId, updateReq);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of(
                            "message", "Feedback updated successfully",
                            "rating_id", ratingId
                    ));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // /movies/{movie_id}/feedback/{rating_id}
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id and rating id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("MovieFeedbackServlet DELETE path parts: " + Arrays.toString(parts));

        if (!(parts.length == 5 && "feedbacks".equals(parts[3]))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL for deleting feedback"));
            return;
        }

        int movieId;
        int ratingId;

        try {
            movieId = Integer.parseInt(parts[2]);
            ratingId = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id or rating_id"));
            return;
        }

        try {
            boolean deleted = movieFeedbackService.deleteFeedback(movieId, ratingId);

            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Feedback not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Feedback deleted successfully"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    private Integer parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}