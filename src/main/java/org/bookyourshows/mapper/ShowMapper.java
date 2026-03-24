package org.bookyourshows.mapper;

import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSummary;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowMapper {
    public static ShowDetails mapRowShowDetails(ResultSet resultSet) throws SQLException {

        ShowDetails showDetails = new ShowDetails();

        showDetails.setShowId(resultSet.getInt("show_id"));
        showDetails.setTheatreId(resultSet.getInt("theatre_id"));
        showDetails.setScreenId(resultSet.getInt("screen_id"));
        showDetails.setMovieId(resultSet.getInt("movie_id"));
        showDetails.setShowDate(resultSet.getDate("show_date"));
        showDetails.setBasePrice(resultSet.getDouble("base_price"));
        showDetails.setStartTime(resultSet.getTime("start_time"));
        showDetails.setEndTime(resultSet.getTime("end_time"));

        return showDetails;
    }

    public static ShowSummary mapRowShowSummary(ResultSet resultSet) throws SQLException {
        ShowSummary showSummary = new ShowSummary();

        showSummary.setShowId(resultSet.getInt("show_id"));
        showSummary.setScreenId(resultSet.getInt("screen_id"));
        showSummary.setMovieId(resultSet.getInt("movie_id"));
        showSummary.setShowDate(resultSet.getDate("show_date"));
        showSummary.setStartTime(resultSet.getTime("start_time"));
        showSummary.setEndTime(resultSet.getTime("end_time"));
        showSummary.setBasePrice(resultSet.getDouble("base_price"));
        return showSummary;

    }
}
