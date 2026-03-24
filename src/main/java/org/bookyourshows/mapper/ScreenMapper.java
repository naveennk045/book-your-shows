package org.bookyourshows.mapper;

import org.bookyourshows.dto.screen.ScreenDetails;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScreenMapper {
    public static ScreenDetails mapRowToScreenDetail(ResultSet resultSet) throws SQLException {
        ScreenDetails screenDetails = new ScreenDetails();

        screenDetails.setScreenId(resultSet.getInt("screen_id"));
        screenDetails.setScreenName(resultSet.getString("screen_name"));
        screenDetails.setScreenTypeId(resultSet.getInt("screen_type_id"));
        screenDetails.setScreenTypeName(resultSet.getString("screen_type_name"));
        screenDetails.setPriceMultiplier(resultSet.getDouble("price_multiplier"));
        screenDetails.setTotalRows(resultSet.getInt("total_rows"));
        screenDetails.setNoOfSeats(resultSet.getInt("no_of_seats"));
        screenDetails.setTheatreId(resultSet.getInt("theatre_id"));

        return screenDetails;
    }
}
