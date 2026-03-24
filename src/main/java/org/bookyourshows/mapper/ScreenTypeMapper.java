package org.bookyourshows.mapper;

import org.bookyourshows.dto.screen.ScreenType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScreenTypeMapper {

    public static ScreenType mapRowToScreenType(ResultSet resultSet) throws SQLException {
        ScreenType screenType = new ScreenType();

        screenType.setScreenTypeId(resultSet.getInt("screen_type_id"));
        screenType.setScreenTypeName(resultSet.getString("screen_type"));
        screenType.setScreenTypeDescription(resultSet.getString("description"));
        screenType.setPriceMultiplier(resultSet.getDouble("price_multiplier"));

        return screenType;
    }
}
