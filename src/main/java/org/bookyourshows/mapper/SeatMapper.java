package org.bookyourshows.mapper;

import org.bookyourshows.dto.seat.SeatSummary;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeatMapper {

    public static SeatSummary mapRowToSeatSummary(ResultSet rs) throws SQLException {

        SeatSummary seat = new SeatSummary();

        seat.setSeatId(rs.getInt("seat_id"));
        seat.setSeatCategory(rs.getString("seat_category"));
        seat.setSeatNumber(String.valueOf(rs.getString("seat_number")));
        seat.setPriceMultiplier(rs.getDouble("price_multiplier"));
        seat.setStatus(rs.getString("seat_status"));

        return seat;
    }
}
