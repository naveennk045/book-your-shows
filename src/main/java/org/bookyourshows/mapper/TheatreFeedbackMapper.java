package org.bookyourshows.mapper;

import org.bookyourshows.dto.feedback.theatre.TheatreFeedbackResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TheatreFeedbackMapper {

    public static TheatreFeedbackResponse mapRowToTheatreFeedback(ResultSet resultSet) throws SQLException {
        TheatreFeedbackResponse theatreFeedbackResponse = new TheatreFeedbackResponse();

        theatreFeedbackResponse.setRatingId(resultSet.getInt("rating_id"));
        theatreFeedbackResponse.setTheatreId(resultSet.getInt("theatre_id"));
        theatreFeedbackResponse.setBookingId(resultSet.getInt("booking_id"));
        theatreFeedbackResponse.setUserId(resultSet.getInt("user_id"));
        theatreFeedbackResponse.setRatings(resultSet.getInt("ratings"));
        theatreFeedbackResponse.setComment(resultSet.getString("comment"));
        theatreFeedbackResponse.setCreatedAt(resultSet.getTimestamp("created_at"));
        theatreFeedbackResponse.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return theatreFeedbackResponse;
    }
}