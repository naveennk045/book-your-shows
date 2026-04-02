package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.TheatreFeedbackService;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TheatreFeedbackServlet extends HttpServlet {

    private final TheatreFeedbackService theatreFeedbackService;
    private final ObjectMapper objectMapper;
    private final TheatreService theatreService;

    public TheatreFeedbackServlet() {
        this.theatreFeedbackService = new TheatreFeedbackService();
        this.theatreService = new TheatreService();
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

        String path = request.getPathInfo(); // /theatres/{theatre_id}/feedback
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("TheatreFeedbackServlet GET path parts: " + Arrays.toString(parts));

        if (parts.length == 4 && "feedbacks".equals(parts[3])) {

            int theatreId;
            try {
                theatreId = Integer.parseInt(parts[2]);
                Optional<TheatreDetails> theatreDetails = this.theatreService.getTheatreById(theatreId);
                if (theatreDetails.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                            "message", "The theatre id " + theatreId + " does not exist"
                    ));
                    return;
                }

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid theatre_id"));
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
                List<TheatreFeedbackResponse> list =
                        theatreFeedbackService.getFeedbacksForTheatre(theatreId, limit, offset);

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

        String path = request.getPathInfo(); // /theatres/{theatre_id}/feedback
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("TheatreFeedbackServlet POST path parts: " + Arrays.toString(parts));

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

        int theatreId;
        TheatreFeedbackCreateRequest createReq;

        try {
            theatreId = Integer.parseInt(parts[2]);
            createReq = objectMapper.readValue(request.getReader(), TheatreFeedbackCreateRequest.class);

            Optional<TheatreDetails> theatreDetails = this.theatreService.getTheatreById(theatreId);
            if (theatreDetails.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "message", "The theatre id " + theatreId + " does not exist"
                ));
                return;
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            TheatreFeedbackResponse created =
                    theatreFeedbackService.createFeedback(theatreId, createReq);

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
                    Map.of("message", e.getMessage()));
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {

            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        String path = request.getPathInfo(); // /theatres/{theatre_id}/feedback/{rating_id}
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id and rating id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("TheatreFeedbackServlet PUT path parts: " + Arrays.toString(parts));

        if (!(parts.length == 5 && "feedbacks".equals(parts[3]))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL for updating feedback"));
            return;
        }

        int theatreId;
        int ratingId;
        TheatreFeedbackUpdateRequest updateReq;


        try {
            theatreId = Integer.parseInt(parts[2]);
            ratingId = Integer.parseInt(parts[4]);

            Optional<TheatreDetails> theatreDetails = this.theatreService.getTheatreById(theatreId);
            if (theatreDetails.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "message", "The theatre id " + theatreId + " does not exist"
                ));
                return;
            }

            updateReq = objectMapper.readValue(request.getReader(), TheatreFeedbackUpdateRequest.class);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre_id or rating_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            theatreFeedbackService.updateFeedback(theatreId, ratingId, updateReq);

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
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        UserContext userContext = (UserContext) request.getSession().getAttribute("userContext");

        String path = request.getPathInfo(); // /theatres/{theatre_id}/feedback/{rating_id}
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id and rating id required"));
            return;
        }

        String[] parts = path.split("/");
        System.out.println("TheatreFeedbackServlet DELETE path parts: " + Arrays.toString(parts));

        if (!(parts.length == 5 && "feedbacks".equals(parts[3]))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL for deleting feedback"));
            return;
        }

        int theatreId;
        int ratingId;

        try {
            theatreId = Integer.parseInt(parts[2]);
            ratingId = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre_id or rating_id"));
            return;
        }

        try {
            boolean deleted = theatreFeedbackService.deleteFeedback(theatreId, ratingId, userContext);

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
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
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