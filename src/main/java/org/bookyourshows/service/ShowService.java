package org.bookyourshows.service;

import org.bookyourshows.dto.show.*;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ShowRepository;

import java.sql.*;
import java.sql.Date;
import java.util.*;

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

    public Optional<ShowDetails> getShowById(int showId) throws SQLException {
        return this.showRepository.getShowById(showId);
    }

    public List<ShowSummary> getShows(int theatreId, Date showDate) throws SQLException {

        if (theatreId <= 0) {
            throw new IllegalArgumentException("Invalid theatre_id");
        }

        return showRepository.getShows(theatreId, showDate);
    }

    public int createShow(ShowCreateRequest request) throws SQLException {

        if (screenRepository.getScreenByScreenId(request.getScreenId()).isEmpty()) {
            throw new IllegalArgumentException("Screen not found");
        }

        if (movieRepository.getMovieById(request.getMovieId()).isEmpty()) {
            throw new IllegalArgumentException("Movie not found");
        }

        if (request.getStartTime().after(request.getEndTime())) {
            throw new IllegalArgumentException("Invalid timing");
        }

        if (showRepository.isShowConflict(
                request.getScreenId(),
                request.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new IllegalArgumentException("Show timing conflicts with existing show");
        }

        Integer showId = showRepository.createShow(request);

        if (showId == null) {
            throw new IllegalArgumentException("Create show failed");
        }

        boolean isShowSeatingCreated = this.showRepository.createShowSeating(request.getScreenId(), showId);
        if (!isShowSeatingCreated) {
            this.showRepository.deleteShow(showId);
            throw new IllegalArgumentException("Create show failed");
        }

        return showId;
    }


    public List<ShowSeatingResponse> getShowSeats(Integer showId) throws SQLException {

        if(showRepository.getShowSeats(showId).isEmpty()) {
            throw new IllegalArgumentException("Show seats not found");
        }

        Map<Integer, List<ShowSeating>> map = showRepository.getShowSeats(showId);

        List<ShowSeatingResponse> response = new ArrayList<>();

        for (Map.Entry<Integer, List<ShowSeating>> entry : map.entrySet()) {

            ShowSeatingResponse row = new ShowSeatingResponse();
            row.setRowNo(entry.getKey());
            row.setSeats(entry.getValue());

            response.add(row);
        }
        return response;
    }

    public boolean updateShow(int showId, ShowUpdateRequest request) throws SQLException {

        Optional<ShowDetails> showDetails = showRepository.getShowById(showId);
        if (showDetails.isEmpty()) {
            throw new IllegalArgumentException("Show not found");
        }

        ShowDetails show = showDetails.get();

        validateModificationAllowed(show);

        if (request.getStartTime().after(request.getEndTime())) {
            throw new IllegalArgumentException("Invalid timing");
        }

        if (showRepository.isShowConflict(
                show.getScreenId(),
                show.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new IllegalArgumentException("Timing conflict");
        }

        return showRepository.updateShowTiming(showId, request.getStartTime(), request.getEndTime());
    }

    public boolean deleteShow(int showId) throws SQLException {

        ShowDetails show = showRepository.getShowById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));

        validateModificationAllowed(show);

        return showRepository.deleteShow(showId);
    }
}
