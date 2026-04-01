package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@WebServlet("/theatres/*")
public class TheatreServlet extends HttpServlet {

    private final TheatreService theatreService;
    private final ObjectMapper objectMapper;

    public TheatreServlet() {
        this.theatreService = new TheatreService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fullPath = request.getPathInfo();
        String[] parts = fullPath.split("/");


        try {

            // 1. GET /theatres/{id}/address
            if (parts.length == 4 && "address".equals(parts[3])) {

                int theatreId;
                try {
                    theatreId = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Invalid theatre id"));
                    return;
                }

                Optional<Address> address = theatreService.getTheatreAddress(theatreId);

                if (address.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Address not found"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), address.get());
                return;
            }


            // 2. GET /theatres/{id}
            String path = fullPath.substring("/theatres".length());

            if (path.length() > 1) {

                int theatreId = Integer.parseInt(path.substring(1));
                Optional<TheatreDetails> theatreDetails = theatreService.getTheatreById(theatreId);

                if (theatreDetails.isPresent()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), theatreDetails.get());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Theatre not found"));
                }
                return;
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre_id"));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
            return;
        }
        UserContext userContext = (UserContext) request.getAttribute("userContext");

        // 3. LIST THEATRES
        String theatreName = request.getParameter("theatre_name");
        String city = request.getParameter("city");
        String status = request.getParameter("status");
        Integer limit = parseIntOrDefault(request.getParameter("limit"), 20);
        Integer offset = parseIntOrDefault(request.getParameter("offset"), 0);

        if (limit > 100 || limit < 0 || offset < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid pagination"));
            return;
        }

        try {
            List<TheatreSummary> theatres =
                    theatreService.getAllTheatre(limit, offset, theatreName, city, status, userContext);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), theatres);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        UserContext userContext = (UserContext) request.getAttribute("userContext");
        // POST : /theatres

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
            int user_id = userContext.getUserId();

            if (user_id == -1) {
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
                    Map.of("message", e.getMessage()));
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

        String fullPath = request.getPathInfo();
        String[] parts = fullPath.split("/");

        int ownerId = (int) request.getAttribute("user_id");
        String userRole = (String) request.getAttribute("user_role");


        // 1. PUT /theatres/{id}/address
        if (parts.length == 4 && "address".equals(parts[3])) {

            int theatreId;
            try {
                theatreId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid theatre id"));
                return;
            }

            Address req;
            try {
                req = objectMapper.readValue(request.getReader(), Address.class);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid JSON body"));
                return;
            }

            try {
                theatreService.updateTheatreAddress(theatreId, ownerId, userRole, req);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Theatre address updated successfully"));

            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", e.getMessage()));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Database error"));
            }

            return;
        }


        // 2. PUT /theatres/{id}
        String path = fullPath.substring("/theatres".length());

        if (path.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id is required"));
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(path.substring(1));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre id"));
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
            boolean updated = theatreService.updateTheatre(theatreId, ownerId, userRole, updateReq);

            if (!updated) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Theatre not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre updated successfully"));

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

        // DELETE : /theatres/{theatre_id}

        String path = request.getPathInfo().substring("/theatres".length());
        if (path.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre id is required in path"));
            return;
        }

        int userId = (int) request.getAttribute("user_id");
        String userRole = (String) request.getAttribute("user_role");
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
            boolean deleted = theatreService.deleteTheatre(theatreId, userId, userRole);

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
