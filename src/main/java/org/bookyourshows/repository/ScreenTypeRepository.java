package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.screen.ScreenType;
import org.bookyourshows.mapper.ScreenTypeMapper;

import java.sql.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ScreenTypeRepository {


    public ScreenTypeRepository() {
    }

    public Optional<ScreenType> getScreenTypeById(Integer screenTypeId) throws SQLException {

        String query = """
                SELECT
                  screen_type_id,
                  name as screen_type,
                  description,
                  price_multiplier
                FROM screen_types
                WHERE screen_type_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, screenTypeId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                ScreenType screenType = ScreenTypeMapper.mapRowToScreenType(resultSet);
                return Optional.of(screenType);
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }

        return Optional.empty();
    }
}
