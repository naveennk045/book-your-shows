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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TheatreFeedbackServlet extends HttpServlet {

    private final TheatreFeedbackService theatreFeedbackService;
    private final TheatreService theatreService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(TheatreFeedbackServlet.class);

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

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/feedbacks
            if (parts.length == 4 && "feedbacks".equals(parts[3])) {
                handleListFeedbacks(parts[2], request, response);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/feedbacks
            if (parts.length == 4 && "feedbacks".equals(parts[3])) {
                handleCreateFeedback(parts[2], request, response);
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
            // /theatres/{theatreId}/feedbacks/{ratingId}
            if (parts.length == 5 && "feedbacks".equals(parts[3])) {
                handleUpdateFeedback(parts[2], parts[4], request, response);
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/feedbacks/{ratingId}
            if (parts.length == 5 && "feedbacks".equals(parts[3])) {
                handleDeleteFeedback(parts[2], parts[4], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }


    private void handleListFeedbacks(String theatreIdStr, HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        if (!theatreExists(theatreId, response)) return;

        int limit = parseIntOrDefault(request.getParameter("limit"), 20);
        int offset = parseIntOrDefault(request.getParameter("offset"), 0);

        if (limit > 100 || limit < 0 || offset < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pagination values");
            return;
        }

        List<TheatreFeedbackResponse> list =
                theatreFeedbackService.getFeedbacksForTheatre(theatreId, limit, offset);

        if (list.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                list.isEmpty() ? Map.of("message", "No feedbacks") : list);
    }

    private void handleCreateFeedback(String theatreIdStr, HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        TheatreFeedbackCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), TheatreFeedbackCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        if (!theatreExists(theatreId, response)) return;

        TheatreFeedbackResponse created = theatreFeedbackService.createFeedback(theatreId, createReq);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Feedback created successfully", "rating_id", created.getRatingId()));
    }

    private void handleUpdateFeedback(String theatreIdStr, String ratingIdStr,
                                      HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id or rating_id", response);
        if (theatreId == -1) return;

        int ratingId = parseId(ratingIdStr, "Invalid theatre_id or rating_id", response);
        if (ratingId == -1) return;

        TheatreFeedbackUpdateRequest updateReq;
        try {
            updateReq = objectMapper.readValue(request.getReader(), TheatreFeedbackUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        if (!theatreExists(theatreId, response)) return;

        theatreFeedbackService.updateFeedback(theatreId, ratingId, updateReq);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Feedback updated successfully", "rating_id", ratingId));
    }

    private void handleDeleteFeedback(String theatreIdStr, String ratingIdStr,
                                      HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id or rating_id", response);
        if (theatreId == -1) return;

        int ratingId = parseId(ratingIdStr, "Invalid theatre_id or rating_id", response);
        if (ratingId == -1) return;

        UserContext userContext = getUserContext(request);

        boolean deleted = theatreFeedbackService.deleteFeedback(theatreId, ratingId, userContext);

        if (!deleted) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Feedback not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "Feedback deleted successfully"));
    }


    private boolean theatreExists(int theatreId, HttpServletResponse response)
            throws IOException, SQLException {

        Optional<TheatreDetails> theatreDetails = theatreService.getTheatreById(theatreId);
        if (theatreDetails.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "The theatre id " + theatreId + " does not exist");
            return false;
        }
        return true;
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

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}