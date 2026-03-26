package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.show.*;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ShowRepository;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import static org.bookyourshows.utils.ShowUtils.validateModificationAllowed;

public class ShowService {

    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public ShowService() {
        this.showRepository = new ShowRepository();
        this.screenRepository = new ScreenRepository();
        this.movieRepository = new MovieRepository();
        this.theatreRepository = new TheatreRepository();
    }

    public Optional<ShowDetails> getShowById(int showId) throws SQLException {
        return this.showRepository.getShowById(showId);
    }

    public List<TheatreShowsResponse> getShows(Integer theatreId, String location, Date showDate, int movieId) throws SQLException {

        List<ShowDetails> shows = showRepository.getShows(theatreId, location, showDate, movieId);

        Map<Integer, List<ShowDetails>> grouped = new LinkedHashMap<>();

        for (ShowDetails show : shows) {
            grouped.computeIfAbsent(show.getTheatreId(), k -> new ArrayList<>()).add(show);
        }

        List<TheatreShowsResponse> response = new ArrayList<>();

        for (Map.Entry<Integer, List<ShowDetails>> entry : grouped.entrySet()) {
            TheatreShowsResponse row = new TheatreShowsResponse();
            row.setTheatreId(entry.getKey());
            row.setShows(entry.getValue());
            response.add(row);
        }

        return response;
    }

    public int createShow(ShowCreateRequest request) throws SQLException {

        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(request.getScreenId());
        if (screenDetails.isEmpty()) {
            throw new IllegalArgumentException("Screen not found");
        }
        if (!Objects.equals(request.getTheatreId(), screenDetails.get().getTheatreId())) {
            throw new RuntimeException("Theatre id mismatch");
        }
        ;

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
        System.out.println("Created show with id: " + showId);

        if (showId == null) {
            throw new IllegalArgumentException("Create show failed");
        }

        boolean isShowSeatingCreated = this.showRepository.createShowSeating(request.getScreenId(), showId);
        if (!isShowSeatingCreated) {
            this.showRepository.deleteShow(showId);
            throw new IllegalArgumentException("Create show failed.");
        }

        return showId;
    }


    public List<ShowSeatingResponse> getShowSeats(Integer showId) throws SQLException {

        if (showRepository.getShowSeats(showId).isEmpty()) {
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
