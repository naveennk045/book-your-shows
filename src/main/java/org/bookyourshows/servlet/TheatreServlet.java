package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.TheatreCreateRequest;
import org.bookyourshows.dto.TheatreDetails;
import org.bookyourshows.dto.TheatreSummary;
import org.bookyourshows.dto.TheatreUpdateRequest;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@WebServlet("/theatres/*")
public class TheatreServlet extends HttpServlet {

    private TheatreService theatreService;
    private ObjectMapper objectMapper;

    public TheatreServlet() {
        this.theatreService = new TheatreService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        try {

            if (path != null && path.length() > 1) {

                int theatreId = Integer.parseInt(path.substring(1));
                Optional<TheatreDetails> theatreDetails = theatreService.getTheatreById(theatreId);

                if (theatreDetails.isPresent()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), theatreDetails.get());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre not found"));
                }
                return;
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Database error"));
            return;
        }


        String theatreName = request.getParameter("theatre_name");
        String city = request.getParameter("city");
        Integer limit = parseIntOrDefault(request.getParameter("limit"), 20);
        Integer offset = parseIntOrDefault(request.getParameter("offset"), 0);

        if (limit > 100 || limit < 0 || offset < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Limit is greater than 100,should be less than or equal to 100 "));
            return;
        }
        try {
            List<TheatreSummary> theatres = theatreService.getAllTheatre(limit, offset, theatreName, city);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), theatres);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Database error"));
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        TheatreCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), TheatreCreateRequest.class);
            int user_id = request.getIntHeader("user_id");

            if(user_id == -1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "User id is required"));
                return;
            }
            createReq.setOwnerId(user_id);
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            TheatreDetails created = theatreService.createTheatre(createReq);

            int id = created.getTheatre().getTheatreId();
            response.setStatus(HttpServletResponse.SC_CREATED);

            Map<String, Object> body = Map.of(
                    "message", "Theatre created successfully",
                    "theatre_id", id
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

        String path = request.getPathInfo();
        if (path == null || path.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id is required in path"));
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(path.substring(1));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre id: " + path.substring(1)));
            return;
        }

        TheatreUpdateRequest updateReq;
        try {
            updateReq = objectMapper.readValue(request.getReader(), TheatreUpdateRequest.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            boolean updated = theatreService.updateTheatre(theatreId, updateReq);

            if (!updated) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Theatre not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre updated successfully",
                            "theatre_id", theatreId));

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

        String path = request.getPathInfo();
        if (path == null || path.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id is required in path"));
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(path.substring(1));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre id: " + path.substring(1)));
            return;
        }

        try {
            boolean deleted = theatreService.deleteTheatre(theatreId);

            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Theatre not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre deleted successfully"));


        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }


    private Integer parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
