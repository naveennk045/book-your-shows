package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.Views;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.TheatreService;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TheatreServlet extends HttpServlet {

    private final TheatreService theatreService;
    private final ObjectMapper objectMapper;

    public TheatreServlet() {
        this.theatreService = new TheatreService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // GET /theatres/{id}/address
            if (parts.length == 4 && "address".equals(parts[3])) {
                handleGetTheatreAddress(parts[2], response);
                return;
            }

            // GET /theatres/{id}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleGetTheatreById(parts[2], response, userContext);
                return;
            }

            // GET /theatres  (list)
            handleListTheatres(request, response, userContext);

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserContext userContext = getUserContext(request);

        TheatreCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), TheatreCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        createReq.setOwnerId(userContext.getUserId());

        try {
            TheatreDetails created = theatreService.createTheatre(createReq);
            int id = created.getTheatre().getTheatreId();

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Theatre created successfully", "theatre_id", id));

        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (RuntimeException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // PUT /theatres/{id}/address
            if (parts.length == 4 && "address".equals(parts[3])) {
                handleUpdateTheatreAddress(parts[2], request, response, userContext);
                return;
            }

            // PUT /theatres/{id}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleUpdateTheatre(parts[2], request, response, userContext);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Theatre id is required");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        if (parts.length < 3 || parts[2].isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Theatre id is required in path");
            return;
        }

        int theatreId;
        try {
            theatreId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id: " + parts[2]);
            return;
        }

        try {
            boolean deleted = theatreService.deleteTheatre(theatreId, userContext.getUserId(), userContext.getUserRole());

            if (!deleted) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Theatre not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre deleted successfully"));

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    //
    // Route handlers
    private void handleGetTheatreAddress(String idStr, HttpServletResponse response)
            throws IOException, SQLException {

        int theatreId;
        try {
            theatreId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id");
            return;
        }

        Optional<Address> address = theatreService.getTheatreAddress(theatreId);

        if (address.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), address.get());
    }

    private void handleGetTheatreById(String theatreIdStr, HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException {

        int theatreId;
        try {
            theatreId = Integer.parseInt(theatreIdStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id");
            return;
        }

        Optional<TheatreDetails> theatreDetails = theatreService.getTheatreById(theatreId);

        if (theatreDetails.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Theatre not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        writeWithView(response, theatreDetails.get(), userContext.getUserRole());
    }

    private void handleListTheatres(HttpServletRequest request, HttpServletResponse response,
                                    UserContext userContext)
            throws IOException, SQLException, CustomException {

        String theatreName = request.getParameter("theatre_name");
        String city = request.getParameter("city");
        String status = request.getParameter("status");
        int limit = parseIntOrDefault(request.getParameter("limit"), 20);
        int offset = parseIntOrDefault(request.getParameter("offset"), 0);

        if (limit > 100 || limit < 0 || offset < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pagination");
            return;
        }

        List<TheatreSummary> theatres = theatreService.getAllTheatre(limit, offset, theatreName, city, status, userContext);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), theatres);
    }

    private void handleUpdateTheatreAddress(String idStr, HttpServletRequest request,
                                            HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int theatreId;
        try {
            theatreId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id");
            return;
        }

        Address addressRequest;
        try {
            addressRequest = objectMapper.readValue(request.getReader(), Address.class);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        try {
            theatreService.updateTheatreAddress(theatreId, userContext.getUserId(), userContext.getUserRole(), addressRequest);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre address updated successfully"));
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleUpdateTheatre(String idStr, HttpServletRequest request,
                                     HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int theatreId;
        try {
            theatreId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theatre id");
            return;
        }

        TheatreUpdateRequest updateReq;
        try {
            updateReq = objectMapper.readValue(request.getReader(), TheatreUpdateRequest.class);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        try {
            boolean updated = theatreService.updateTheatre(theatreId, userContext.getUserId(), userContext.getUserRole(), updateReq);

            if (!updated) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Theatre not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre updated successfully"));
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }



    private UserContext getUserContext(HttpServletRequest request) {
        return (UserContext) request.getAttribute("userContext");
    }


    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
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

    private void writeWithView(HttpServletResponse response, Object data, String role)
            throws IOException {
        Class<?> view = Views.resolveView(role);
        System.out.println("[JsonView] role='" + role + "' view=" + view.getSimpleName()); // add this
        objectMapper.writerWithView(view).writeValue(response.getWriter(), data);
    }
}