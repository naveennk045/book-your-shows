package org.bookyourshows.mapper;

import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.dto.user.UserSummary;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {

    public static UserDetails mapRowToUserDetails(ResultSet resultSet) throws SQLException {
        UserDetails userDetails = new UserDetails();

        userDetails.setUserId(resultSet.getInt("user_id"));
        userDetails.setFirstName(resultSet.getString("first_name"));
        userDetails.setLastName(resultSet.getString("last_name"));
        userDetails.setEmail(resultSet.getString("email"));
        userDetails.setMobileNumber(resultSet.getString("mobile_number"));
        userDetails.setDateOfBirth(resultSet.getDate("date_of_birth"));
        userDetails.setProfilePictureUrl(resultSet.getString("profile_picture"));
        userDetails.setAddressLine1(resultSet.getString("address_line1"));
        userDetails.setAddressLine2(resultSet.getString("address_line2"));
        userDetails.setCity(resultSet.getString("city"));
        userDetails.setState(resultSet.getString("state"));
        userDetails.setCountry(resultSet.getString("country"));
        userDetails.setPincode(resultSet.getString("pincode"));

        return userDetails;
    }

    public static UserSummary mapRowToUserSummary(ResultSet resultSet) throws SQLException {
        UserSummary userSummary = new UserSummary();

        userSummary.setUserId(resultSet.getInt("user_id"));
        userSummary.setFirstName(resultSet.getString("first_name"));
        userSummary.setLastName(resultSet.getString("last_name"));
        userSummary.setEmail(resultSet.getString("email"));
        userSummary.setAccountStatus(resultSet.getString("account_status"));

        return userSummary;
    }


}
