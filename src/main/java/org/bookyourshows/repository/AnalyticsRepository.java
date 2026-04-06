package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.analytics.*;
import org.bookyourshows.mapper.AnalyticsMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsRepository {

    public List<MoviePerformanceResponse> getMoviePerformance(MoviePerformanceRequest request) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.movie_id AS movie_id,
                    m.title    AS movie_name,
                    COUNT(DISTINCT sh.show_id) AS no_of_shows,
                    SUM(CASE WHEN ss.status = 'BOOKED' THEN 1 ELSE 0 END)    AS seats_booked,
                    SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS seats_not_booked
                FROM shows sh
                JOIN movies m ON m.movie_id = sh.movie_id
                JOIN show_seating ss ON ss.show_id = sh.show_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (request.getTheatreId() != null) {
            sql.append(" AND sh.theatre_id = ?");
            params.add(request.getTheatreId());
        }

        if (request.getYear() != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(request.getYear());
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(request.getMonth());
        }

        sql.append(" GROUP BY m.movie_id, m.title ");

        String sort = request.getSort();
        if (sort == null || sort.isBlank()
                || "bookings".equalsIgnoreCase(sort)
                || "seats".equalsIgnoreCase(sort)
                || "seats_booked".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY seats_booked DESC ");
        } else if ("shows".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY no_of_shows DESC ");
        } else {
            sql.append(" ORDER BY seats_booked DESC ");
        }

        sql.append(" LIMIT ? ");
        params.add(request.getLimit() == null ? 10 : request.getLimit());

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<MoviePerformanceResponse> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(AnalyticsMapper.mapRowToMoviePerformanceResponse(resultSet));
                }
                return results;
            }
        }
    }

    public List<PeakShowTimeResponse> getPeakShowTimes(Integer theatreId, Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    CASE
                        WHEN HOUR(sh.start_time) >= 7  AND HOUR(sh.start_time) < 12 THEN 'Morning'
                        WHEN HOUR(sh.start_time) >= 12 AND HOUR(sh.start_time) < 15 THEN 'Matinee'
                        WHEN HOUR(sh.start_time) >= 15 AND HOUR(sh.start_time) < 19 THEN 'Evening'
                        WHEN HOUR(sh.start_time) >= 19 AND HOUR(sh.start_time) < 22 THEN 'Night'
                        ELSE 'Late Night'
                    END AS show_category,
                    SUM(IF(ss.status = 'BOOKED', 1, 0))     AS seats_booked,
                    SUM(IF(ss.status = 'AVAILABLE', 1, 0))  AS seats_not_booked
                FROM shows sh
                JOIN show_seating ss ON ss.show_id = sh.show_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (theatreId != null) {
            sql.append(" AND sh.theatre_id = ?");
            params.add(theatreId);
        }

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }

        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY show_category ORDER BY seats_booked DESC");

        List<PeakShowTimeResponse> list = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PeakShowTimeResponse peakShowTimeResponse = new PeakShowTimeResponse();
                    peakShowTimeResponse.setShowCategory(resultSet.getString("show_category"));
                    peakShowTimeResponse.setSeatsBooked(resultSet.getInt("seats_booked"));
                    peakShowTimeResponse.setSeatsNotBooked(resultSet.getInt("seats_not_booked"));
                    list.add(peakShowTimeResponse);
                }
            }
        }

        return list;
    }

    public List<UserBookingAnalytics> getUserBookingsAnalytics(Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    u.user_id,
                    u.first_name,
                    COUNT(DISTINCT b.booking_id) AS no_of_bookings,
                    COUNT(bsd.show_seat_id)      AS no_of_seats
                FROM users u
                LEFT JOIN bookings b   ON b.user_id   = u.user_id
                LEFT JOIN booking_seat_details bsd ON bsd.booking_id = b.booking_id
                LEFT JOIN shows sh     ON sh.show_id  = b.show_id
                WHERE b.booking_status = 'CONFIRMED'
                """);

        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }

        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY u.user_id, u.first_name ORDER BY no_of_seats DESC");

        List<UserBookingAnalytics> list = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UserBookingAnalytics analytics = new UserBookingAnalytics();
                    analytics.setUserId(resultSet.getInt("user_id"));
                    analytics.setFirstName(resultSet.getString("first_name"));
                    analytics.setNoOfBookings(resultSet.getInt("no_of_bookings"));
                    analytics.setNoOfSeats(resultSet.getInt("no_of_seats"));
                    list.add(analytics);
                }
            }
        }

        return list;
    }

    public List<TheatreBookingAnalytics> getTheatreBookingsAnalytics(Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    t.theatre_id,
                    t.theatre_name,
                    COUNT(DISTINCT b.booking_id) AS no_of_bookings,
                    COUNT(bsd.show_seat_id)      AS no_of_seats_booked
                FROM theatres t
                LEFT JOIN shows sh ON sh.theatre_id = t.theatre_id
                LEFT JOIN bookings b ON b.show_id = sh.show_id AND b.booking_status = 'CONFIRMED'
                LEFT JOIN booking_seat_details bsd ON bsd.booking_id = b.booking_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }

        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY t.theatre_id, t.theatre_name ORDER BY no_of_seats_booked DESC");

        return getTheatreAnalytics(sql.toString(), params);
    }

    public List<TheatreBookingAnalytics> getTheatreBookingsAnalyticsByTheatre(Integer theatreId, Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    t.theatre_id,
                    t.theatre_name,
                    COUNT(DISTINCT b.booking_id) AS no_of_bookings,
                    COUNT(bsd.show_seat_id)      AS no_of_seats_booked
                FROM theatres t
                LEFT JOIN shows sh ON sh.theatre_id = t.theatre_id
                LEFT JOIN bookings b ON b.show_id = sh.show_id AND b.booking_status = 'CONFIRMED'
                LEFT JOIN booking_seat_details bsd ON bsd.booking_id = b.booking_id
                WHERE t.theatre_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(theatreId);

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }

        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY t.theatre_id, t.theatre_name");

        return getTheatreAnalytics(sql.toString(), params);
    }

    public List<TopSpentUser> getTopSpentUsers(Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    u.user_id,
                    u.first_name,
                    SUM(b.total_amount) AS amount_spent
                FROM users u
                LEFT JOIN bookings b ON b.user_id = u.user_id
                LEFT JOIN shows sh   ON sh.show_id = b.show_id
                WHERE b.booking_status = 'CONFIRMED'
                """);

        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }

        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY u.user_id, u.first_name ORDER BY amount_spent DESC LIMIT 10");

        List<TopSpentUser> list = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    TopSpentUser topSpentUser = new TopSpentUser();
                    topSpentUser.setUserId(resultSet.getInt("user_id"));
                    topSpentUser.setFirstName(resultSet.getString("first_name"));
                    topSpentUser.setAmountSpent(resultSet.getDouble("amount_spent"));
                    list.add(topSpentUser);
                }
            }
        }

        return list;
    }


    private List<TheatreBookingAnalytics> getTheatreAnalytics(String sql, List<Object> params) throws SQLException {

        List<TheatreBookingAnalytics> list = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    TheatreBookingAnalytics analytics = new TheatreBookingAnalytics();
                    analytics.setTheatreId(resultSet.getInt("theatre_id"));
                    analytics.setTheatreName(resultSet.getString("theatre_name"));
                    analytics.setNoOfBookings(resultSet.getInt("no_of_bookings"));
                    analytics.setNoOfSeatsBooked(resultSet.getInt("no_of_seats_booked"));
                    list.add(analytics);
                }
            }
        }

        return list;
    }

    public List<TheatreRevenueAnalytics> getTheatreRevenue(Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    t.theatre_id,
                    t.theatre_name,
                    SUM(b.total_amount) AS revenue
                FROM theatres t
                JOIN shows sh ON sh.theatre_id = t.theatre_id
                JOIN bookings b ON b.show_id = sh.show_id
                WHERE b.booking_status = 'CONFIRMED'
                """);

        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }
        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY t.theatre_id, t.theatre_name ORDER BY revenue DESC");

        return fetchTheatreRevenue(sql.toString(), params);
    }

    public List<TheatreRevenueAnalytics> getTheatreRevenueByTheatre(Integer theatreId, Integer year, Integer month) throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    t.theatre_id,
                    t.theatre_name,
                    SUM(b.total_amount) AS revenue
                FROM theatres t
                JOIN shows sh ON sh.theatre_id = t.theatre_id
                JOIN bookings b ON b.show_id = sh.show_id
                WHERE b.booking_status = 'CONFIRMED'
                AND t.theatre_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(theatreId);

        if (year != null) {
            sql.append(" AND YEAR(sh.show_date) = ?");
            params.add(year);
        }
        if (month != null) {
            sql.append(" AND MONTH(sh.show_date) = ?");
            params.add(month);
        }

        sql.append(" GROUP BY t.theatre_id, t.theatre_name");

        return fetchTheatreRevenue(sql.toString(), params);
    }

    private List<TheatreRevenueAnalytics> fetchTheatreRevenue(String sql, List<Object> params) throws SQLException {

        List<TheatreRevenueAnalytics> list = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TheatreRevenueAnalytics analytics = new TheatreRevenueAnalytics();
                    analytics.setTheatreId(rs.getInt("theatre_id"));
                    analytics.setTheatreName(rs.getString("theatre_name"));
                    analytics.setRevenue(rs.getDouble("revenue"));
                    list.add(analytics);
                }
            }
        }
        return list;
    }
}