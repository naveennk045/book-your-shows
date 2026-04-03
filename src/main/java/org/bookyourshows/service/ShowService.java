package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.show.*;
import org.bookyourshows.dto.theatre.Theatre;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.*;
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
    private final ShowCacheRepository showCacheRepository;
    private final ShowSeatCacheRepository showSeatCacheRepository;
    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;

    public ShowService() {
        this.showRepository = new ShowRepository();
        this.screenRepository = new ScreenRepository();
        this.movieRepository = new MovieRepository();
        this.showCacheRepository = new ShowCacheRepository();
        this.showSeatCacheRepository = new ShowSeatCacheRepository();
        this.theatreRepository = new TheatreRepository();
        this.seatRepository = new SeatRepository();
    }

    public Optional<ShowDetails> getShowById(int showId) throws SQLException, ResourceNotFoundException {

        Optional<ShowDetails> showDetails = showCacheRepository.getById(showId);

        if (showDetails.isPresent()) {
            return showDetails;
        }
        showDetails = showRepository.getShowById(showId);
        if (showDetails.isPresent()) {
            showCacheRepository.save(showDetails.get());
            return showDetails;

        }

        throw new ResourceNotFoundException("Show not found.");

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

    public int createShow(ShowCreateRequest request, UserContext userContext) throws SQLException, CustomException {

        validateShowCreationAllowed(request);
        System.out.println("Creating show....");
        hasAccessToShows(request.getTheatreId(), userContext);


        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(request.getScreenId());
        if (screenDetails.isEmpty()) {
            throw new ResourceNotFoundException("Screen not found");
        }
        if (!Objects.equals(request.getTheatreId(), screenDetails.get().getTheatreId())) {
            throw new ResourceConflictException("Theatre id mismatch");
        }

        if (movieRepository.getMovieById(request.getMovieId()).isEmpty()) {
            throw new ResourceNotFoundException("Movie not found");
        }

        if (request.getStartTime().after(request.getEndTime())) {
            throw new ResourceConflictException("Invalid timing");
        }


        if (showRepository.isShowConflict(
                request.getScreenId(),
                request.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new ResourceConflictException("Show timing conflicts with existing show");
        }

        Integer showId = showRepository.createShow(request);
        System.out.println("Created show with id: " + showId);

        if (showId == null) {
            throw new ActionFailedException("Create show failed");
        }

        boolean isScreenHaveSeats = !this.seatRepository.getSeatByScreenId(request.getScreenId()).isEmpty();
        if (!isScreenHaveSeats) {
            throw new ResourceNotFoundException("Seats not found, Pls create seats.");
        }

        boolean isShowSeatingCreated = this.showRepository.createShowSeating(request.getScreenId(), showId);
        if (!isShowSeatingCreated) {
            this.showRepository.deleteShow(showId);
            throw new ActionFailedException("Create show failed.");
        }
        Optional<ShowDetails> showDetails = showRepository.getShowById(showId);
        showCacheRepository.save(showDetails.get());

        Map<Integer, ShowSeating> showSeatingLayout = this.showRepository.getShowSeatsByShowId(showId);
        List<ShowSeating> showSeatingList = new ArrayList<>(showSeatingLayout.values());
        this.showSeatCacheRepository.saveAll(showSeatingList, showId);
        return showId;
    }

    public List<ShowSeatingResponse> getShowSeats(Integer showId) throws SQLException, ShowCreationException {

        if (showRepository.getShowSeats(showId).isEmpty()) {
            throw new ShowCreationException("Show seats not found");
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

    public boolean updateShow(int showId, ShowUpdateRequest request, UserContext userContext) throws SQLException, CustomException {

        Optional<ShowDetails> showDetails = showRepository.getShowById(showId);
        if (showDetails.isEmpty()) {
            throw new ResourceNotFoundException("Show not found");
        }

        hasAccessToShows(showDetails.get().getTheatreId(), userContext);


        ShowDetails show = showDetails.get();

        validateModificationAllowed(show);

        if (request.getStartTime().after(request.getEndTime())) {
            throw new ResourceConflictException("Invalid timing");
        }

        if (showRepository.isShowConflict(
                show.getScreenId(),
                show.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new ShowCreationException("Timing conflict");
        }


        boolean isShowUpdated = showRepository.updateShowTiming(showId, request.getStartTime(), request.getEndTime());

        if (isShowUpdated) {
            showDetails = this.showRepository.getShowById(showId);
            showDetails.ifPresent(showCacheRepository::save);
        }

        if (isShowUpdated) {
            this.showSeatCacheRepository.deleteAllSeatsByShowId(showId);
        }

        Map<Integer, ShowSeating> showSeatingLayout = this.showRepository.getShowSeatsByShowId(showId);
        List<ShowSeating> showSeatingList = new ArrayList<>(showSeatingLayout.values());
        this.showSeatCacheRepository.saveAll(showSeatingList, showId);


        return true;
    }

    private void hasAccessToShows(Integer theatreId, UserContext userContext) throws CustomException, SQLException {

        Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(theatreId);

        if (theatreDetails.isEmpty()) {
            throw new ResourceNotFoundException("No theatre found.");
        }

        Theatre theatre = theatreDetails.get().getTheatre();

        boolean isAdmin = "ADMIN".equals(userContext.getUserRole());
        boolean isOwner = theatre.getOwnerId().equals(userContext.getUserId());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("Access denied.");
        }
    }
/*
    public boolean deleteShow(int showId) throws SQLException, CustomException {

        ShowDetails show = showRepository.getShowById(showId)
                .orElseThrow(() -> new ShowCreationException("Show not found"));


        validateModificationAllowed(show);
        if (Objects.equals(show.getStatus(), "SCHEDULED") || Objects.equals(show.getStatus(), "RESCHEDULED")) {
            if (!bookingRepository.getAllBookingsByShowId(showId).isEmpty()) {
                throw new ShowCreationException("Show have bookings.");
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
    }*/


}
