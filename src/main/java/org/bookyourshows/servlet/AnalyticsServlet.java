package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.bookyourshows.dto.analytics.*;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.AnalyticsService;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;
    private final TheatreService theatreService;

    public AnalyticsServlet() {
        this.analyticsService = new AnalyticsService();
        this.objectMapper = new ObjectMapper();
        this.theatreService = new TheatreService();

        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = splitPath(request);

        UserContext userContext = (UserContext) request.getAttribute("userContext");

        if (path == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No route found"));
            return;
        }

        try {

            // 1. MOVIE PERFORMANCE (PUBLIC + OWNER + ADMIN)
            if (parts.length > 2 && "movie-performance".equals(parts[2])) {
                handleMoviePerformance(request, response);
                return;
            }

            // 2. ADMIN / THEATRE_OWNER CHECK
            if (!"ADMIN".equals(userContext.getUserRole()) && !"THEATRE_OWNER".equals(userContext.getUserRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "Unauthorized"));
                return;
            }

            // 3. PEAK SHOW TIMES
            if (parts.length > 2 && "peak-show-times".equals(parts[2])) {

                Integer theatreId = parseInt(request.getParameter("theatre_id"));
                Integer year      = parseInt(request.getParameter("year"));
                Integer month     = parseInt(request.getParameter("month"));

                // FIX: typo "requestuired" → "required"
                if (theatreId == null || year == null || month == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "theatre_id, year, month are required"));
                    return;
                }

                List<PeakShowTimeResponse> data =
                        analyticsService.getPeakShowTimes(theatreId, year, month);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), data);
                return;
            }

            // 4. USER BOOKINGS
            if (parts.length > 2 && "users-bookings".equals(parts[2])) {

                Integer year  = parseInt(request.getParameter("year"));
                Integer month = parseInt(request.getParameter("month"));

                List<UserBookingAnalytics> data =
                        analyticsService.getUserBookingsAnalytics(year, month);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), data);
                return;
            }

            // 5. THEATRE BOOKINGS
            if (parts.length > 2 && "theatres-bookings".equals(parts[2])) {

                Integer year  = parseInt(request.getParameter("year"));
                Integer month = parseInt(request.getParameter("month"));

                List<TheatreBookingAnalytics> data =
                        analyticsService.getTheatreBookingsAnalytics(userContext, year, month);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), data);
                return;
            }

            // 6. TOP SPENT USERS
            if (parts.length > 2 && "top-spent".equals(parts[2])) {

                Integer year  = parseInt(request.getParameter("year"));
                Integer month = parseInt(request.getParameter("month"));

                List<TopSpentUser> data =
                        analyticsService.getTopSpentUsers(year, month);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), data);
                return;
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No route found"));

        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Database error"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }

    private void handleMoviePerformance(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserContext userContext = (UserContext) request.getAttribute("userContext");

        try {
            MoviePerformanceRequest requestDto = new MoviePerformanceRequest();

            String yearParam = request.getParameter("year");
            if (yearParam != null && !yearParam.isBlank()) {
                requestDto.setYear(Integer.parseInt(yearParam));
            }

            String monthParam = request.getParameter("month");
            if (monthParam != null && !monthParam.isBlank()) {
                requestDto.setMonth(Integer.parseInt(monthParam));
            }

            String sortParam = request.getParameter("sort");
            if (sortParam != null && !sortParam.isBlank()) {
                requestDto.setSort(sortParam);
            }

            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                requestDto.setLimit(Integer.parseInt(limitParam));
            }

            boolean isAdmin = "ADMIN".equals(userContext.getUserRole());
            Integer userId  = userContext.getUserId();

            response.setStatus(HttpServletResponse.SC_OK);

            if (isAdmin) {
                List<MoviePerformanceResponse> topMovies =
                        analyticsService.getMoviePerformance(requestDto);
                objectMapper.writeValue(response.getWriter(), topMovies);

            } else {
                Optional<TheatreDetails> theatreDetails = theatreService.getTheatreByOwnerId(userId);

                if (theatreDetails.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "No theatre found for this owner"));
                    return;
                }

                Integer theatreId = theatreDetails.get().getTheatre().getTheatreId();
                requestDto.setTheatreId(theatreId);

                List<MoviePerformanceResponse> topMoviesByTheatre =
                        analyticsService.getMoviePerformance(requestDto);

                Map<String, Object> body = new HashMap<>();
                body.put("theatre_id", theatreId);
                body.put("top_movies", topMoviesByTheatre);

                objectMapper.writeValue(response.getWriter(), body);
            }

        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid numeric parameter"));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", ex.getMessage()));
        }
    }

    private Integer parseInt(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }
}