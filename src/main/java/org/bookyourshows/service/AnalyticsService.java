package org.bookyourshows.service;

import org.bookyourshows.dto.analytics.MoviePerformanceRequest;
import org.bookyourshows.dto.analytics.MoviePerformanceResponse;
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

}