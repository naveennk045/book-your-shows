package org.bookyourshows.repository;


import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSeating;
import org.bookyourshows.mapper.ShowMapper;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ShowRepository {


    public Optional<ShowDetails> getShowById(int showId) throws SQLException {
        String query = """ 
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
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, showId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(ShowMapper.mapRowShowDetails(resultSet));
            }
        }
        return Optional.empty();
    }


    public Map<Integer, List<ShowSeating>> getShowSeats(Integer showId) throws SQLException {

        String query = """
                  SELECT ss.show_seat_id,
                        s.row_no,
                       s.seat_id,
                       s.seat_number,
                       sc.name                                                               AS category,
                       ss.status,
                       (sh.base_price * screen_types.price_multiplier * sc.price_multiplier) AS final_price
                FROM show_seating ss
                         JOIN shows as sh ON ss.show_id = sh.show_id
                         JOIN seats as s ON ss.seat_id = s.seat_id
                         JOIN seat_categories as sc ON s.seat_category_id = sc.seat_category_id
                         JOIN screens as scr ON s.screen_id = scr.screen_id
                         JOIN screen_types ON scr.screen_type_id = screen_types.screen_type_id
                WHERE sh.show_id = ?;
                
                """;


        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, showId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, List<ShowSeating>> showSeatLayoutMap = new HashMap<>();

            while (resultSet.next()) {
                int rowNo = resultSet.getInt("row_no");
                ShowSeating showSeating = ShowMapper.mapRowShowSeating(resultSet);

                List<ShowSeating> toBeUpdated = showSeatLayoutMap.getOrDefault(rowNo, new ArrayList<>());
                toBeUpdated.add(showSeating);
                showSeatLayoutMap.put(rowNo, toBeUpdated);
            }
            return showSeatLayoutMap;
        }
    }

    public List<ShowDetails> getShowsByTheatreId(int theatreId, Date showDate, Integer movieId) throws SQLException {

        StringBuilder query = new StringBuilder("""
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
            query.append(" AND show_date = ?");
            params.add(showDate);
        }
        if (movieId != null) {
            query.append(" AND movie_id = ?");
            params.add(movieId);
        }

        query.append(" ORDER BY start_time");

        List<ShowDetails> shows = new java.util.ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                shows.add(ShowMapper.mapRowShowDetails(resultSet));
            }
        }

        return shows;
    }

    public List<ShowDetails> getShows(Integer theatreId, String location, Date showDate, Integer movieId) throws SQLException {

        StringBuilder query = new StringBuilder("""
                SELECT
                    s.show_id,
                    s.theatre_id,
                    t.theatre_name,
                    ta.city,
                    s.movie_id,
                    s.screen_id,
                    scr.screen_name,
                    s.show_date,
                    s.start_time,
                    s.end_time,
                    s.base_price
                FROM shows AS s
                         JOIN theatres AS t ON s.theatre_id = t.theatre_id
                         JOIN theatre_addresses AS ta ON t.theatre_id = ta.theatre_id
                         JOIN screens  AS scr ON s.screen_id = scr.screen_id
                         JOIN screen_types AS scrt ON scrt.screen_type_id = scr.screen_type_id
                WHERE LOWER(ta.city) = LOWER (?) AND
                    s.movie_id = ? AND
                    s.show_date = ?
                    AND ( s.status = 'SCHEDULED'  OR s.status = 'RESCHEDULED')
                """);

        if (theatreId != null) {
            query.append(" AND s.theatre_id = ?");
        }

        query.append(" ORDER BY s.theatre_id, s.start_time;");
        List<ShowDetails> shows = new java.util.ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query.toString())) {


            preparedStatement.setString(1, location);
            preparedStatement.setInt(2, movieId);
            preparedStatement.setDate(3, showDate);
            if (theatreId != null) {
                preparedStatement.setInt(4, theatreId);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                shows.add(ShowMapper.mapRowShowDetails(resultSet));
            }
        }
        return shows;
    }


    public Integer createShow(ShowCreateRequest request) throws SQLException {

        String query = """
                INSERT INTO shows
                (theatre_id, screen_id, movie_id, show_date, start_time, end_time, base_price)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, request.getTheatreId());
            preparedStatement.setInt(2, request.getScreenId());
            preparedStatement.setInt(3, request.getMovieId());
            preparedStatement.setDate(4, request.getShowDate());
            preparedStatement.setTime(5, request.getStartTime());
            preparedStatement.setTime(6, request.getEndTime());
            preparedStatement.setDouble(7, request.getBasePrice());

            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) return resultSet.getInt(1);
            }
        }
        throw new RuntimeException("Failed to create show");
    }

    public boolean createShowSeating(Integer screenId, Integer showId) throws SQLException {

        String query = """
                
                INSERT INTO show_seating (show_id, seat_id, status)
                SELECT ?, seat_id, 'AVAILABLE'
                FROM seats
                WHERE screen_id = ? AND seat_status = 'AVAILABLE';
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, showId);
            preparedStatement.setInt(2, screenId);

            return preparedStatement.executeUpdate() != 0;
        }


    }


    public boolean isShowConflict(int screenId, Date date, Time start, Time end) throws SQLException {

        String query = """
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
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

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


    public boolean updateShowTiming(int showId, Time start, Time end) throws SQLException {

        String query = """
                UPDATE shows
                SET start_time = ?, end_time = ?, status = 'RESCHEDULED'
                WHERE show_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setTime(1, start);
            preparedStatement.setTime(2, end);
            preparedStatement.setInt(3, showId);

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteShow(int showId) throws SQLException {

        String query = "DELETE FROM shows WHERE show_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, showId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public Map<Integer, ShowSeating> getShowSeatsByShowId(Integer showId) throws SQLException {

        String query = """
                  SELECT ss.show_seat_id,
                        s.row_no,
                       s.seat_id,
                       s.seat_number,
                       sc.name                                                               AS category,
                       ss.status,
                       (sh.base_price * screen_types.price_multiplier * sc.price_multiplier) AS final_price
                FROM show_seating ss
                         JOIN shows as sh ON ss.show_id = sh.show_id
                         JOIN seats as s ON ss.seat_id = s.seat_id
                         JOIN seat_categories as sc ON s.seat_category_id = sc.seat_category_id
                         JOIN screens as scr ON s.screen_id = scr.screen_id
                         JOIN screen_types ON scr.screen_type_id = screen_types.screen_type_id
                WHERE sh.show_id = ?;
                
                """;


        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, showId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, ShowSeating> showSeatingMap = new HashMap<>();

            while (resultSet.next()) {
                int showSeatId = resultSet.getInt("show_seat_id");
                ShowSeating showSeating = ShowMapper.mapRowShowSeating(resultSet);
                showSeatingMap.put(showSeatId, showSeating);
            }
            return showSeatingMap;
        }
    }
}
