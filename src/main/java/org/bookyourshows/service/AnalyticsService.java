package org.bookyourshows.service;

import org.bookyourshows.dto.analytics.*;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ForbiddenException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.repository.AnalyticsRepository;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final TheatreRepository theatreRepository;

    public AnalyticsService() {
        this.analyticsRepository = new AnalyticsRepository();
        this.theatreRepository = new TheatreRepository();
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

    public List<UserBookingAnalytics> getUserBookingsAnalytics(Integer year, Integer month) throws SQLException {
        return analyticsRepository.getUserBookingsAnalytics(year, month);
    }

    public List<TheatreBookingAnalytics> getTheatreBookingsAnalytics(UserContext userContext, Integer year, Integer month) throws SQLException, CustomException {
        if (userContext.getUserRole().equals("ADMIN")) {
            return analyticsRepository.getTheatreBookingsAnalytics(year, month);
        }
        if (userContext.getUserRole().equals("THEATRE_OWNER")) {
            Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreByOwnerId(userContext.getUserId());
            if (theatreDetails.isEmpty()) {
                throw new ResourceNotFoundException("No theatre found");
            }
            return analyticsRepository.getTheatreBookingsAnalyticsByTheatre(
                    theatreDetails.get().getTheatre().getTheatreId(), year, month);
        } else {
            throw new ForbiddenException("Access denied");
        }
    }

    public List<TopSpentUser> getTopSpentUsers(Integer year, Integer month) throws SQLException {
        return analyticsRepository.getTopSpentUsers(year, month);
    }
}