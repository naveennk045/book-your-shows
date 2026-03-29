package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.analytics.MoviePerformanceRequest;
import org.bookyourshows.dto.analytics.MoviePerformanceResponse;
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
}