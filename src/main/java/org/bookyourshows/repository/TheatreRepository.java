package org.bookyourshows.repository;


import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.mapper.AddressMapper;
import org.bookyourshows.mapper.TheatreMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TheatreRepository {

    public TheatreRepository() {
    }


    public Optional<TheatreDetails> getTheatreById(Integer theatreId) throws SQLException {
        String query = """
                SELECT t.theatre_id,
                       t.theatre_name,
                       t.owner_id,
                       t.email,
                       t.contact_number,
                       (SELECT COUNT(*) FROM screens s WHERE s.theatre_id = t.theatre_id) AS total_screens,
                       a.address_id,
                       a.address_line1,
                       a.city,
                       a.state,
                       a.pincode,
                       a.latitude,
                       a.longitude
                FROM theatres t
                JOIN theatre_addresses a ON a.theatre_id = t.theatre_id
                WHERE t.theatre_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
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

    public List<TheatreSummary> getAllTheatres(Integer limit, Integer offset, String theatreName, String city, String status) throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT t.theatre_id,
                       t.theatre_name,
                       (SELECT COUNT(*) FROM screens s WHERE s.theatre_id = t.theatre_id) AS total_screens,
                       a.city
                FROM theatres AS t
                JOIN theatre_addresses AS a ON a.theatre_id = t.theatre_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (theatreName != null && !theatreName.isBlank()) {
            query.append(" AND t.theatre_name LIKE ?");
            params.add("%" + theatreName + "%");
        }


        if (city != null && !city.isBlank()) {
            query.append(" AND a.city LIKE ?");
            params.add("%" + city + "%");
        }

        if (status != null && !status.isBlank()) {
            query.append(" AND t.status = ?");
            params.add(status);
        }

        if (limit != null) {
            query.append(" LIMIT ?");
            params.add(limit);
        }

        if (offset != null) {
            query.append(" OFFSET ?");
            params.add(offset);
        }

        try (Connection Connection = DatabaseManager.getConnection()) {

            PreparedStatement preparedStatement = Connection.prepareStatement(query.toString());
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

    public Optional<TheatreDetails> getTheatreByOwnerId(Integer ownerId) throws SQLException {
        String query = """
                SELECT t.theatre_id,
                       t.owner_id,
                       t.theatre_name,
                       t.email,
                       t.contact_number,
                       (SELECT COUNT(*) FROM screens s WHERE s.theatre_id = t.theatre_id) AS total_screens,
                       a.address_id,
                       a.address_line1,
                       a.city,
                       a.state,
                       a.pincode,
                       a.latitude,
                       a.longitude
                
                FROM theatres AS t
                JOIN theatre_addresses AS a ON a.theatre_id = t.theatre_id
                WHERE t.owner_id = ?
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
            preparedStatement.setInt(1, ownerId);
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

    public Optional<Address> getTheatreAddress(int theatreId) throws SQLException {

        String query = """
                    SELECT address_line1, address_line2, city, state, country, pincode, latitude, longitude
                    FROM theatre_addresses
                    WHERE theatre_id = ?
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, theatreId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(AddressMapper.mapRowToAddress(rs));
            }
        }

        return Optional.empty();
    }

    public boolean updateTheatreAddress(int theatreId, Address req) throws SQLException {

        String query = """
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

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, req.getAddressLine1());
            ps.setString(2, req.getAddressLine2());
            ps.setString(3, req.getCity());
            ps.setString(4, req.getState());
            ps.setString(5, req.getCountry());
            ps.setString(6, req.getPincode());
            ps.setBigDecimal(7, req.getLatitude());
            ps.setBigDecimal(8, req.getLongitude());
            ps.setInt(9, theatreId);

            return ps.executeUpdate() > 0;
        }
    }

    public int addTheatre(TheatreCreateRequest request) throws SQLException {
        String insertTheatreQuery = """
                INSERT INTO theatres
                    (owner_id, theatre_name, email, contact_number, total_screens, license_document,status)
                VALUES (?, ?, ?, ?, ?, ?,?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            int theatreId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertTheatreQuery, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, request.getOwnerId());
                preparedStatement.setString(2, request.getTheatreName());
                preparedStatement.setString(3, request.getEmail());
                preparedStatement.setString(4, request.getContactNumber());
                preparedStatement.setInt(5, request.getTotalScreens());
                preparedStatement.setString(6, request.getLicenseDocument());
                preparedStatement.setString(7, request.getState());


                int affected = preparedStatement.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("Failed to create theatre");
                }

                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        theatreId = keys.getInt(1);
                    } else {
                        throw new RuntimeException("Failed to create theatre");
                    }
                }
            }
            return theatreId;
        }
    }

    public boolean addTheatreAddress(TheatreCreateRequest request, int theatreId) throws SQLException {
        String insertAddressQuery = """
                INSERT INTO theatre_addresses
                    (theatre_id, address_line1, address_line2, city, state, country,
                     pincode, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertAddressQuery);

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
                return false;
            }

        }
        return true;
    }

    public boolean updateTheatre(int theatreId, TheatreUpdateRequest request) throws SQLException {

        String updateTheatreQuery = """
                                UPDATE theatres
                                SET theatre_name = ?,
                                    email = ?,
                                    contact_number = ?,
                                    total_screens = ?,
                                    license_document = ?
                                WHERE theatre_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            int theatreRows;

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateTheatreQuery)) {
                preparedStatement.setString(1, request.getTheatreName());
                preparedStatement.setString(2, request.getEmail());
                preparedStatement.setString(3, request.getContactNumber());
                preparedStatement.setInt(4, request.getTotalScreens());
                preparedStatement.setString(5, request.getLicenseDocument());
                preparedStatement.setInt(6, theatreId);

                theatreRows = preparedStatement.executeUpdate();
            }

            return theatreRows != 0;
        }
    }

    public boolean updateTheatreAddress(int theatreId, TheatreUpdateRequest request) throws SQLException {

        String updateAddressQuery = """
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
            int addressRows;


            try (PreparedStatement preparedStatement = connection.prepareStatement(updateAddressQuery)) {
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

            return addressRows != 0;
        }
    }


    public boolean deleteTheatre(int theatreId) throws SQLException {
        String deleteTheatreQuery = "DELETE FROM theatres WHERE theatre_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            int theatreRows;
            try (PreparedStatement preparedStatementTheatre = connection.prepareStatement(deleteTheatreQuery)) {
                preparedStatementTheatre.setInt(1, theatreId);
                theatreRows = preparedStatementTheatre.executeUpdate();
            }
            return theatreRows != 0;
        }
    }


    public List<TheatreDetails> getAllTheatre() throws SQLException {
        String query = """
                SELECT t.theatre_id,
                       t.owner_id,
                       t.theatre_name,
                       t.email,
                       t.contact_number,
                       (SELECT COUNT(*) FROM screens s WHERE s.theatre_id = t.theatre_id) AS total_screens,
                       a.address_id,
                       a.address_line1,
                       a.city,
                       a.state,
                       a.pincode,
                       a.latitude,
                       a.longitude
                
                FROM theatres AS t
                JOIN theatre_addresses AS a ON a.theatre_id = t.theatre_id
                """;

        try (Connection Connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = Connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<TheatreDetails> theatreDetails = new ArrayList<>();
            while (resultSet.next()) {
                TheatreDetails theatreDetail = TheatreMapper.mapRowToTheatreDetails(resultSet);
                theatreDetails.add(theatreDetail);
            }
            return theatreDetails;
        }
    }
}