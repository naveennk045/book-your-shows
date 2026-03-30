package org.bookyourshows.mapper;

import org.bookyourshows.dto.theatre.Theatre;
import org.bookyourshows.dto.theatre.TheatreAddress;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TheatreMapper {


    private static boolean hasColumn(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static TheatreSummary mapRowToTheatreSummary(ResultSet resultSet) throws SQLException {

        TheatreSummary theatreSummary = new TheatreSummary();

        theatreSummary.setTheatreId(resultSet.getInt("theatre_id"));
        theatreSummary.setTheatreName(resultSet.getString("theatre_name"));
        theatreSummary.setTotalScreens(resultSet.getInt("total_screens"));

        TheatreAddress address = new TheatreAddress();
        theatreSummary.setCity(resultSet.getString("city"));

        return theatreSummary;
    }

    public static TheatreDetails mapRowToTheatreDetails(ResultSet resultSet) throws SQLException {

        Theatre theatre = new Theatre();
        TheatreAddress theatreAddress = new TheatreAddress();
        TheatreDetails theatreDetails = new TheatreDetails();

        theatre.setTheatreId(resultSet.getInt("theatre_id"));
        theatre.setTheatreName(resultSet.getString("theatre_name"));
        theatre.setEmail(resultSet.getString("email"));
        theatre.setContactNumber(resultSet.getString("contact_number"));
        theatre.setTotalScreens(resultSet.getInt("total_screens"));
        if (hasColumn(resultSet, "owner_id")) {
            theatre.setOwnerId(resultSet.getInt("owner_id"));
        }

        theatreDetails.setTheatre(theatre);


        theatreAddress.setAddressId(resultSet.getInt("address_id"));
        theatreAddress.setAddressLine1(resultSet.getString("address_line1"));
        theatreAddress.setCity(resultSet.getString("city"));
        theatreAddress.setState(resultSet.getString("state"));
        theatreAddress.setPincode(resultSet.getString("pincode"));
        theatreAddress.setLatitude(resultSet.getDouble("latitude"));
        theatreAddress.setLongitude(resultSet.getDouble("longitude"));
        theatreDetails.setAddress(theatreAddress);

        return theatreDetails;
    }
}
