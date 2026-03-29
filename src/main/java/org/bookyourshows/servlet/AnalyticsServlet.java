package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.bookyourshows.dto.analytics.*;
import org.bookyourshows.service.AnalyticsService;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.BookingService;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;
    private final BookingService bookingService;
    private final TheatreService theatreService;
    ;

    public AnalyticsServlet() {
        this.analyticsService = new AnalyticsService();
        this.objectMapper = new ObjectMapper();
        this.theatreService = new TheatreService();
        this.bookingService = new BookingService();

        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), Map.of("message", "Not found"));
            return;
        }

        String[] parts = path.split("/");

        try {

            // 1. MOVIE PERFORMANCE (PUBLIC + OWNER + ADMIN)
            if (parts.length > 2 && "movie-performance".equals(parts[2])) {
                handleMoviePerformance(req, resp);
                return;
            }

            // 2. ADMIN CHECK
            String role = String.valueOf(req.getHeader("user_role"));
            if (!"ADMIN".equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                objectMapper.writeValue(resp.getWriter(),
                        Map.of("message", "Unauthorized"));
                return;
            }

            // 3. PEAK SHOW TIMES
            if (parts.length > 2 && "peak-show-times".equals(parts[2])) {

                Integer theatreId = parseInt(req.getParameter("theatre_id"));
                Integer year = parseInt(req.getParameter("year"));
                Integer month = parseInt(req.getParameter("month"));

                if (theatreId == null || year == null || month == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(resp.getWriter(),
                            Map.of("message", "theatre_id, year, month are required"));
                    return;
                }

                List<PeakShowTimeResponse> data =
                        analyticsService.getPeakShowTimes(theatreId, year, month);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), data);
                return;
            }

            // 4. USER BOOKINGS
            if (parts.length > 2 && "users-bookings".equals(parts[2])) {

                List<UserBookingAnalytics> data =
                        analyticsService.getUserBookingsAnalytics();

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), data);
                return;
            }


            // 5. THEATRE BOOKINGS
            if (parts.length > 2 && "theatres-bookings".equals(parts[2])) {

                List<TheatreBookingAnalytics> data =
                        analyticsService.getTheatreBookingsAnalytics();

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), data);
                return;
            }


            // 6. TOP SPENT USERS
            if (parts.length > 2 && "top-spent".equals(parts[2])) {

                List<TopSpentUser> data =
                        analyticsService.getTopSpentUsers();

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), data);
                return;
            }


            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    Map.of("message", "Not found"));

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(),
                    Map.of("message", "Database error"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(),
                    Map.of("message", e.getMessage()));
        }
    }


    private void handleMoviePerformance(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            MoviePerformanceRequest requestDto = new MoviePerformanceRequest();

            String yearParam = req.getParameter("year");
            if (yearParam != null && !yearParam.isBlank()) {
                requestDto.setYear(Integer.parseInt(yearParam));
            }

            String monthParam = req.getParameter("month");
            if (monthParam != null && !monthParam.isBlank()) {
                requestDto.setMonth(Integer.parseInt(monthParam));
            }

            String sortParam = req.getParameter("sort");
            if (sortParam != null && !sortParam.isBlank()) {
                requestDto.setSort(sortParam);
            }

            String limitParam = req.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                requestDto.setLimit(Integer.parseInt(limitParam));
            }


            List<MoviePerformanceResponse> topMovies =
                    analyticsService.getMoviePerformance(requestDto);

            boolean isAdmin = "ADMIN".equals(req.getHeader("user_role"));
            Integer userId = req.getIntHeader("user_id");
            System.out.println("User id: " + userId);

            resp.setStatus(HttpServletResponse.SC_OK);

            if (isAdmin) {
                objectMapper.writeValue(resp.getWriter(), topMovies);
            } else {


                Integer theatreId = theatreService.getTheatreByOwnerId(userId).get().getTheatre().getTheatreId();
                if (theatreId == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(resp.getWriter(), Map.of("message", "Not found"));
                    return;
                }
                List<MoviePerformanceResponse> topMoviesByTheatre =
                        analyticsService.getMoviePerformance(requestDto);
                requestDto.setTheatreId(theatreId);


                Map<String, Object> body = new HashMap<>();
                System.out.println("body: " + objectMapper.writeValueAsString(body));
                body.put("theatre_id", requestDto.getTheatreId());
                body.put("top_movies", topMoviesByTheatre);
                objectMapper.writeValue(resp.getWriter(), body);
            }

        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(),
                    Map.of("message", "Invalid numeric parameter"));
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(),
                    Map.of("message", ex.getMessage()));
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


}
