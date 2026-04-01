package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.show.*;
import org.bookyourshows.repository.*;
import org.bookyourshows.repository.cache.show.ShowCacheRepository;
import org.bookyourshows.repository.cache.show.ShowSeatCacheRepository;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import static org.bookyourshows.utils.ShowUtils.validateModificationAllowed;
import static org.bookyourshows.utils.ShowUtils.validateShowCreationAllowed;

public class ShowService {

    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final ShowCacheRepository showCacheRepository;
    private final ShowSeatCacheRepository showSeatCacheRepository;

    public ShowService() {
        this.showRepository = new ShowRepository();
        this.screenRepository = new ScreenRepository();
        this.movieRepository = new MovieRepository();
        this.bookingRepository = new BookingRepository();
        this.showCacheRepository = new ShowCacheRepository();
        this.showSeatCacheRepository = new ShowSeatCacheRepository();
    }

    public Optional<ShowDetails> getShowById(int showId) throws SQLException {

        Optional<ShowDetails> showDetails = showCacheRepository.getById(showId);

        if (showDetails.isPresent()) {
            System.out.println("-- Get Show by ID : " + showId + " from cache -- ");
            return showDetails;
        }

        showDetails = showRepository.getShowById(showId);
        if (showDetails.isPresent()) {
            try {
                showCacheRepository.save(showDetails.get());
                System.out.println("-- Saved Show Details  in redis-- ");
            } catch (Exception e) {
                System.out.println("[ShowService] " + e.getMessage());
            }
        }
        return showDetails;
    }

    public List<TheatreShowsResponse> getShows(Integer theatreId, String location, Date showDate, int movieId) throws SQLException {

        List<ShowDetails> shows = showCacheRepository.search(theatreId, location, showDate, movieId);

        if (shows.isEmpty()) {
            shows = showRepository.getShows(theatreId, location, showDate, movieId);
        }

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

        validateShowCreationAllowed(request);

        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(request.getScreenId());
        if (screenDetails.isEmpty()) {
            throw new IllegalArgumentException("Screen not found");
        }
        if (!Objects.equals(request.getTheatreId(), screenDetails.get().getTheatreId())) {
            throw new RuntimeException("Theatre id mismatch");
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
        System.out.println("Created show with id: " + showId);

        if (showId == null) {
            throw new IllegalArgumentException("Create show failed");
        }

        boolean isShowSeatingCreated = this.showRepository.createShowSeating(request.getScreenId(), showId);
        if (!isShowSeatingCreated) {
            this.showRepository.deleteShow(showId);
            throw new IllegalArgumentException("Create show failed.");
        }
        Optional<ShowDetails> showDetails = showRepository.getShowById(showId);

        if (showDetails.isPresent()) {
            try {
                showCacheRepository.save(showDetails.get());
            } catch (Exception e) {
                System.out.println("[ShowService] " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Saving show is failed in redis");
        }


        Map<Integer, ShowSeating> showSeatingLayout = this.showRepository.getShowSeatsByShowId(showId);

        List<ShowSeating> showSeatingList = new ArrayList<>(showSeatingLayout.values());

        if (showSeatingList.isEmpty()) {
            throw new RuntimeException("Show seating list is empty, Update failed in redis");
        }

        this.showSeatCacheRepository.saveAll(showSeatingList, showId);
        return showId;
    }

    public List<ShowSeatingResponse> getShowSeats(Integer showId) throws SQLException {

        if (showRepository.getShowSeats(showId).isEmpty()) {
            throw new IllegalArgumentException("Show seats not found");
        }

        Map<Integer, List<ShowSeating>> map = null;

        try {
            map = this.showSeatCacheRepository.getShowSeats(showId);
        } catch (Exception e) {
            System.out.println("[ShowService] Cache miss: " + e.getMessage());
        }

        if (map == null || map.isEmpty()) {
            map = showRepository.getShowSeats(showId);
            List<ShowSeating> showSeatingList = new ArrayList<>();
            for (List<ShowSeating> seatingList : map.values()) {
                showSeatingList.addAll(seatingList);
            }
            this.showSeatCacheRepository.saveAll(showSeatingList, showId);
        }

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


        boolean isShowUpdated = showRepository.updateShowTiming(showId, request.getStartTime(), request.getEndTime());

        if (isShowUpdated) {
            showDetails = this.showRepository.getShowById(showId);
            if (showDetails.isPresent()) {
                showCacheRepository.save(showDetails.get());
            } else {
                throw new IllegalArgumentException("Update show failed,Show Not found");
            }
        }

        if (isShowUpdated) {
            this.showSeatCacheRepository.deleteAllSeatsByShowId(showId);
        }

        Map<Integer, ShowSeating> showSeatingLayout = this.showRepository.getShowSeatsByShowId(showId);

        List<ShowSeating> showSeatingList = new ArrayList<>(showSeatingLayout.values());

        if (showSeatingList.isEmpty()) {
            throw new RuntimeException("Show seating list is empty, Update failed in redis");
        }

        this.showSeatCacheRepository.saveAll(showSeatingList, showId);


        return true;
    }

    public boolean deleteShow(int showId) throws SQLException {

        ShowDetails show = showRepository.getShowById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));


        validateModificationAllowed(show);
        if (Objects.equals(show.getStatus(), "SCHEDULED") || Objects.equals(show.getStatus(), "RESCHEDULED")) {
            if (!bookingRepository.getAllBookingsByShowId(showId).isEmpty()) {
                throw new IllegalArgumentException("Show have bookings.");
            }
            ;
        }

        boolean isDeleted = showRepository.deleteShow(showId);

        if (isDeleted) {
            try {
                showCacheRepository.delete(showId);
                System.out.println("-- Created Movie by ID : " + showId + " to cache -- ");
            } catch (Exception e) {
                System.err.println("[Service] Redis sync after delete failed: " + e.getMessage());
            }
        }

        if (isDeleted) {
            this.showSeatCacheRepository.deleteAllSeatsByShowId(showId);
        }
        return true;
    }
}
