package org.bookyourshows.mapper;

import org.bookyourshows.dto.screen.ScreenDetail;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScreenMapper {
    public static ScreenDetail mapRowToScreenDetail(ResultSet resultSet) throws SQLException {
        ScreenDetail screenDetail = new ScreenDetail();

        screenDetail.setScreenId(resultSet.getInt("screen_id"));
        screenDetail.setScreenName(resultSet.getString("screen_name"));
        screenDetail.setScreenTypeId(resultSet.getInt("screen_type_id"));
        screenDetail.setScreenTypeName(resultSet.getString("screen_type_name"));
        screenDetail.setPriceMultiplier(resultSet.getDouble("price_multiplier"));
        screenDetail.setTotalRows(resultSet.getInt("total_rows"));
        screenDetail.setNoOfSeats(resultSet.getInt("no_of_seats"));
        screenDetail.setTheatreId(resultSet.getInt("theatre_id"));

        return screenDetail;
    }
}
