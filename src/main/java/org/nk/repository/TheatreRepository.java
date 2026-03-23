package org.nk.repository;


import org.nk.config.DatabaseManager;
import org.nk.dto.*;
import org.nk.mapper.TheatreMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TheatreRepository {

    public TheatreRepository() {
    }


    public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {
        String sqlQuery = """
                SELECT t.theatre_id,
                       t.theatre_name,
                       t.email,
                       t.contact_number,
                       t.total_screens,
                       a.address_id,
                       a.address_line1,
                       a.city,
                       a.state,
                       a.pincode,
                       a.latitude,
                       a.longitude
                
                FROM theatres AS t
                JOIN theatre_addresses AS a ON a.theatre_id = t.theatre_id
                WHERE t.theatre_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, theatreId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                TheatreDetails details = TheatreMapper.mapRowToTheatreDetails(resultSet);
                return Optional.of(details);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return Optional.empty();

    }

    public List<TheatreSummary> getAllTheatres(Integer limit, Integer offset, String theatreName, String city) throws SQLException {

        StringBuilder sqlQuery = new StringBuilder("""
                SELECT t.theatre_id,
                       t.theatre_name,
                       t.total_screens,
                       a.city
                FROM theatres AS t
                JOIN theatre_addresses AS a ON a.theatre_id = t.theatre_id
                WHERE t.status = 'APPROVED'
                """);

        List<Object> params = new ArrayList<>();

        if (theatreName != null && !theatreName.isBlank()) {
            sqlQuery.append(" AND t.theatre_name LIKE ?");
            params.add("%" + theatreName + "%");
        }

        if (city != null && !city.isBlank()) {
            sqlQuery.append(" AND a.city LIKE ?");
            params.add("%" + city + "%");
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ?");
            params.add(limit);
        }

        if (offset != null) {
            sqlQuery.append(" OFFSET ?");
            params.add(offset);
        }

        try (Connection Connection = DatabaseManager.getConnection()) {

            PreparedStatement preparedStatement = Connection.prepareStatement(sqlQuery.toString());
            int index = 1;
            for (Object param : params) {
                preparedStatement.setObject(index++, param);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<TheatreSummary> details = new ArrayList<>();

            while (resultSet.next()) {
                TheatreSummary detail = TheatreMapper.mapRowToTheatreSummary(resultSet);
                details.add(detail);
            }
            return details;
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public int addTheatre(TheatreCreateRequest request) throws SQLException {
        String insertTheatreSql = """
                INSERT INTO theatres
                    (owner_id, theatre_name, email, contact_number, total_screens, license_document,status)
                VALUES (?, ?, ?, ?, ?, ?,?)
                """;

        String insertAddressSql = """
                INSERT INTO theatre_addresses
                    (theatre_id, address_line1, address_line2, city, state, country,
                     pincode, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            int theatreId;

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertTheatreSql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, request.getOwnerId());
                preparedStatement.setString(2, request.getTheatreName());
                preparedStatement.setString(3, request.getEmail());
                preparedStatement.setString(4, request.getContactNumber());
                preparedStatement.setInt(5, request.getTotalScreens());
                preparedStatement.setString(6, request.getLicenseDocument());
                preparedStatement.setString(7, request.getState());


                int affected = preparedStatement.executeUpdate();
                if (affected == 0) {
                    connection.rollback();
                    throw new SQLException("Creating theatre failed.");
                }

                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        theatreId = keys.getInt(1);
                    } else {
                        connection.rollback();
                        throw new SQLException("Creating theatre failed.");
                    }
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertAddressSql)) {
                preparedStatement.setInt(1, theatreId);
                preparedStatement.setString(2, request.getAddressLine1());
                preparedStatement.setString(3, request.getAddressLine2());
                preparedStatement.setString(4, request.getCity());
                preparedStatement.setString(5, request.getState());
                preparedStatement.setString(6, request.getCountry());
                preparedStatement.setString(7, request.getPincode());
                preparedStatement.setDouble(8, request.getLatitude());
                preparedStatement.setDouble(9, request.getLongitude());

                int affected = preparedStatement.executeUpdate();
                if (affected == 0) {
                    connection.rollback();
                    throw new SQLException("creating theatre address fail no rows affected.");
                }
            }

            connection.commit();

            return theatreId;
        }
    }

    public boolean updateTheatreWithAddress(int theatreId, TheatreUpdateRequest request) throws SQLException {

        String updateTheatreSql = """
                UPDATE theatres
                SET theatre_name = ?,
                    email = ?,
                    contact_number = ?,
                    total_screens = ?,
                    license_document = ?
                WHERE theatre_id = ?
                """;

        String updateAddressSql = """
                UPDATE theatre_addresses
                SET address_line1 = ?,
                    address_line2 = ?,
                    city = ?,
                    state = ?,
                    country = ?,
                    pincode = ?,
                    latitude = ?,
                    longitude = ?
                WHERE theatre_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            int theatreRows;
            int addressRows;

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateTheatreSql)) {
                preparedStatement.setString(1, request.getTheatreName());
                preparedStatement.setString(2, request.getEmail());
                preparedStatement.setString(3, request.getContactNumber());
                preparedStatement.setInt(4, request.getTotalScreens());
                preparedStatement.setString(5, request.getLicenseDocument());
                preparedStatement.setInt(6, theatreId);

                theatreRows = preparedStatement.executeUpdate();
            }

            if (theatreRows == 0) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateAddressSql)) {
                preparedStatement.setString(1, request.getAddressLine1());
                preparedStatement.setString(2, request.getAddressLine2());
                preparedStatement.setString(3, request.getCity());
                preparedStatement.setString(4, request.getState());
                preparedStatement.setString(5, request.getCountry());
                preparedStatement.setString(6, request.getPincode());
                preparedStatement.setDouble(7, request.getLatitude());
                preparedStatement.setDouble(8, request.getLongitude());
                preparedStatement.setInt(9, theatreId);

                addressRows = preparedStatement.executeUpdate();
            }

            if (addressRows == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }

    public boolean deleteTheatre(int theatreId) throws SQLException {
        String deleteAddressSql = "DELETE FROM theatre_addresses WHERE theatre_id = ?";
        String deleteTheatreSql = "DELETE FROM theatres WHERE theatre_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatementAddress = connection.prepareStatement(deleteAddressSql)) {
                preparedStatementAddress.setInt(1, theatreId);
                preparedStatementAddress.executeUpdate();
            }

            int theatreRows;
            try (PreparedStatement preparedStatementTheatre = connection.prepareStatement(deleteTheatreSql)) {
                preparedStatementTheatre.setInt(1, theatreId);
                theatreRows = preparedStatementTheatre.executeUpdate();
            }

            if (theatreRows == 0) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        }
    }


}