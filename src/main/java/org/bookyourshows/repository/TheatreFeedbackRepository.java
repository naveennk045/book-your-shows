package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackCreateRequest;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;
import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackUpdateRequest;
import org.bookyourshows.mapper.TheatreFeedbackMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TheatreFeedbackRepository {

    public List<TheatreFeedbackResponse> getFeedbacksByTheatre(int theatreId, Integer limit, Integer offset) throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT
                    rating_id,
                    theatre_id,
                    booking_id,
                    user_id,
                    ratings,
                    comment,
                    created_at,
                    updated_at
                FROM theatre_ratings
                WHERE theatre_id = ?
                ORDER BY created_at DESC
                """);

        List<Object> params = new ArrayList<>();
        params.add(theatreId);

        if (limit != null) {
            query.append(" LIMIT ?");
            params.add(limit);
        }

        if (offset != null) {
            query.append(" OFFSET ?");
            params.add(offset);
        }

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                prepareStatement.setObject(i + 1, params.get(i));
            }

            ResultSet resultSet = prepareStatement.executeQuery();
            List<TheatreFeedbackResponse> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(TheatreFeedbackMapper.mapRowToTheatreFeedback(resultSet));
            }

            return list;
        }
    }

    public Optional<TheatreFeedbackResponse> getFeedbackByTheatreIdRatingId(int theatreId, int ratingId) throws SQLException {
        String query = """
                SELECT
                    rating_id,
                    theatre_id,
                    booking_id,
                    user_id,
                    ratings,
                    comment,
                    created_at,
                    updated_at
                FROM theatre_ratings
                WHERE theatre_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query)) {

            prepareStatement.setInt(1, theatreId);
            prepareStatement.setInt(2, ratingId);

            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(TheatreFeedbackMapper.mapRowToTheatreFeedback(resultSet));
            }
        }

        return Optional.empty();
    }

    public Optional<TheatreFeedbackResponse> getFeedbackByRatingId(int ratingId) throws SQLException {
        String query = """
                SELECT
                    rating_id,
                    theatre_id,
                    booking_id,
                    user_id,
                    ratings,
                    comment,
                    created_at,
                    updated_at
                FROM theatre_ratings
                WHERE rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query)) {

            prepareStatement.setInt(1, ratingId);

            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(TheatreFeedbackMapper.mapRowToTheatreFeedback(resultSet));
            }
        }

        return Optional.empty();
    }

    public int createFeedback(int theatreId, TheatreFeedbackCreateRequest request) throws SQLException {
        String query = """
                INSERT INTO theatre_ratings
                    (theatre_id, booking_id, user_id, ratings, comment)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            prepareStatement.setInt(1, theatreId);
            prepareStatement.setInt(2, request.getBookingId());
            prepareStatement.setInt(3, request.getUserId());
            prepareStatement.setInt(4, request.getRatings());
            prepareStatement.setString(5, request.getComment());

            int affected = prepareStatement.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to create theatre feedback");
            }

            try (ResultSet keys = prepareStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new RuntimeException("Failed to retrieve theatre feedback ID");
    }

    public boolean updateFeedback(int theatreId, int ratingId, TheatreFeedbackUpdateRequest request) throws SQLException {

        String query = """
                UPDATE theatre_ratings
                SET ratings = ?,
                    comment = ?
                WHERE theatre_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query)) {

            prepareStatement.setInt(1, request.getRatings());
            prepareStatement.setString(2, request.getComment());
            prepareStatement.setInt(3, theatreId);
            prepareStatement.setInt(4, ratingId);

            return prepareStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteFeedback(int theatreId, int ratingId) throws SQLException {
        String query = """
                DELETE FROM theatre_ratings
                WHERE theatre_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement prepareStatement = connection.prepareStatement(query)) {

            prepareStatement.setInt(1, theatreId);
            prepareStatement.setInt(2, ratingId);

            return prepareStatement.executeUpdate() > 0;
        }
    }
}