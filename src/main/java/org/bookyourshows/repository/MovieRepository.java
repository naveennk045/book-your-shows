package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.movie.*;
import org.bookyourshows.mapper.MovieMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieRepository {

    public Optional<MovieDetails> getMovieById(int movieId) throws SQLException {
        String query = """
                SELECT
                    movie_id,
                    title,
                    language,
                    genre,
                    duration,
                    release_date,
                    poster_url,
                    trailer_url,
                    description,
                    censor_rating
                FROM movies
                WHERE movie_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, movieId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(MovieMapper.mapRowToMovieDetail(resultSet));
            }
        }

        return Optional.empty();
    }

    public List<MovieDetails> getAllMovies() throws SQLException {

        String query = """
                   SELECT
                    movie_id,
                    title,
                    language,
                    genre,
                    duration,
                    release_date,
                    poster_url,
                    trailer_url,
                    description,
                    censor_rating
                FROM movies
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<MovieDetails> movies = new ArrayList<>();

            while (resultSet.next()) {
                movies.add(MovieMapper.mapRowToMovieDetail(resultSet));
            }

            return movies;
        }
    }

    public List<MovieSummary> getAllMovies(MovieQueryParameter queryParameter) throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT
                    movie_id,
                    title,
                    release_date
                FROM movies
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (queryParameter.getName() != null) {
            query.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + queryParameter.getName() + "%");
        }

        if (queryParameter.getLanguage() != null) {
            query.append(" AND language = ?");
            params.add(queryParameter.getLanguage());
        }

        if (queryParameter.getGenre() != null) {
            query.append(" AND genre = ?");
            params.add(queryParameter.getGenre());
        }

        if (queryParameter.getReleaseYear() != null) {
            query.append(" AND YEAR(release_date) = ?");
            params.add(queryParameter.getReleaseYear());
        }

        if ("release_date".equalsIgnoreCase(queryParameter.getSort())) {
            query.append(" ORDER BY release_date DESC");
        } else {
            query.append(" ORDER BY movie_id DESC");
        }

        if (queryParameter.getLimit() != null) {
            query.append(" LIMIT ?");
            params.add(queryParameter.getLimit());
        }

        if (queryParameter.getOffset() != null) {
            query.append(" OFFSET ?");
            params.add(queryParameter.getOffset());
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            List<MovieSummary> movies = new ArrayList<>();

            while (resultSet.next()) {
                movies.add(MovieMapper.mapRowToMovieSummary(resultSet));
            }

            return movies;
        }
    }

    public int createMovie(MovieCreateRequest request) throws SQLException {

        String query = """
                INSERT INTO movies
                (title, language, genre, duration, release_date,
                 poster_url, trailer_url, description, censor_rating)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, request.getTitle());
            preparedStatement.setString(2, request.getLanguage());
            preparedStatement.setString(3, request.getGenre());
            preparedStatement.setInt(4, request.getDuration());
            preparedStatement.setDate(5, request.getReleaseDate());
            preparedStatement.setString(6, request.getPosterUrl());
            preparedStatement.setString(7, request.getTrailerUrl());
            preparedStatement.setString(8, request.getDescription());
            preparedStatement.setString(9, request.getCensorRating());

            int affected = preparedStatement.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to create movie");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new RuntimeException("Failed to retrieve movie ID");
    }

    public boolean updateMovie(Integer movieId, MovieUpdateRequest request) throws SQLException {

        String query = """
                UPDATE movies
                SET language = ?,
                    genre = ?,
                    duration = ?,
                    release_date = ?,
                    poster_url = ?,
                    trailer_url = ?,
                    description = ?,
                    censor_rating = ?
                WHERE movie_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, request.getLanguage());
            preparedStatement.setString(2, request.getGenre());
            preparedStatement.setInt(3, request.getDuration());
            preparedStatement.setDate(4, request.getReleaseDate());
            preparedStatement.setString(5, request.getPosterUrl());
            preparedStatement.setString(6, request.getTrailerUrl());
            preparedStatement.setString(7, request.getDescription());
            preparedStatement.setString(8, request.getCensorRating());
            preparedStatement.setInt(9, movieId);

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteMovie(int movieId) throws SQLException {

        String query = "DELETE FROM movies WHERE movie_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, movieId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}