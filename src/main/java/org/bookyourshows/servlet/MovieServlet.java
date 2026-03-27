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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        try {
            //  /movies/{movie_id}
            if (parts.length == 3) {
                int movieId = Integer.parseInt(parts[2]);
                Optional<MovieDetails> movie = movieService.getMovieById(movieId);

                if (movie.isPresent()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), movie.get());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Movie not found"));
                }
                return;
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id"));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
            return;
        }

        // /movies?name=&language=&genre=&sort=&
        MovieQueryParameter query = new MovieQueryParameter();

        query.setName(request.getParameter("name"));
        query.setLanguage(request.getParameter("language"));
        query.setGenre(request.getParameter("genre"));
        query.setSort(request.getParameter("sort"));

        query.setLimit(parseIntOrDefault(request.getParameter("limit"), 20));
        query.setOffset(parseIntOrDefault(request.getParameter("offset"), 0));
        String year = request.getParameter("release_year");

        if (year != null) {
            try {
                query.setReleaseYear(Integer.parseInt(year));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Invalid release_year"));
                return;
            }
        }

        if (query.getLimit() > 100 || query.getLimit() < 0 || query.getOffset() < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid pagination values"));
            return;
        }

        try {
            List<MovieSummary> movies = movieService.getAllMovies(query);

            if (movies.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), Map.of("message", "No movies"));
                return;
            }


            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), movies);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!request.getAttribute("user_role").equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Unauthorized"));
            return;
        }


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {

            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }

        MovieCreateRequest createReq;

        try {
            createReq = objectMapper.readValue(request.getReader(), MovieCreateRequest.class);
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            MovieDetails created = movieService.createMovie(createReq);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of(
                            "message", "Movie created successfully",
                            "movie_id", created.getMovieId()
                    ));

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

        System.out.println("role : " + request.getAttribute("user_role"));

        if (!request.getAttribute("user_role").equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Unauthorized"));
            return;
        }


        if (request.getContentType() == null ||
                !request.getContentType().toLowerCase().contains("application/json")) {

            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Content-Type must be application/json"));
            return;
        }
        String path = request.getPathInfo();
        String[] parts = path.split("/");


        if (path.length() < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id required"));
            return;
        }

        int movieId;
        MovieUpdateRequest updateReq;

        try {
            movieId = Integer.parseInt(parts[2]);
            updateReq = objectMapper.readValue(request.getReader(), MovieUpdateRequest.class);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id"));
            return;
        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid JSON body"));
            return;
        }

        try {
            boolean updated = movieService.updateMovie(movieId, updateReq);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of(
                            "message", "Movie updated successfully",
                            "movie_id", movieId
                    ));

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

        if (!request.getAttribute("user_role").equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Unauthorized"));
            return;
        }

        String path = request.getPathInfo();
        String[] parts = path.split("/");


        if (path.length() < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie id required"));
            return;
        }

        int movieId;

        try {
            movieId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Invalid movie_id"));
            return;
        }

        try {
            boolean deleted = movieService.deleteMovie(movieId);

            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Movie not found"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Movie deleted successfully"));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "Database error"));
        }
    }

    private Integer parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
