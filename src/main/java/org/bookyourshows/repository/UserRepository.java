package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.user.*;
import org.bookyourshows.mapper.AddressMapper;
import org.bookyourshows.mapper.UserMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {


    public Optional<UserDetails> getUserByUserId(Integer userId) throws SQLException {

        String query = """
                SELECT
                    users.user_id,
                    email,
                    mobile_number,
                    password_hash,
                    user_role,
                    first_name,
                    last_name,
                    date_of_birth,
                    profile_picture ,
                    account_status,
                    ua.address_line1,
                    ua.address_line2,
                    ua.city,
                    ua.state,
                    ua.country,
                    ua.pincode,
                    ua.created_at,
                    ua.updated_at,
                    last_login
                
                FROM users
                LEFT JOIN user_addresses ua ON users.user_id = ua.user_id
                WHERE users.user_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(UserMapper.mapRowToUserDetails(resultSet));
            }

        }
        return Optional.empty();
    }

    public Optional<Address> getUserAddress(int userId) throws SQLException {

        String query = """
                    SELECT address_line1, address_line2, city, state, country, pincode
                    FROM user_addresses
                    WHERE user_id = ?
                """;

        try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(AddressMapper.mapRowToAddress(rs));
            }
        }

        return Optional.empty();
    }

    public boolean updateUserAddress(Address req, int userId) throws SQLException {

        String query = """
                    UPDATE user_addresses
                    SET address_line1 = ?,
                        address_line2 = ?,
                        city = ?,
                        state = ?,
                        country = ?,
                        pincode = ?
                    WHERE user_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, req.getAddressLine1());
            ps.setString(2, req.getAddressLine2());
            ps.setString(3, req.getCity());
            ps.setString(4, req.getState());
            ps.setString(5, req.getCountry());
            ps.setString(6, req.getPincode());
            ps.setInt(7, userId);

            return ps.executeUpdate() > 0;
        }
    }


    public Optional<UserAuth> getUserAuthByEmail(String email) throws SQLException {

        String query = """
                SELECT user_id, email, password_hash, user_role
                FROM users
                WHERE email = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    UserAuth user = new UserAuth();
                    user.setUserId(rs.getInt("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password_hash"));
                    user.setRole(rs.getString("user_role"));
                    System.out.println("User: " + user.getUserId() + " " + user.getEmail() + " " + user.getPassword() + " " + user.getRole());

                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }


    public Optional<UserDetails> getUserByEmail(String email) throws SQLException {

        String query = """
                SELECT
                    users.user_id,
                    email,
                    mobile_number,
                    password_hash,
                    user_role,
                    first_name,
                    last_name,
                    date_of_birth,
                    profile_picture ,
                    account_status,
                    ua.address_line1,
                    ua.address_line2,
                    ua.city,
                    ua.state,
                    ua.country,
                    ua.pincode,
                    ua.created_at,
                    ua.updated_at,
                    last_login
                
                FROM users
                LEFT JOIN user_addresses ua ON users.user_id = ua.user_id
                WHERE users.email = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(UserMapper.mapRowToUserDetails(resultSet));
            }

        }
        return Optional.empty();
    }

    public Optional<UserDetails> getUserByMobileNumber(String mobileNumber) throws SQLException {

        String query = """
                SELECT
                    users.user_id,
                    email,
                    mobile_number,
                    password_hash,
                    user_role,
                    first_name,
                    last_name,
                    date_of_birth,
                    profile_picture ,
                    account_status,
                    ua.address_line1,
                    ua.address_line2,
                    ua.city,
                    ua.state,
                    ua.country,
                    ua.pincode,
                    ua.created_at,
                    ua.updated_at,
                    last_login
                
                FROM users
                LEFT JOIN user_addresses ua ON users.user_id = ua.user_id
                WHERE users.mobile_number = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, mobileNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(UserMapper.mapRowToUserDetails(resultSet));
            }

        }
        return Optional.empty();
    }

    public List<UserSummary> getAllUsers(Integer limit, Integer offset, String email, String role) throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT user_id, first_name, last_name, email, user_role, account_status
                FROM users
                WHERE 1=1
                """);

        if (email != null && !email.isBlank()) {
            query.append(" AND email LIKE ?");
        }

        if (role != null && !role.isBlank()) {
            query.append(" AND user_role = ?");
        }

        query.append(" LIMIT ? OFFSET ?");

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            int index = 1;

            if (email != null && !email.isBlank()) {
                ps.setString(index++, "%" + email + "%");
            }

            if (role != null && !role.isBlank()) {
                ps.setString(index++, role);
            }

            ps.setInt(index++, limit);
            ps.setInt(index, offset);

            ResultSet rs = ps.executeQuery();

            List<UserSummary> users = new ArrayList<>();

            while (rs.next()) {
                UserSummary u = new UserSummary();
                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setUserRole(rs.getString("user_role"));
                u.setAccountStatus(rs.getString("account_status"));

                users.add(u);
            }

            return users;
        }
    }


    public Integer createUser(UserCreateRequest userCreateRequest) throws SQLException {
        String query = """
                        INSERT INTO users(email, mobile_number, password_hash, user_role, first_name, last_name, date_of_birth,profile_picture)
                        VALUES (?, ?, ?, ?,?, ?, ?,?);
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, userCreateRequest.getEmail());
            preparedStatement.setString(2, userCreateRequest.getMobileNumber());
            preparedStatement.setString(3, userCreateRequest.getPassword());
            preparedStatement.setString(4, userCreateRequest.getUserRole());
            preparedStatement.setString(5, userCreateRequest.getFirstName());
            preparedStatement.setString(6, userCreateRequest.getLastName());
            preparedStatement.setDate(7, userCreateRequest.getDateOfBirth());
            preparedStatement.setString(8, userCreateRequest.getProfilePictureUrl());

            int userId;

            int affected = preparedStatement.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Failed to create user");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    userId = keys.getInt(1);
                } else {
                    throw new RuntimeException("Failed to create user");
                }
            }
            return userId;
        }
    }

    public Boolean createUserAddress(UserCreateRequest userCreateRequest, int userID) throws SQLException {
        String query = """
                       INSERT INTO user_addresses(user_id, address_line1, address_line2, city, state, country, pincode)
                       VALUES (?, ?, ?,?, ?, ?, ?);
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, userCreateRequest.getAddressLine1());
            preparedStatement.setString(3, userCreateRequest.getAddressLine2());
            preparedStatement.setString(4, userCreateRequest.getCity());
            preparedStatement.setString(5, userCreateRequest.getState());
            preparedStatement.setString(6, userCreateRequest.getCountry());
            preparedStatement.setString(7, userCreateRequest.getPincode());


            return preparedStatement.executeUpdate() != 0;

        }
    }


    public boolean updateUser(UserUpdateRequest userUpdateRequest, Integer userId) throws SQLException {
        String query = """
                UPDATE users
                SET
                    first_name = ?,
                    last_name = ?,
                    date_of_birth = ?,
                    profile_picture = ?
                WHERE user_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, userUpdateRequest.getFirstName());
            preparedStatement.setString(2, userUpdateRequest.getLastName());
            preparedStatement.setDate(3, userUpdateRequest.getDateOfBirth());
            preparedStatement.setString(4, userUpdateRequest.getProfilePictureUrl());
            preparedStatement.setInt(5, userId);

            return preparedStatement.executeUpdate() != 0;
        }
    }

    public Boolean updateUserAddress(UserUpdateRequest userAddressUpdateRequest, int userID) throws SQLException {
        String query = """
                UPDATE user_addresses
                SET address_line1 = ?,
                    address_line2 = ?,
                    city = ?,
                    state = ?,
                    country = ?,
                    pincode = ?
                WHERE user_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, userAddressUpdateRequest.getAddressLine1());
            preparedStatement.setString(2, userAddressUpdateRequest.getAddressLine2());
            preparedStatement.setString(3, userAddressUpdateRequest.getCity());
            preparedStatement.setString(4, userAddressUpdateRequest.getState());
            preparedStatement.setString(5, userAddressUpdateRequest.getCountry());
            preparedStatement.setString(6, userAddressUpdateRequest.getPincode());
            preparedStatement.setInt(7, userID);

            return preparedStatement.executeUpdate() != 0;

        }
    }

    public boolean deleteUser(Integer userID) throws SQLException {
        String query = """
                        DELETE FROM users
                        WHERE user_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userID);
            return preparedStatement.executeUpdate() != 0;
        }
    }

    public void updateLoginTimeStamp(Timestamp timestamp, Integer userId) throws SQLException {
        String query = """
                UPDATE users
                SET
                    last_login= ?
                WHERE user_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setTimestamp(1, timestamp);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeUpdate();
        }
    }


}
