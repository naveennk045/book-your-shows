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

    private static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static ShowDetails mapRowShowDetails(ResultSet rs) throws SQLException {

        ShowDetails show = new ShowDetails();

        if (hasColumn(rs, "show_id")) show.setShowId(rs.getInt("show_id"));
        if (hasColumn(rs, "theatre_id")) show.setTheatreId(rs.getInt("theatre_id"));
        if (hasColumn(rs, "theatre_name")) show.setTheatreName(rs.getString("theatre_name"));
        if (hasColumn(rs, "city")) show.setTheatreLocation(rs.getString("city"));

        if (hasColumn(rs, "screen_id")) show.setScreenId(rs.getInt("screen_id"));
        if (hasColumn(rs, "screen_name")) show.setScreenName(rs.getString("screen_name"));

        if (hasColumn(rs, "movie_id")) show.setMovieId(rs.getInt("movie_id"));
        if (hasColumn(rs, "movie_name")) show.setMovieName(rs.getString("movie_name"));

        if (hasColumn(rs, "show_date")) show.setShowDate(rs.getDate("show_date"));
        if (hasColumn(rs, "start_time")) show.setStartTime(rs.getTime("start_time"));
        if (hasColumn(rs, "end_time")) show.setEndTime(rs.getTime("end_time"));

        if (hasColumn(rs, "base_price")) show.setBasePrice(rs.getDouble("base_price"));

        return show;
    }
}
