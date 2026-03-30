package org.bookyourshows.mapper;


import org.bookyourshows.dto.address.AddressDTO;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressMapper {

    private static boolean hasColumn(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static AddressDTO mapRowToAddress(ResultSet resultSet) throws SQLException {
        AddressDTO address = new AddressDTO();

        address.setAddressLine1(resultSet.getString("address_line1"));
        address.setAddressLine2(resultSet.getString("address_line2"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setCountry(resultSet.getString("country"));
        address.setPincode(resultSet.getString("pincode"));

        if (hasColumn(resultSet, "latitude")) {
            address.setLatitude(resultSet.getBigDecimal("latitude"));
        }
        if (hasColumn(resultSet, "longitude")) {
            address.setLongitude(resultSet.getBigDecimal("longitude"));
        }

        return address;
    }
}