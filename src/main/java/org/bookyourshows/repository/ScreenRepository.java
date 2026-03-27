package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.screen.ScreenUpdateRequest;
import org.bookyourshows.mapper.ScreenMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScreenRepository {

    public ScreenRepository() {

    }

    public List<ScreenDetails> getScreensByTheatreId(Integer theatreId) throws SQLException {
        String query = """
                
                       SELECT s.screen_id,
                       s.screen_name,
                       s.screen_type_id,
                       st.name AS screen_type_name,
                       st.price_multiplier,
                       s.total_rows,
                       s.no_of_seats,
                       s.theatre_id

                       FROM screens AS s
                       JOIN screen_types st on s.screen_type_id = st.screen_type_id
                       WHERE s.theatre_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
            preparedStatement.setInt(1, theatreId);

            List<ScreenDetails> screenDetails = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ScreenDetails screenDetail = ScreenMapper.mapRowToScreenDetail(resultSet);
                screenDetails.add(screenDetail);
            }

            return screenDetails;

        } catch (SQLException e) {
            throw new SQLException(e);
        }

    }

    public Optional<ScreenDetails> getScreenByScreenId(Integer screenId) throws SQLException {
        String query = """
                       SELECT s.screen_id,
                       s.screen_name,
                       s.screen_type_id,
                       st.name AS screen_type_name,
                       st.price_multiplier,
                       s.total_rows,
                       s.no_of_seats,
                       s.theatre_id
                       FROM screens AS s
                       JOIN screen_types st on s.screen_type_id = st.screen_type_id
                       WHERE s.screen_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
            preparedStatement.setInt(1, screenId);


            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ScreenDetails screenDetails = ScreenMapper.mapRowToScreenDetail(resultSet);
                return Optional.of(screenDetails);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return Optional.empty();
    }

    public Optional<ScreenDetails> getScreenById(int screenId, int theatreId) throws SQLException {
        String query = """
                       SELECT s.screen_id,
                       s.screen_name,
                       s.screen_type_id,
                       st.name AS screen_type_name,
                       st.price_multiplier,
                       s.total_rows,
                       s.no_of_seats,
                       s.theatre_id
                       FROM screens AS s
                       JOIN screen_types st on s.screen_type_id = st.screen_type_id
                       WHERE s.screen_id = ?
                       AND s.theatre_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
            preparedStatement.setInt(1, screenId);
            preparedStatement.setInt(2, theatreId);


            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ScreenDetails screenDetails = ScreenMapper.mapRowToScreenDetail(resultSet);
                return Optional.of(screenDetails);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return Optional.empty();
    }


    public int addScreen(ScreenCreateRequest request) throws SQLException {
        String insertScreenQuery = """
                INSERT INTO screens(theatre_id, screen_name, screen_type_id, total_rows, no_of_seats)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            int screenId;

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertScreenQuery, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, request.getTheatreId());
                preparedStatement.setString(2, request.getScreenName());
                preparedStatement.setInt(3, request.getScreenTypeId());
                preparedStatement.setInt(4, request.getTotalRows());
                preparedStatement.setInt(5, request.getNoOfSeats());


                int affected = preparedStatement.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("Failed to create screen");
                }

                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        screenId = keys.getInt(1);
                    } else {
                        throw new RuntimeException("Failed to create screem");
                    }
                }
            }
            return screenId;
        }
    }

    public boolean updateScreen(ScreenUpdateRequest request, int screenId) throws SQLException {

        String updateScreenQuery = """
                UPDATE screens
                SET screen_name    = ?,
                    screen_type_id = ?,
                    total_rows     = ?,
                    no_of_seats    = ?
                WHERE screen_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(updateScreenQuery);
            {
                preparedStatement.setString(1, request.getScreenName());
                preparedStatement.setInt(2, request.getScreenTypeId());
                preparedStatement.setInt(3, request.getTotalRows());
                preparedStatement.setInt(4, request.getNoOfSeats());
                preparedStatement.setInt(5, screenId);


                int affected = preparedStatement.executeUpdate();

                return affected != 0;


            }
        }
    }

    public boolean deleteScreen(int screenId) throws SQLException {
        String deleteScreenQuery = "DELETE FROM screens WHERE screen_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            int screenRows;
            try (PreparedStatement preparedStatementTheatre = connection.prepareStatement(deleteScreenQuery)) {
                preparedStatementTheatre.setInt(1, screenId);
                screenRows = preparedStatementTheatre.executeUpdate();
            }
            return screenRows != 0;
        }
    }



}
