package org.bookyourshows.mapper;

import org.bookyourshows.dto.feedback.movie.MovieFeedbackResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieFeedbackMapper {

    public static MovieFeedbackResponse mapRowToMovieFeedback(ResultSet resultSet) throws SQLException {
        MovieFeedbackResponse movieFeedbackResponse = new MovieFeedbackResponse();

        movieFeedbackResponse.setRatingId(resultSet.getInt("rating_id"));
        movieFeedbackResponse.setMovieId(resultSet.getInt("movie_id"));
        movieFeedbackResponse.setBookingId(resultSet.getInt("booking_id"));
        movieFeedbackResponse.setUserId(resultSet.getInt("user_id"));
        movieFeedbackResponse.setRatings(resultSet.getInt("ratings"));
        movieFeedbackResponse.setComment(resultSet.getString("comment"));
        movieFeedbackResponse.setCreatedAt(resultSet.getTimestamp("created_at"));
        movieFeedbackResponse.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return movieFeedbackResponse;
    }
}