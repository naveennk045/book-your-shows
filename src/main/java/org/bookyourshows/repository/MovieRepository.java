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
        String sql = """
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, movieId);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return Optional.of(MovieMapper.mapRowToMovieDetail(rs));
            }
        }

        return Optional.empty();
    }

    public List<MovieSummary> getAllMovies(MovieQueryParameter query) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    movie_id,
                    title,
                    release_date
                FROM movies
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (query.getName() != null) {
            sql.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + query.getName() + "%");
        }

        if (query.getLanguage() != null) {
            sql.append(" AND language = ?");
            params.add(query.getLanguage());
        }

        if (query.getGenre() != null) {
            sql.append(" AND genre = ?");
            params.add(query.getGenre());
        }

        if (query.getReleaseYear() != null) {
            sql.append(" AND YEAR(release_date) = ?");
            params.add(query.getReleaseYear());
        }

        if ("release_date".equalsIgnoreCase(query.getSort())) {
            sql.append(" ORDER BY release_date DESC");
        } else {
            sql.append(" ORDER BY movie_id DESC");
        }

        if (query.getLimit() != null) {
            sql.append(" LIMIT ?");
            params.add(query.getLimit());
        }

        if (query.getOffset() != null) {
            sql.append(" OFFSET ?");
            params.add(query.getOffset());
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            ResultSet rs = preparedStatement.executeQuery();
            List<MovieSummary> movies = new ArrayList<>();

            while (rs.next()) {
                movies.add(MovieMapper.mapRowToMovieSummary(rs));
            }

            return movies;
        }
    }

    public int createMovie(MovieCreateRequest request) throws SQLException {

        String sql = """
                INSERT INTO movies
                (title, language, genre, duration, release_date,
                 poster_url, trailer_url, description, censor_rating)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

    public boolean updateMovie(int movieId, MovieUpdateRequest request) throws SQLException {

        String sql = """
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

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

        String sql = "DELETE FROM movies WHERE movie_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, movieId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}