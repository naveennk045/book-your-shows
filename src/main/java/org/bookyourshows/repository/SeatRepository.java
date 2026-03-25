package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.seat.SeatCreateDetails;
import org.bookyourshows.dto.seat.SeatSummary;
import org.bookyourshows.dto.seat.SeatUpdateRequest;
import org.bookyourshows.mapper.SeatMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SeatRepository {

    public SeatRepository() {
    }

    public Optional<SeatSummary> getSeatById(int seatId) throws SQLException {

        String sql = """
                  SELECT s.seat_id,
                           s.row_no,
                           s.seat_number,
                           sc.name AS seat_category,
                           sc.price_multiplier,
                           s.seat_status
                
                    FROM seats AS s
                             JOIN seat_categories AS sc ON sc.seat_category_id = s.seat_category_id
                    WHERE s.seat_id = ?
                    ORDER BY s.row_no
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, seatId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(SeatMapper.mapRowToSeatSummary(resultSet));
            }
            return Optional.empty();
        }
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, screenId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int rowNo = resultSet.getInt("row_no");

                    int maxSeat = resultSet.getObject("max_seat") != null
                            ? resultSet.getInt("max_seat")
                            : 0;

                    rowMaxSeatMap.put(rowNo, maxSeat);
                }
            }
        }
        return rowMaxSeatMap;
    }

    public Map<Integer, List<SeatSummary>> getSeatByScreenId(int screenId) throws SQLException {

        String sql = """
                    SELECT s.seat_id,
                           s.row_no,
                           s.seat_number,
                           sc.name AS seat_category,
                           sc.price_multiplier,
                           s.seat_status
                
                    FROM seats AS s
                             JOIN seat_categories AS sc ON sc.seat_category_id = s.seat_category_id
                    WHERE screen_id = ?
                    ORDER BY s.row_no
                """;


        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, screenId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, List<SeatSummary>> seatLayoutMap = new HashMap<>();

            while (resultSet.next()) {
                int rowNo = resultSet.getInt("row_no");
                SeatSummary seatSummary = SeatMapper.mapRowToSeatSummary(resultSet);

                List<SeatSummary> toBeUpdated = seatLayoutMap.getOrDefault(rowNo, new ArrayList<>());
                toBeUpdated.add(seatSummary);
                seatLayoutMap.put(rowNo, toBeUpdated);
            }

            return seatLayoutMap;
        }
    }


    public void addAllSeats(List<SeatCreateDetails> seats) throws SQLException {

        String query = "INSERT INTO seats (screen_id, seat_category_id, row_no, seat_number) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (SeatCreateDetails seat : seats) {

                preparedStatement.setInt(1, seat.getScreenId());
                preparedStatement.setInt(2, seat.getSeatCategoryId());
                preparedStatement.setInt(3, seat.getRowNo());
                preparedStatement.setString(4, seat.getSeatNumber());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }


    public boolean updateSeat(int seatId, SeatUpdateRequest seatUpdateRequest) throws SQLException {
        String query = """
                UPDATE seats
                SET
                seat_category_id = ?,
                seat_status = ?
                WHERE seat_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, seatUpdateRequest.getSeatCategoryId());
            preparedStatement.setString(2, seatUpdateRequest.getStatus());
            preparedStatement.setInt(3, seatId);

            return preparedStatement.executeUpdate() != 0;
        }
    }


    public boolean deleteSeat(int seatId) throws SQLException {
        String query = "DELETE FROM seats WHERE seat_id = ?";
        try (Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, seatId);
            return preparedStatement.executeUpdate() != 0;
        }
    }
}
