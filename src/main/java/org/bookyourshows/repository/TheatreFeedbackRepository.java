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

    public List<TheatreFeedbackResponse> getFeedbacksByTheatre(int theatreId, Integer limit, Integer offset)
            throws SQLException {

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

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            List<TheatreFeedbackResponse> list = new ArrayList<>();

            while (rs.next()) {
                list.add(TheatreFeedbackMapper.mapRowToTheatreFeedback(rs));
            }

            return list;
        }
    }

    public Optional<TheatreFeedbackResponse> getFeedbackById(int theatreId, int ratingId) throws SQLException {
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

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, theatreId);
            ps.setInt(2, ratingId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(TheatreFeedbackMapper.mapRowToTheatreFeedback(rs));
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

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, theatreId);
            ps.setInt(2, request.getBookingId());
            ps.setInt(3, request.getUserId());
            ps.setInt(4, request.getRatings());
            ps.setString(5, request.getComment());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to create theatre feedback");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new RuntimeException("Failed to retrieve theatre feedback ID");
    }

    public boolean updateFeedback(int theatreId, int ratingId, TheatreFeedbackUpdateRequest request)
            throws SQLException {

        String query = """
                UPDATE theatre_ratings
                SET ratings = ?,
                    comment = ?
                WHERE theatre_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, request.getRatings());
            ps.setString(2, request.getComment());
            ps.setInt(3, theatreId);
            ps.setInt(4, ratingId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFeedback(int theatreId, int ratingId) throws SQLException {
        String query = """
                DELETE FROM theatre_ratings
                WHERE theatre_id = ? AND rating_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, theatreId);
            ps.setInt(2, ratingId);

            return ps.executeUpdate() > 0;
        }
    }
}