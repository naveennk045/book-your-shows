package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.screen.ScreenUpdateRequest;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.ScreenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScreenServlet extends HttpServlet {

    private final ScreenService screenService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(ScreenServlet.class);


    public ScreenServlet() {
        this.screenService = new ScreenService();
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
            // /theatres/{theatreId}/screens/{screenId}
            if (parts.length == 5 && "screens".equals(parts[3])) {
                handleGetScreenById(parts[2], parts[4], response);
                return;
            }

            // /theatres/{theatreId}/screens
            if (parts.length == 4 && "screens".equals(parts[3])) {
                handleListScreens(parts[2], response);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/screens
            if (parts.length == 4 && "screens".equals(parts[3])) {
                handleCreateScreen(parts[2], request, response);
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);

        try {
            // /theatres/{theatreId}/screens/{screenId}
            if (parts.length == 5 && "screens".equals(parts[3])) {
                handleUpdateScreen(parts[2], parts[4], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "screen_id is required");

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


    private void handleListScreens(String theatreIdStr, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        List<ScreenDetails> screenDetails = screenService.getScreensByTheatreId(theatreId);

        if (screenDetails.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                screenDetails.isEmpty() ? Map.of("message", "No screens found") : screenDetails);
    }

    private void handleGetScreenById(String theatreIdStr, String screenIdStr,
                                     HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        int screenId = parseId(screenIdStr, "Invalid screen_id", response);
        if (screenId == -1) return;

        Optional<ScreenDetails> screenDetail = screenService.getScreensByScreenId(screenId, theatreId);

        if (screenDetail.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Screen not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), screenDetail.get());
    }

    private void handleCreateScreen(String theatreIdStr, HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        ScreenCreateRequest screenCreateRequest;
        try {
            screenCreateRequest = objectMapper.readValue(request.getReader(), ScreenCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        screenCreateRequest.setTheatreId(theatreId);

        UserContext userContext = getUserContext(request);
        int screenId = screenService.createScreen(screenCreateRequest, userContext);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Screen created successfully", "screen_id", screenId));
    }

    private void handleUpdateScreen(String theatreIdStr, String screenIdStr,
                                    HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int theatreId = parseId(theatreIdStr, "Invalid theatre_id", response);
        if (theatreId == -1) return;

        int screenId = parseId(screenIdStr, "Invalid screen_id", response);
        if (screenId == -1) return;

        ScreenUpdateRequest screenUpdateRequest;
        try {
            screenUpdateRequest = objectMapper.readValue(request.getReader(), ScreenUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        UserContext userContext = getUserContext(request);
        boolean updated = screenService.updateScreen(screenUpdateRequest, screenId, theatreId, userContext);

        if (!updated) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Screen not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Screen updated successfully", "screen_id", screenId));
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