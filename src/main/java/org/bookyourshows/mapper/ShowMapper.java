package org.bookyourshows.mapper;

import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSeating;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowMapper {

    public static ShowSeating mapRowShowSeating(ResultSet resultSet) throws SQLException {

        ShowSeating showSeating = new ShowSeating();
        showSeating.setShowSeatId(resultSet.getInt("show_seat_id"));
        showSeating.setSeatNumber(resultSet.getString("seat_number"));
        showSeating.setCategory(resultSet.getString("category"));
        showSeating.setStatus(resultSet.getString("status"));
        showSeating.setFinalPrice(resultSet.getDouble("final_price"));

        return showSeating;

    }

    private static boolean hasColumn(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static ShowDetails mapRowShowDetails(ResultSet resultSet) throws SQLException {

        ShowDetails showDetails = new ShowDetails();

        if (hasColumn(resultSet, "show_id")) showDetails.setShowId(resultSet.getInt("show_id"));
        if (hasColumn(resultSet, "theatre_id")) showDetails.setTheatreId(resultSet.getInt("theatre_id"));
        if (hasColumn(resultSet, "theatre_name")) showDetails.setTheatreName(resultSet.getString("theatre_name"));
        if (hasColumn(resultSet, "city")) showDetails.setTheatreLocation(resultSet.getString("city"));

        if (hasColumn(resultSet, "screen_id")) showDetails.setScreenId(resultSet.getInt("screen_id"));
        if (hasColumn(resultSet, "screen_name")) showDetails.setScreenName(resultSet.getString("screen_name"));

        if (hasColumn(resultSet, "movie_id")) showDetails.setMovieId(resultSet.getInt("movie_id"));
        if (hasColumn(resultSet, "movie_name")) showDetails.setMovieName(resultSet.getString("movie_name"));

        if (hasColumn(resultSet, "show_date")) showDetails.setShowDate(resultSet.getDate("show_date"));
        if (hasColumn(resultSet, "start_time")) showDetails.setStartTime(resultSet.getTime("start_time"));
        if (hasColumn(resultSet, "end_time")) showDetails.setEndTime(resultSet.getTime("end_time"));

        if (hasColumn(resultSet, "base_price")) showDetails.setBasePrice(resultSet.getDouble("base_price"));
        if (hasColumn(resultSet, "status")) showDetails.setStatus(resultSet.getString("status"));

        return showDetails;
    }
}
