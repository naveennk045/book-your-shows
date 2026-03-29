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

            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<MoviePerformanceResponse> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(AnalyticsMapper.mapRowToMoviePerformanceResponse(rs));
                }
                return results;
            }
        }
    }

    public List<PeakShowTimeResponse> getPeakShowTimes(Integer theatreId, Integer year, Integer month) throws SQLException {

        String query = """
                    SELECT 
                        CASE
                            WHEN HOUR(sh.start_time) >= 7 AND HOUR(sh.start_time) < 12 THEN 'Morning'
                            WHEN HOUR(sh.start_time) >= 12 AND HOUR(sh.start_time) < 15 THEN 'Matinee'
                            WHEN HOUR(sh.start_time) >= 15 AND HOUR(sh.start_time) < 19 THEN 'Evening'
                            WHEN HOUR(sh.start_time) >= 19 AND HOUR(sh.start_time) < 22 THEN 'Night'
                            ELSE 'Late night'
                        END AS show_category,
                        SUM(IF(ss.status = 'BOOKED', 1, 0)) AS seats_booked,
                        SUM(IF(ss.status = 'AVAILABLE', 1, 0)) AS seats_not_booked
                    FROM shows sh
                    JOIN show_seating ss ON ss.show_id = sh.show_id
                    WHERE sh.theatre_id = ?
                    AND YEAR(sh.show_date) = ?
                    AND MONTH(sh.show_date) = ?
                    GROUP BY show_category
                    ORDER BY seats_booked DESC
                """;

        List<PeakShowTimeResponse> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, theatreId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PeakShowTimeResponse r = new PeakShowTimeResponse();
                r.setShowCategory(rs.getString("show_category"));
                r.setSeatsBooked(rs.getInt("seats_booked"));
                r.setSeatsNotBooked(rs.getInt("seats_not_booked"));
                list.add(r);
            }
        }

        return list;
    }

    public List<UserBookingAnalytics> getUserBookingsAnalytics() throws SQLException {

        String query = """
                    SELECT u.user_id,
                            u.first_name,
                           COUNT(DISTINCT b.booking_id) AS no_of_bookings,
                           COUNT(bsd.show_seat_id) AS no_of_seats
                    FROM users u
                    LEFT JOIN bookings b ON b.user_id = u.user_id
                    LEFT JOIN booking_seat_details bsd ON b.booking_id = bsd.booking_id
                    WHERE b.booking_status = 'CONFIRMED'
                    GROUP BY u.user_id
                    ORDER BY no_of_seats DESC
                """;

        return getUserBookingAnalytics(query);
    }

    private static List<UserBookingAnalytics> getUserBookingAnalytics(String query) throws SQLException {
        List<UserBookingAnalytics> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                UserBookingAnalytics u = new UserBookingAnalytics();
                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setNoOfBookings(rs.getInt("no_of_bookings"));
                u.setNoOfSeats(rs.getInt("no_of_seats"));
                list.add(u);
            }
        }
        return list;
    }

    public List<TheatreBookingAnalytics> getTheatreBookingsAnalytics() throws SQLException {

        String query = """
                    SELECT t.theatre_id,t.theatre_name,
                           COUNT(DISTINCT b.booking_id) AS no_of_bookings,
                           COUNT(bsd.show_seat_id) AS no_of_seats_booked
                    FROM theatres t
                    LEFT JOIN shows sh ON t.theatre_id = sh.theatre_id
                    LEFT JOIN bookings b ON b.show_id = sh.show_id
                    LEFT JOIN booking_seat_details bsd ON b.booking_id = bsd.booking_id
                    WHERE b.booking_status = 'CONFIRMED'
                    GROUP BY t.theatre_id
                    ORDER BY no_of_seats_booked DESC
                """;

        List<TheatreBookingAnalytics> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TheatreBookingAnalytics t = new TheatreBookingAnalytics();
                t.setTheatreId(rs.getInt("theatre_id"));
                t.setTheatreName(rs.getString("theatre_name"));
                t.setNoOfBookings(rs.getInt("no_of_bookings"));
                t.setNoOfSeatsBooked(rs.getInt("no_of_seats_booked"));
                list.add(t);
            }
        }

        return list;
    }

    public List<TopSpentUser> getTopSpentUsers() throws SQLException {

        String query = """
                    SELECT u.user_id,u.first_name, SUM(b.total_amount) AS amount_spent
                    FROM users u
                    LEFT JOIN bookings b ON b.user_id = u.user_id
                    GROUP BY u.user_id
                    ORDER BY amount_spent DESC
                    LIMIT 10
                """;

        List<TopSpentUser> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TopSpentUser u = new TopSpentUser();
                u.setUserId(rs.getInt("user_id"));
                u.setFirstName(rs.getString("first_name"));
                u.setAmountSpent(rs.getDouble("amount_spent"));
                list.add(u);
            }
        }

        return list;
    }
}