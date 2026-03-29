package org.bookyourshows.service;

import org.bookyourshows.dto.analytics.*;
import org.bookyourshows.repository.AnalyticsRepository;

import java.sql.SQLException;
import java.util.List;

public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService() {
        this.analyticsRepository = new AnalyticsRepository();
    }

    public List<MoviePerformanceResponse> getMoviePerformance(MoviePerformanceRequest request) throws SQLException {

        if (request.getLimit() == null || request.getLimit() <= 0) {
            request.setLimit(10);
        } else if (request.getLimit() > 100) {
            request.setLimit(100);
        }

        return analyticsRepository.getMoviePerformance(request);
    }
    public List<PeakShowTimeResponse> getPeakShowTimes(Integer theatreId, Integer year, Integer month) throws SQLException {
        return analyticsRepository.getPeakShowTimes(theatreId, year, month);
    }

    public List<UserBookingAnalytics> getUserBookingsAnalytics() throws SQLException {
        return analyticsRepository.getUserBookingsAnalytics();
    }

    public List<TheatreBookingAnalytics> getTheatreBookingsAnalytics() throws SQLException {
        return analyticsRepository.getTheatreBookingsAnalytics();
    }

    public List<TopSpentUser> getTopSpentUsers() throws SQLException {
        return analyticsRepository.getTopSpentUsers();
    }

}