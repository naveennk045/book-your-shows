package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.seat.SeatCreateDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatRepository {

    public SeatRepository() {
    }


    public Map<Integer, Integer> getMaxSeatNumberByScreen(int screenId) throws SQLException {

        String sql = """
                        SELECT
                            row_no,
                            MAX(CAST(REGEXP_SUBSTR(seat_number, '[0-9]+$') AS UNSIGNED)) AS max_seat
                        FROM seats
                        WHERE screen_id = ?
                        GROUP BY row_no
                """;

        Map<Integer, Integer> rowMaxSeatMap = new HashMap<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, screenId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rowNo = rs.getInt("row_no");

                    int maxSeat = rs.getObject("max_seat") != null
                            ? rs.getInt("max_seat")
                            : 0;

                    rowMaxSeatMap.put(rowNo, maxSeat);
                }
            }
        }
        return rowMaxSeatMap;
    }


    public void addAllSeats(List<SeatCreateDetails> seats) throws SQLException {

        String query = "INSERT INTO seats (screen_id, seat_category_id, row_no, seat_number) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(query);
            for (SeatCreateDetails seat : seats) {

                ps.setInt(1, seat.getScreenId());
                ps.setInt(2, seat.getSeatCategoryId());
                ps.setInt(3, seat.getRowNo());
                ps.setString(4, seat.getSeatNumber());

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }


}
