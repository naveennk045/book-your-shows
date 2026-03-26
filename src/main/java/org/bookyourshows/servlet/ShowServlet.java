package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.*;
import org.bookyourshows.dto.show.*;
import org.bookyourshows.service.ShowService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShowServlet extends HttpServlet {

    private final ShowService showService;
    private final ObjectMapper objectMapper;

    public ShowServlet() {
        this.showService = new ShowService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // POST /theatres/{theatre_id}/screens/{screen_id}/shows
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = request.getPathInfo().split("/");

        if (parts.length < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid URL"));
            return;
        }

        int theatreId;
        int screenId;

        try {
            theatreId = Integer.parseInt(parts[2]);
            screenId = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid theatre_id or screen_id"));
            return;
        }

        try {
            ShowCreateRequest req = objectMapper.readValue(request.getReader(), ShowCreateRequest.class);
            req.setTheatreId(theatreId);
            req.setScreenId(screenId);

            int showId = showService.createShow(req);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Show created successfully", "show_id", showId));

        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON"));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        try {

            //  /shows/{show_id}/seats ( seat availability for a show)
            if (parts.length == 4 && parts[3].equals("seats")) {
                int showId = Integer.parseInt(parts[2]);

                List<ShowSeatingResponse> seats = showService.getShowSeats(showId);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), seats);
                return;
            }

            // /shows/{id}
            if (parts.length == 3) {
                int showId = Integer.parseInt(parts[2]);

                Optional<ShowDetails> show = showService.getShowById(showId);

                if (show.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Show not found"));
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), show.get());
                return;
            }

            // /shows?theatre_id=&show_date=&movie_id=
            String theatreIdParam = request.getParameter("theatre_id");
            String dateParam = request.getParameter("show_date");
            String movieIdParam = request.getParameter("movie_id");

            if (theatreIdParam == null || dateParam == null || movieIdParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid parameters"));
                return;
            }

            int theatreId = Integer.parseInt(theatreIdParam);
            Date date = Date.valueOf(dateParam);
            int movieId = Integer.parseInt(movieIdParam);

            List<ShowDetails> shows = showService.getShows(theatreId, date, movieId);

            response.setStatus(HttpServletResponse.SC_OK);
            if(shows.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "No live shows found"));
            }
            objectMapper.writeValue(response.getWriter(), shows);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid show id or theatre id"));
        } catch (IllegalArgumentException e) {
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "show_id required"));
            return;
        }

        int showId;
        ShowUpdateRequest req;

        try {
            showId = Integer.parseInt(parts[2]);
            req = objectMapper.readValue(request.getReader(), ShowUpdateRequest.class);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid show_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            boolean updated = showService.updateShow(showId, req);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Show updated successfully", "show_id", showId));

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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "show_id required"));
            return;
        }

        int showId;

        try {
            showId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid show_id"));
            return;
        }

        try {
            boolean deleted = showService.deleteShow(showId);

            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Show not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Show deleted successfully"));

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
}