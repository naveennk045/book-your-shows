package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;
import org.bookyourshows.dto.feedback.movie.MovieFeedbackUpdateRequest;
import org.bookyourshows.mapper.MovieFeedbackMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieFeedbackRepository {

    public List<MovieFeedbackResponse> getFeedbacksByMovie(int movieId, Integer limit, Integer offset)
            throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT
                    rating_id,
                    movie_id,
                    booking_id,
                    user_id,
                    ratings,
                    comment,
                    created_at,
                    updated_at
                FROM movie_ratings
                WHERE movie_id = ?
                ORDER BY created_at DESC
                """);

        List<Object> params = new ArrayList<>();
        params.add(movieId);

        if (limit != null) {
            query.append(" LIMIT ?");
            params.add(limit);
        }

        if (offset != null) {
            query.append(" OFFSET ?");
            params.add(offset);
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            List<MovieFeedbackResponse> list = new ArrayList<>();

            while (rs.next()) {
                list.add(MovieFeedbackMapper.mapRowToMovieFeedback(rs));
            }

            return list;
        }
    }

    public Optional<MovieFeedbackResponse> getFeedbackById(int movieId, int ratingId) throws SQLException {
        String query = """
                SELECT
                    rating_id,
                    movie_id,
                    booking_id,
                    user_id,
                    ratings,
                    comment,
                    created_at,
                    updated_at
                FROM movie_ratings
                WHERE movie_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, movieId);
            ps.setInt(2, ratingId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(MovieFeedbackMapper.mapRowToMovieFeedback(rs));
            }
        }

        return Optional.empty();
    }

    public int createFeedback(int movieId, MovieFeedbackCreateRequest request) throws SQLException {
        String query = """
                INSERT INTO movie_ratings
                    (movie_id, booking_id, user_id, ratings, comment)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, movieId);
            ps.setInt(2, request.getBookingId());
            ps.setInt(3, request.getUserId());
            ps.setInt(4, request.getRatings());
            ps.setString(5, request.getComment());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to create movie feedback");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new RuntimeException("Failed to retrieve movie feedback ID");
    }

    public boolean updateFeedback(int movieId, int ratingId, MovieFeedbackUpdateRequest request)
            throws SQLException {

        String query = """
                UPDATE movie_ratings
                SET ratings = ?,
                    comment = ?
                WHERE movie_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, request.getRatings());
            ps.setString(2, request.getComment());
            ps.setInt(3, movieId);
            ps.setInt(4, ratingId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFeedback(int movieId, int ratingId) throws SQLException {
        String query = """
                DELETE FROM movie_ratings
                WHERE movie_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, movieId);
            ps.setInt(2, ratingId);

            return ps.executeUpdate() > 0;
        }
    }
}