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
import org.bookyourshows.service.ScreenService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScreenServlet extends HttpServlet {

    private final ScreenService screenService;
    private final ObjectMapper objectMapper;

    public ScreenServlet() {
        this.screenService = new ScreenService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String path = request.getPathInfo();
        String[] part = path.split("/");


        int theatreId;

        try {
            theatreId = Integer.parseInt(part[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
            return;
        }

        // GET : /theatres/{theatre_id}/screens
        if (part.length == 4) {
            try {

                List<ScreenDetails> screenDetails = screenService.getScreensByTheatreId(theatreId);

                if (screenDetails.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "No screens found"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), screenDetails);
            } catch (RuntimeException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
                return;
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
                return;
            }
        }

        // GET :  /theatres/{theatre_id}/screens/{screen_id}
        int screenId;
        try {
            if (path.length() > 1) {
                screenId = Integer.parseInt(part[part.length - 1]);
                Optional<ScreenDetails> screenDetail = screenService.getScreensByScreenId(screenId, theatreId);
                if (screenDetail.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "No screen found"));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), screenDetail.get());
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid screen_id"));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Database error"));
            return;
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        // POST : /theatres/{theatre_id}/screens
        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        String path = request.getPathInfo();
        String[] part = path.split("/");
        UserContext userContext = (UserContext) request.getAttribute("userContext");


        int theatreId;

        try {
            theatreId = Integer.parseInt(part[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
            return;
        }

        ScreenCreateRequest screenCreateRequest;
        try {
            screenCreateRequest = objectMapper.readValue(request.getReader(), ScreenCreateRequest.class);
            screenCreateRequest.setTheatreId(theatreId);
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            int screenId = screenService.createScreen(screenCreateRequest, userContext);
            response.setStatus(HttpServletResponse.SC_CREATED);

            Map<String, Object> body = Map.of(
                    "message", "Screen created successfully",
                    "screen_id", screenId
            );
            objectMapper.writeValue(response.getWriter(), body);

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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        UserContext userContext = (UserContext) request.getAttribute("userContext");

        // /screens/{screen_id}
        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        String path = request.getPathInfo();
        String[] part = path.split("/");


        int theatreId;

        try {
            theatreId = Integer.parseInt(part[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
            return;
        }


        if (path.length() <= 4) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "screen id is required in path"));
            return;
        }


        int screenId;
        ScreenUpdateRequest screenUpdateRequest;

        try {
            screenId = Integer.parseInt(part[part.length - 1]);
            screenUpdateRequest = objectMapper.readValue(request.getReader(), ScreenUpdateRequest.class);

        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid screen_id"));
            return;
        }

        try {
            boolean screenDeleted = screenService.updateScreen(screenUpdateRequest, screenId, theatreId, userContext);
            if (!screenDeleted) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "failed to update the screen"));
            }
            response.setStatus(HttpServletResponse.SC_CREATED);

            Map<String, Object> body = Map.of(
                    "message", "Screen updated successfully",
                    "screen_id", screenId
            );
            objectMapper.writeValue(response.getWriter(), body);

        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (AccessDeniedException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Access denied"));
        }
    }
/*
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] part = path.split("/");

        UserContext userContext = (UserContext) request.getAttribute("userContext");

        // /screens/{screen_id}
        int theatreId;

        try {
            theatreId = Integer.parseInt(part[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
            return;
        }


        if (path.length() <= 4) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "screen id is required in path"));
            return;
        }

        int screenId;

        try {
            screenId = Integer.parseInt(part[part.length - 1]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid screen_id"));
            return;
        }

        try {
            boolean screenDeleted = screenService.deleteScreen(screenId, theatreId, userContext);

            if (!screenDeleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "Screen not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_CREATED);

            Map<String, Object> body = Map.of(
                    "message", "Screen Deleted successfully"
            );
            objectMapper.writeValue(response.getWriter(), body);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }*/
}
