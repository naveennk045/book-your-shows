package org.bookyourshows.mapper;

import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TheatreFeedbackMapper {

    public static TheatreFeedbackResponse mapRowToTheatreFeedback(ResultSet rs) throws SQLException {
        TheatreFeedbackResponse response = new TheatreFeedbackResponse();

        response.setRatingId(rs.getInt("rating_id"));
        response.setTheatreId(rs.getInt("theatre_id"));
        response.setBookingId(rs.getInt("booking_id"));
        response.setUserId(rs.getInt("user_id"));
        response.setRatings(rs.getInt("ratings"));
        response.setComment(rs.getString("comment"));
        response.setCreatedAt(rs.getTimestamp("created_at"));
        response.setUpdatedAt(rs.getTimestamp("updated_at"));

        return response;
    }
}