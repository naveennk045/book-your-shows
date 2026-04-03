package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.movie.*;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.MovieService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MovieServlet extends HttpServlet {

    private final MovieService movieService;
    private final ObjectMapper objectMapper;

    public MovieServlet() {
        this.movieService = new MovieService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        String[] parts = splitPath(request);

        try {
            // /movies/{movieId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleGetMovieById(parts[2], response);
                return;
            }

            // /movies
            handleListMovies(request, response);

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

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        try {
            handleCreateMovie(request, response);
        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        UserContext userContext = getUserContext(request);

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String[] parts = splitPath(request);

        try {
            // /movies/{movieId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleUpdateMovie(parts[2], request, response);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Movie id is required");

        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }
    }


    private void handleGetMovieById(String movieIdStr, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id", response);
        if (movieId == -1) return;

        Optional<MovieDetails> movie = movieService.getMovieById(movieId);

        if (movie.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Movie not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), movie.get());
    }

    private void handleListMovies(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        MovieQueryParameter query = new MovieQueryParameter();
        query.setName(request.getParameter("name"));
        query.setLanguage(request.getParameter("language"));
        query.setGenre(request.getParameter("genre"));
        query.setSort(request.getParameter("sort"));
        query.setLimit(parseIntOrDefault(request.getParameter("limit"), 20));
        query.setOffset(parseIntOrDefault(request.getParameter("offset"), 0));

        String year = request.getParameter("release_year");
        if (year != null) {
            int parsedYear = parseId(year, "Invalid release_year", response);
            if (parsedYear == -1) return;
            query.setReleaseYear(parsedYear);
        }

        if (query.getLimit() > 100 || query.getLimit() < 0 || query.getOffset() < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pagination values");
            return;
        }

        List<MovieSummary> movies = movieService.getAllMovies(query);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                movies.isEmpty() ? Map.of("message", "No movies") : movies);
    }

    private void handleCreateMovie(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        MovieCreateRequest createReq;
        try {
            createReq = objectMapper.readValue(request.getReader(), MovieCreateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        MovieDetails created = movieService.createMovie(createReq);

        response.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Movie created successfully", "movie_id", created.getMovieId()));
    }

    private void handleUpdateMovie(String movieIdStr, HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException, SQLException, CustomException {

        int movieId = parseId(movieIdStr, "Invalid movie_id", response);
        if (movieId == -1) return;

        MovieUpdateRequest updateReq;
        try {
            updateReq = objectMapper.readValue(request.getReader(), MovieUpdateRequest.class);
        } catch (JsonProcessingException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        movieService.updateMovie(movieId, updateReq);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(),
                Map.of("message", "Movie updated successfully", "movie_id", movieId));
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