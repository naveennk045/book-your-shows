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

    public static Map<String, String> mapShowToHashData(ShowDetails showDetail) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("show_id", String.valueOf(showDetail.getShowId()));
        hashMap.put("theatre_id", String.valueOf(showDetail.getTheatreId()));
        hashMap.put("theatre_name", nullSafe(showDetail.getTheatreName()));
        hashMap.put("city", nullSafe(showDetail.getTheatreLocation()));
        hashMap.put("screen_id", String.valueOf(showDetail.getScreenId()));
        hashMap.put("screen_name", nullSafe(showDetail.getScreenName()));
        hashMap.put("movie_id", String.valueOf(showDetail.getMovieId()));
        hashMap.put("show_date", showDetail.getShowDate() != null ? String.valueOf(showDetail.getShowDate().getTime()) : "0");
        hashMap.put("start_time", showDetail.getStartTime() != null ? String.valueOf(showDetail.getStartTime().getTime()) : "0");
        hashMap.put("end_time", showDetail.getEndTime() != null ? String.valueOf(showDetail.getEndTime().getTime()) : "0");
        hashMap.put("base_price", showDetail.getBasePrice() != null ? String.valueOf(showDetail.getBasePrice()) : "0");
        return hashMap;
    }

    public static ShowDetails mapHashToShowDetails(Map<String, String> showDetailsMap) {
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

        return showDetails;
    }

    public static Map<String, String> mapShowSeatingToHashData(ShowSeating showSeating) {

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("show_seat_id", showSeating.getShowSeatId() != null ? String.valueOf(showSeating.getShowSeatId()) : "0");
        hashMap.put("seat_id", showSeating.getSeatId() != null ? String.valueOf(showSeating.getSeatId()) : "0");
        hashMap.put("seat_number", nullSafe(showSeating.getSeatNumber()));
        hashMap.put("category", nullSafe(showSeating.getCategory()));
        hashMap.put("status", nullSafe(showSeating.getStatus()));
        hashMap.put("final_price", showSeating.getFinalPrice() != null ? String.valueOf(showSeating.getFinalPrice()) : "0.0");
        hashMap.put("row_no", String.valueOf(showSeating.getRowNo()));
        return hashMap;
    }

    public static ShowSeating mapHashToShowSeating(Map<String, String> hashMap) {

        ShowSeating showSeating = new ShowSeating();
        showSeating.setShowSeatId(parseIntSafe(hashMap.get("show_seat_id")));
        showSeating.setSeatId(parseIntSafe(hashMap.get("seat_id")));
        showSeating.setSeatNumber(nullSafe(hashMap.get("seat_number")));
        showSeating.setCategory(nullSafe(hashMap.get("category")));
        showSeating.setStatus(nullSafe(hashMap.get("status")));
        showSeating.setFinalPrice(parseDoubleSafe(hashMap.get("final_price")));
        showSeating.setRowNo(parseIntSafe(hashMap.get("row_no")));

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