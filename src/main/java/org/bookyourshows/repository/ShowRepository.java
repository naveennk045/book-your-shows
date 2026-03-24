package org.bookyourshows.repository;


import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSummary;
import org.bookyourshows.mapper.ShowMapper;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class ShowRepository {

    public int createShow(ShowCreateRequest request) throws SQLException {

        String sql = """
                INSERT INTO shows
                (theatre_id, screen_id, movie_id, show_date, start_time, end_time, base_price)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, request.getTheatreId());
            preparedStatement.setInt(2, request.getScreenId());
            preparedStatement.setInt(3, request.getMovieId());
            preparedStatement.setDate(4, request.getShowDate());
            preparedStatement.setTime(5, request.getStartTime());
            preparedStatement.setTime(6, request.getEndTime());
            preparedStatement.setDouble(7, request.getBasePrice());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) return resultSet.getInt(1);
        }

        throw new RuntimeException("Failed to create show");
    }

    public boolean isShowConflict(int screenId, Date date, Time start, Time end) throws SQLException {

        String sql = """
                SELECT 1 FROM shows
                WHERE screen_id = ?
                AND show_date = ?
                AND (
                    (start_time < ? AND end_time > ?) OR
                    (start_time < ? AND end_time > ?) OR
                    (start_time >= ? AND end_time <= ?)
                )
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, screenId);
            preparedStatement.setDate(2, date);

            preparedStatement.setTime(3, end);
            preparedStatement.setTime(4, end);

            preparedStatement.setTime(5, start);
            preparedStatement.setTime(6, start);

            preparedStatement.setTime(7, start);
            preparedStatement.setTime(8, end);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public List<ShowSummary> getShows(int theatreId, Date showDate) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    show_id,
                    screen_id,
                    movie_id,
                    show_date,
                    start_time,
                    end_time,
                    base_price
                FROM shows
                WHERE theatre_id = ?
                """);

        List<Object> params = new java.util.ArrayList<>();
        params.add(theatreId);

        if (showDate != null) {
            sql.append(" AND show_date = ?");
            params.add(showDate);
        }

        sql.append(" ORDER BY start_time");

        List<ShowSummary> shows = new java.util.ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                shows.add(ShowMapper.mapRowShowSummary(rs));
            }
        }

        return shows;
    }

    public Optional<ShowDetails> getShowById(int showId) throws SQLException {
        String sql = """ 
                SELECT
                    show_id,
                    theatre_id,
                    screen_id,
                    movie_id,
                    show_date,
                    start_time,
                    end_time,
                    base_price
                FROM shows
                WHERE show_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, showId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(ShowMapper.mapRowShowDetails(resultSet));
            }
        }
        return Optional.empty();
    }

    public boolean updateShowTiming(int showId, Time start, Time end) throws SQLException {

        String sql = """
                UPDATE shows
                SET start_time = ?, end_time = ?, status = 'RESCHEDULED'
                WHERE show_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setTime(1, start);
            preparedStatement.setTime(2, end);
            preparedStatement.setInt(3, showId);

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteShow(int showId) throws SQLException {

        String sql = "DELETE FROM shows WHERE show_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, showId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}
