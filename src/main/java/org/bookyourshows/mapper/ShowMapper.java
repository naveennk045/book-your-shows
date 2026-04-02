package org.bookyourshows.mapper;

import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSeating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class ShowMapper {

    public static ShowSeating mapRowShowSeating(ResultSet resultSet) throws SQLException {
        ShowSeating showSeating = new ShowSeating();
        showSeating.setShowSeatId(resultSet.getInt("show_seat_id"));
        showSeating.setSeatNumber(resultSet.getString("seat_number"));
        showSeating.setCategory(resultSet.getString("category"));
        showSeating.setStatus(resultSet.getString("status"));
        showSeating.setFinalPrice(resultSet.getDouble("final_price"));
        showSeating.setRowNo(resultSet.getInt("row_no"));
        return showSeating;
    }

    public static ShowDetails mapRowShowDetails(ResultSet resultSet) throws SQLException {
        ShowDetails showDetails = new ShowDetails();

        if (hasColumn(resultSet, "show_id")) showDetails.setShowId(resultSet.getInt("show_id"));
        if (hasColumn(resultSet, "theatre_id")) showDetails.setTheatreId(resultSet.getInt("theatre_id"));
        if (hasColumn(resultSet, "theatre_name")) showDetails.setTheatreName(resultSet.getString("theatre_name"));
        if (hasColumn(resultSet, "city")) showDetails.setTheatreLocation(resultSet.getString("city"));
        if (hasColumn(resultSet, "screen_id")) showDetails.setScreenId(resultSet.getInt("screen_id"));
        if (hasColumn(resultSet, "screen_name")) showDetails.setScreenName(resultSet.getString("screen_name"));
        if (hasColumn(resultSet, "movie_id")) showDetails.setMovieId(resultSet.getInt("movie_id"));
        if (hasColumn(resultSet, "movie_name")) showDetails.setMovieName(resultSet.getString("movie_name"));
        if (hasColumn(resultSet, "show_date")) showDetails.setShowDate(resultSet.getDate("show_date"));
        if (hasColumn(resultSet, "start_time")) showDetails.setStartTime(resultSet.getTime("start_time"));
        if (hasColumn(resultSet, "end_time")) showDetails.setEndTime(resultSet.getTime("end_time"));
        if (hasColumn(resultSet, "base_price")) showDetails.setBasePrice(resultSet.getDouble("base_price"));
        if (hasColumn(resultSet, "status")) showDetails.setStatus(resultSet.getString("status"));


        return showDetails;
    }

    public static Map<String, String> mapShowDetailsToHashMap(ShowDetails showDetail) {
        HashMap<String, String> showDetailsMap = new HashMap<>();
        showDetailsMap.put("show_id", String.valueOf(showDetail.getShowId()));
        showDetailsMap.put("theatre_id", String.valueOf(showDetail.getTheatreId()));
        showDetailsMap.put("theatre_name", nullSafe(showDetail.getTheatreName()));
        showDetailsMap.put("city", nullSafe(showDetail.getTheatreLocation()));
        showDetailsMap.put("screen_id", String.valueOf(showDetail.getScreenId()));
        showDetailsMap.put("screen_name", nullSafe(showDetail.getScreenName()));
        showDetailsMap.put("movie_id", String.valueOf(showDetail.getMovieId()));
        showDetailsMap.put("show_date", showDetail.getShowDate() != null ? String.valueOf(showDetail.getShowDate().getTime()) : "0");
        showDetailsMap.put("start_time", showDetail.getStartTime() != null ? String.valueOf(showDetail.getStartTime().getTime()) : "0");
        showDetailsMap.put("end_time", showDetail.getEndTime() != null ? String.valueOf(showDetail.getEndTime().getTime()) : "0");
        showDetailsMap.put("base_price", showDetail.getBasePrice() != null ? String.valueOf(showDetail.getBasePrice()) : "0");
        showDetailsMap.put("status", showDetail.getStatus());

        return showDetailsMap;
    }

    public static ShowDetails mapHashMapToShowDetails(Map<String, String> showDetailsMap) {

        ShowDetails showDetails = new ShowDetails();
        showDetails.setShowId(parseIntSafe(showDetailsMap.get("show_id")));
        showDetails.setTheatreId(parseIntSafe(showDetailsMap.get("theatre_id")));
        showDetails.setTheatreName(nullSafe(showDetailsMap.get("theatre_name")));
        showDetails.setTheatreLocation(nullSafe(showDetailsMap.get("city")));
        showDetails.setScreenId(parseIntSafe(showDetailsMap.get("screen_id")));
        showDetails.setMovieId(parseIntSafe(showDetailsMap.get("movie_id")));

        long showDateMillis = parseLongSafe(showDetailsMap.get("show_date"));
        showDetails.setShowDate(showDateMillis > 0 ? new Date(showDateMillis) : null);

        long startTimeMillis = parseLongSafe(showDetailsMap.get("start_time"));
        showDetails.setStartTime(startTimeMillis > 0 ? new Time(startTimeMillis) : null);

        long endTimeMillis = parseLongSafe(showDetailsMap.get("end_time"));
        showDetails.setEndTime(endTimeMillis > 0 ? new Time(endTimeMillis) : null);

        showDetails.setBasePrice(parseDoubleSafe(showDetailsMap.get("base_price")));
        showDetails.setStatus(nullSafe(showDetailsMap.get("status")));


        return showDetails;
    }

    public static Map<String, String> mapShowSeatingToHashData(ShowSeating showSeating) {

        HashMap<String, String> showSeatingMap = new HashMap<>();

        showSeatingMap.put("show_seat_id", showSeating.getShowSeatId() != null ? String.valueOf(showSeating.getShowSeatId()) : "0");
        showSeatingMap.put("seat_id", showSeating.getSeatId() != null ? String.valueOf(showSeating.getSeatId()) : "0");
        showSeatingMap.put("seat_number", nullSafe(showSeating.getSeatNumber()));
        showSeatingMap.put("category", nullSafe(showSeating.getCategory()));
        showSeatingMap.put("status", nullSafe(showSeating.getStatus()));
        showSeatingMap.put("final_price", showSeating.getFinalPrice() != null ? String.valueOf(showSeating.getFinalPrice()) : "0.0");
        showSeatingMap.put("row_no", String.valueOf(showSeating.getRowNo()));
        return showSeatingMap;
    }

    public static ShowSeating mapHashMapToShowSeating(Map<String, String> showSeatingMap) {

        ShowSeating showSeating = new ShowSeating();
        showSeating.setShowSeatId(parseIntSafe(showSeatingMap.get("show_seat_id")));
        showSeating.setSeatId(parseIntSafe(showSeatingMap.get("seat_id")));
        showSeating.setSeatNumber(nullSafe(showSeatingMap.get("seat_number")));
        showSeating.setCategory(nullSafe(showSeatingMap.get("category")));
        showSeating.setStatus(nullSafe(showSeatingMap.get("status")));
        showSeating.setFinalPrice(parseDoubleSafe(showSeatingMap.get("final_price")));
        showSeating.setRowNo(parseIntSafe(showSeatingMap.get("row_no")));

        return showSeating;
    }


    private static boolean hasColumn(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long parseLongSafe(String value) {
        if (value == null || value.isEmpty()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static double parseDoubleSafe(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}