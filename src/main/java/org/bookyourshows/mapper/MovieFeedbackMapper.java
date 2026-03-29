package org.bookyourshows.mapper;

import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieFeedbackMapper {

    public static MovieFeedbackResponse mapRowToMovieFeedback(ResultSet rs) throws SQLException {
        MovieFeedbackResponse response = new MovieFeedbackResponse();

        response.setRatingId(rs.getInt("rating_id"));
        response.setMovieId(rs.getInt("movie_id"));
        response.setBookingId(rs.getInt("booking_id"));
        response.setUserId(rs.getInt("user_id"));
        response.setRatings(rs.getInt("ratings"));
        response.setComment(rs.getString("comment"));
        response.setCreatedAt(rs.getTimestamp("created_at"));
        response.setUpdatedAt(rs.getTimestamp("updated_at"));

        return response;
    }
}