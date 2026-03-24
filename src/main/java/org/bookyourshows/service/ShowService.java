package org.bookyourshows.service;

import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.show.ShowSummary;
import org.bookyourshows.dto.show.ShowUpdateRequest;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ShowRepository;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.ShowUtils.validateModificationAllowed;

public class ShowService {

    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;

    public ShowService() {
        this.showRepository = new ShowRepository();
        this.screenRepository = new ScreenRepository();
        this.movieRepository = new MovieRepository();
    }

    public List<ShowSummary> getShows(int theatreId, Date showDate) throws SQLException {

        if (theatreId <= 0) {
            throw new IllegalArgumentException("Invalid theatre_id");
        }

        return showRepository.getShows(theatreId, showDate);
    }

    public int createShow(ShowCreateRequest req) throws SQLException {

        if (screenRepository.getScreenById(req.getScreenId()).isEmpty()) {
            throw new IllegalArgumentException("Screen not found");
        }

        if (movieRepository.getMovieById(req.getMovieId()).isEmpty()) {
            throw new IllegalArgumentException("Movie not found");
        }

        if (req.getStartTime().after(req.getEndTime())) {
            throw new IllegalArgumentException("Invalid timing");
        }

        if (showRepository.isShowConflict(
                req.getScreenId(),
                req.getShowDate(),
                req.getStartTime(),
                req.getEndTime()
        )) {
            throw new IllegalArgumentException("Show timing conflicts with existing show");
        }

        return showRepository.createShow(req);
    }

    public boolean updateShow(int showId, ShowUpdateRequest req) throws SQLException {

        Optional<ShowDetails> showDetails = showRepository.getShowById(showId);
        if (showDetails.isEmpty()) {
            throw new IllegalArgumentException("Show not found");
        }

        ShowDetails show = showDetails.get();

        validateModificationAllowed(show);

        if (req.getStartTime().after(req.getEndTime())) {
            throw new IllegalArgumentException("Invalid timing");
        }

        if (showRepository.isShowConflict(
                show.getScreenId(),
                show.getShowDate(),
                req.getStartTime(),
                req.getEndTime()
        )) {
            throw new IllegalArgumentException("Timing conflict");
        }

        return showRepository.updateShowTiming(showId, req.getStartTime(), req.getEndTime());
    }

    public boolean deleteShow(int showId) throws SQLException {

        ShowDetails show = showRepository.getShowById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));

        validateModificationAllowed(show);

        return showRepository.deleteShow(showId);
    }


}
