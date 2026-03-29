package org.bookyourshows.mapper;

import org.bookyourshows.dto.analytics.MoviePerformanceResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalyticsMapper {

    public static MoviePerformanceResponse mapRowToMoviePerformanceResponse(ResultSet resultSet) throws SQLException {
        MoviePerformanceResponse moviePerformanceResponse = new MoviePerformanceResponse();

        moviePerformanceResponse.setMovieName(resultSet.getString("movie_name"));
        moviePerformanceResponse.setMovieId(resultSet.getInt("movie_id"));
        moviePerformanceResponse.setSeatsNotBooked(resultSet.getInt("seats_not_booked"));
        moviePerformanceResponse.setSeatsBooked(resultSet.getInt("seats_booked"));
        moviePerformanceResponse.setNoOfShows(resultSet.getInt("no_of_shows"));

        return moviePerformanceResponse;
    }
}
