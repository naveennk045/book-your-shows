package org.bookyourshows.mapper;


import org.bookyourshows.dto.booking.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingMapper {
    private static boolean hasColumn(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static BookingSummary mapRowToBookingSummary(ResultSet resultSet) throws SQLException {
        BookingSummary bookingSummary = new BookingSummary();
        bookingSummary.setBookingId(resultSet.getInt("booking_id"));
        bookingSummary.setUserId(resultSet.getInt("user_id"));
        bookingSummary.setShowId(resultSet.getInt("show_id"));
        bookingSummary.setTotalAmount(resultSet.getDouble("total_amount"));
        bookingSummary.setBookingStatus(resultSet.getString("booking_status"));
        bookingSummary.setPaymentStatus(resultSet.getString("payment_status"));
        bookingSummary.setBookedAt(resultSet.getTimestamp("booked_at"));
        return bookingSummary;
    }

    public static BookingShowInfo mapRowToBookingShowInfo(ResultSet resultSet) throws SQLException {

        BookingShowInfo bookingShowInfo = new BookingShowInfo();
        bookingShowInfo.setShowId(resultSet.getInt("show_id"));
        bookingShowInfo.setShowDate(resultSet.getDate("show_date").toString());
        bookingShowInfo.setStartTime(resultSet.getTime("start_time").toString());
        bookingShowInfo.setEndTime(resultSet.getTime("end_time").toString());
        bookingShowInfo.setBasePrice(resultSet.getDouble("base_price"));

        bookingShowInfo.setTheatreId(resultSet.getInt("theatre_id"));
        bookingShowInfo.setTheatreName(resultSet.getString("theatre_name"));
        bookingShowInfo.setCity(resultSet.getString("city"));

        bookingShowInfo.setScreenId(resultSet.getInt("screen_id"));
        bookingShowInfo.setScreenName(resultSet.getString("screen_name"));
        bookingShowInfo.setScreenType(resultSet.getString("screen_type_name"));

        bookingShowInfo.setMovieId(resultSet.getInt("movie_id"));
        bookingShowInfo.setMovieTitle(resultSet.getString("movie_title"));
        bookingShowInfo.setLanguage(resultSet.getString("language"));
        bookingShowInfo.setCensorRating(resultSet.getString("censor_rating"));
        return bookingShowInfo;
    }

    public static BookingSeatInfo mapRowToBookingSeatInfo(ResultSet resultSet) throws SQLException {

        BookingSeatInfo bookingSeatInfo = new BookingSeatInfo();
        bookingSeatInfo.setBookingSeatId(resultSet.getInt("booking_seat_id"));
        bookingSeatInfo.setShowSeatId(resultSet.getInt("show_seat_id"));
        bookingSeatInfo.setPricePaid(resultSet.getDouble("price_paid"));
        bookingSeatInfo.setRowLabel(resultSet.getString("row_no"));
        bookingSeatInfo.setSeatNumber(resultSet.getString("seat_number"));
        bookingSeatInfo.setSeatCategory(resultSet.getString("seat_category_name"));
        return bookingSeatInfo;
    }

    public static BookingPaymentInfo mapRowToBookingPaymentInfo(ResultSet resultSet) throws SQLException {
        BookingPaymentInfo bookingPaymentInfo = new BookingPaymentInfo();
        bookingPaymentInfo.setTransactionId(resultSet.getInt("transaction_id"));
        bookingPaymentInfo.setAmount(resultSet.getDouble("amount"));
        bookingPaymentInfo.setPaymentGateway(resultSet.getString("payment_gateway"));
        bookingPaymentInfo.setGatewayTransactionId(resultSet.getString("gateway_transaction_id"));
        bookingPaymentInfo.setStatus(resultSet.getString("status"));
        bookingPaymentInfo.setCreatedAt(resultSet.getTimestamp("created_at"));
        bookingPaymentInfo.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return bookingPaymentInfo;
    }
}
