package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.bookyourshows.dto.analytics.MoviePerformanceRequest;
import org.bookyourshows.dto.analytics.MoviePerformanceResponse;
import org.bookyourshows.service.AnalyticsService;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.BookingService;
import org.bookyourshows.service.TheatreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
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
        String[] parts = path.split("/");

        // "/analytics/movie-performance"
        System.out.println(Arrays.toString(parts));
        if (parts[2].equals("movie-performance")) {
            handleMoviePerformance(req, resp);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        objectMapper.writeValue(resp.getWriter(), Map.of("message", "Not found"));
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

            boolean isAdmin = "ADMIN".equals(req.getAttribute("user_role"));
            Integer userId = req.getIntHeader("user_id");
            System.out.println("User id: " + userId);

            resp.setStatus(HttpServletResponse.SC_OK);

            if (isAdmin) {
                objectMapper.writeValue(resp.getWriter(), topMovies);
            } else {


                Integer theatreId = theatreService.getTheatreByOwnerId(userId).get().getTheatre().getTheatreId();
                requestDto.setTheatreId(theatreId);


                Map<String, Object> body = new HashMap<>();
                System.out.println("body: " + objectMapper.writeValueAsString(body));
                body.put("theatre_id", requestDto.getTheatreId());
                body.put("top_movies", topMovies);
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
}
