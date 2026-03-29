package org.bookyourshows.mapper;


import org.bookyourshows.dto.address.AddressDTO;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressMapper {

    private static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static AddressDTO mapRowToAddress(ResultSet rs) throws SQLException {
        AddressDTO address = new AddressDTO();

        address.setAddressLine1(rs.getString("address_line1"));
        address.setAddressLine2(rs.getString("address_line2"));
        address.setCity(rs.getString("city"));
        address.setState(rs.getString("state"));
        address.setCountry(rs.getString("country"));
        address.setPincode(rs.getString("pincode"));

        if (hasColumn(rs, "latitude")) {
            address.setLatitude(rs.getBigDecimal("latitude"));
        }
        if (hasColumn(rs, "longitude")) {
            address.setLongitude(rs.getBigDecimal("longitude"));
        }

        return address;
    }
}