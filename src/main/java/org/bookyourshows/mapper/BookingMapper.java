package org.bookyourshows.mapper;


import org.bookyourshows.dto.booking.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingMapper {

    public static BookingSummary mapRowToBookingSummary(ResultSet rs) throws SQLException {
        BookingSummary b = new BookingSummary();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setShowId(rs.getInt("show_id"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setBookedAt(rs.getTimestamp("booked_at"));
        return b;
    }

    public static BookingShowInfo mapRowToBookingShowInfo(ResultSet rs) throws SQLException {
        BookingShowInfo s = new BookingShowInfo();
        s.setShowId(rs.getInt("show_id"));
        s.setShowDate(rs.getDate("show_date").toString());
        s.setStartTime(rs.getTime("start_time").toString());
        s.setEndTime(rs.getTime("end_time").toString());
        s.setBasePrice(rs.getDouble("base_price"));

        s.setTheatreId(rs.getInt("theatre_id"));
        s.setTheatreName(rs.getString("theatre_name"));
        s.setCity(rs.getString("city"));

        s.setScreenId(rs.getInt("screen_id"));
        s.setScreenName(rs.getString("screen_name"));
        s.setScreenType(rs.getString("screen_type_name"));

        s.setMovieId(rs.getInt("movie_id"));
        s.setMovieTitle(rs.getString("movie_title"));
        s.setLanguage(rs.getString("language"));
        s.setCensorRating(rs.getString("censor_rating"));
        return s;
    }

    public static BookingSeatInfo mapRowToBookingSeatInfo(ResultSet rs) throws SQLException {
        BookingSeatInfo s = new BookingSeatInfo();
        s.setBookingSeatId(rs.getInt("booking_seat_id"));
        s.setShowSeatId(rs.getInt("show_seat_id"));
        s.setPricePaid(rs.getDouble("price_paid"));
        s.setRowLabel(rs.getString("row_no"));
        s.setSeatNumber(rs.getString("seat_number"));
        s.setSeatCategory(rs.getString("seat_category_name"));
        return s;
    }

    public static BookingPaymentInfo mapRowToBookingPaymentInfo(ResultSet rs) throws SQLException {
        BookingPaymentInfo p = new BookingPaymentInfo();
        p.setTransactionId(rs.getInt("transaction_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentGateway(rs.getString("payment_gateway"));
        p.setGatewayTransactionId(rs.getString("gateway_transaction_id"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
