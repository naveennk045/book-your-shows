package org.bookyourshows.mapper;

import org.bookyourshows.dto.theatre.Theatre;
import org.bookyourshows.dto.theatre.TheatreAddress;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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
        if (hasColumn(resultSet, "status")) {
            theatre.setStatus(resultSet.getString("status"));
        }
        if (hasColumn(resultSet, "registration_date")) {
            theatre.setRegistrationDate(resultSet.getTimestamp("registration_date"));
        }
        if (hasColumn(resultSet, "approval_date")) {
            theatre.setApprovalDate(resultSet.getTimestamp("approval_date"));
        }
        if (hasColumn(resultSet, "license_document")) {
            theatre.setLicenseDocument(resultSet.getString("license_document"));
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


    public static Map<String, String> mapTheatreToHash(TheatreDetails theatreDetails) {
        Map<String, String> theatreDetailsMap = new HashMap<>();

        Theatre theatre = theatreDetails.getTheatre();
        theatreDetailsMap.put("theatre_id", String.valueOf(theatre.getTheatreId()));
        theatreDetailsMap.put("owner_id", String.valueOf(theatre.getOwnerId()));
        theatreDetailsMap.put("theatre_name", nullSafe(theatre.getTheatreName()));
        theatreDetailsMap.put("email", nullSafe(theatre.getEmail()));
        theatreDetailsMap.put("contact_number", nullSafe(theatre.getContactNumber()));
        theatreDetailsMap.put("total_screens", String.valueOf(theatre.getTotalScreens()));
        theatreDetailsMap.put("license_document", nullSafe(theatre.getLicenseDocument()));
        theatreDetailsMap.put("status", nullSafe(theatre.getStatus()));
        theatreDetailsMap.put("registration_date", theatre.getRegistrationDate() != null
                ? theatre.getRegistrationDate().toString() : "");
        theatreDetailsMap.put("approval_date", theatre.getApprovalDate() != null
                ? theatre.getApprovalDate().toString() : "");

        TheatreAddress theatreAddress = theatreDetails.getAddress();
        theatreDetailsMap.put("address_id", String.valueOf(theatreAddress.getAddressId()));
        theatreDetailsMap.put("address_line1", nullSafe(theatreAddress.getAddressLine1()));
        theatreDetailsMap.put("city", nullSafe(theatreAddress.getCity()));
        theatreDetailsMap.put("state", nullSafe(theatreAddress.getState()));
        theatreDetailsMap.put("pincode", nullSafe(theatreAddress.getPincode()));
        theatreDetailsMap.put("latitude", String.valueOf(theatreAddress.getLatitude()));
        theatreDetailsMap.put("longitude", String.valueOf(theatreAddress.getLongitude()));

        return theatreDetailsMap;
    }


    public static TheatreDetails mapHashMapToTheatreDetails(Map<String, String> theatreDetailsMap) {
        if (theatreDetailsMap == null || theatreDetailsMap.isEmpty()) {
            return null;
        }

        Theatre theatre = new Theatre();
        theatre.setTheatreId(parseIntSafe(theatreDetailsMap.get("theatre_id")));
        theatre.setOwnerId(parseIntSafe(theatreDetailsMap.get("owner_id")));
        theatre.setTheatreName(theatreDetailsMap.get("theatre_name"));
        theatre.setEmail(theatreDetailsMap.get("email"));
        theatre.setContactNumber(theatreDetailsMap.get("contact_number"));
        theatre.setTotalScreens(parseIntSafe(theatreDetailsMap.get("total_screens")));
        theatre.setLicenseDocument(theatreDetailsMap.get("license_document"));
        theatre.setStatus(theatreDetailsMap.get("status"));
        // In mapHashToTheatreDetails — parse safely
        theatre.setRegistrationDate(parseTimestampSafe(theatreDetailsMap.get("registration_date")));
        theatre.setApprovalDate(parseTimestampSafe(theatreDetailsMap.get("approval_date")));

        TheatreAddress theatreAddress = new TheatreAddress();
        theatreAddress.setAddressId(parseIntSafe(theatreDetailsMap.get("address_id")));
        theatreAddress.setAddressLine1(theatreDetailsMap.get("address_line1"));
        theatreAddress.setCity(theatreDetailsMap.get("city"));
        theatreAddress.setState(theatreDetailsMap.get("state"));
        theatreAddress.setPincode(theatreDetailsMap.get("pincode"));
        theatreAddress.setLatitude(parseDoubleSafe(theatreDetailsMap.get("latitude")));
        theatreAddress.setLongitude(parseDoubleSafe(theatreDetailsMap.get("longitude")));

        TheatreDetails theatreDetails = new TheatreDetails();
        theatreDetails.setTheatre(theatre);
        theatreDetails.setAddress(theatreAddress);

        return theatreDetails;
    }

    public static TheatreSummary mapHashMapToTheatreSummary(Map<String, String> theatreDetailsMap) {
        if (theatreDetailsMap == null || theatreDetailsMap.isEmpty()) {
            return null;
        }

        TheatreSummary theatreSummary = new TheatreSummary();

        theatreSummary.setTheatreId(parseIntSafe(theatreDetailsMap.get("theatre_id")));
        theatreSummary.setTheatreName(theatreDetailsMap.get("theatre_name"));
        theatreSummary.setTotalScreens(parseIntSafe(theatreDetailsMap.get("total_screens")));
        theatreSummary.setCity(theatreDetailsMap.get("city"));


        return theatreSummary;
    }

    private static double parseDoubleSafe(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Timestamp parseTimestampSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Timestamp.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

}
