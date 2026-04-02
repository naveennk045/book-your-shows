package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.seat.*;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.*;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.SeatRepository;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;
import java.util.*;

import static org.bookyourshows.utils.SeatUtils.validateSeatCreateRequests;

public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public SeatService() {
        this.seatRepository = new SeatRepository();
        this.screenRepository = new ScreenRepository();
        this.theatreRepository = new TheatreRepository();
    }

    public List<SeatRowResponse> getSeatsByScreenId(int screenId, int theatreId) throws SQLException {

        Map<Integer, List<SeatSummary>> map = seatRepository.getSeatByScreenId(screenId);

        List<SeatRowResponse> response = new ArrayList<>();

        for (Map.Entry<Integer, List<SeatSummary>> entry : map.entrySet()) {

            SeatRowResponse row = new SeatRowResponse();
            row.setRowNo(entry.getKey());
            row.setSeats(entry.getValue());

            response.add(row);
        }

        return response;
    }

    public void createSeat(List<SeatCreateRequest> requests, Integer screenId, Integer theatreId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToScreen(theatreId, userContext);
        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(screenId);

        if (screenDetails.isEmpty()) {
            throw new ResourceNotFoundException("Screen with id " + screenId + " does not exist");
        }
        if (!Objects.equals(screenDetails.get().getTheatreId(), theatreId)) {
            throw new ResourceConflictException("Screen not belongs to the theatre");
        }
        validateSeatCreateRequests(requests);

        Map<Integer, Integer> rowSeatCountMap = seatRepository.getMaxSeatNumberByScreen(screenId);
        List<SeatCreateDetails> seats = new ArrayList<>();

        for (SeatCreateRequest request : requests) {

            int rowNo = request.getRowNo();
            String rowLabel = generateRowLabel(rowNo);

            int count = rowSeatCountMap.getOrDefault(rowNo, 0);

            for (SeatSegments segment : request.getSegments()) {
                for (int i = 0; i < segment.getSeatCount(); i++) {

                    SeatCreateDetails seat = new SeatCreateDetails();
                    seat.setRowNo(rowNo);
                    seat.setScreenId(screenId);
                    seat.setSeatCategoryId(segment.getSeatCategoryId());
                    seat.setSeatNumber(rowLabel + (++count));

                    seats.add(seat);
                }
            }
            rowSeatCountMap.put(rowNo, count);
        }

        this.seatRepository.addAllSeats(seats);
    }

    public String generateRowLabel(int rowNumber) {
        StringBuilder sb = new StringBuilder();

        while (rowNumber > 0) {
            rowNumber--;
            sb.append((char) ('A' + (rowNumber % 26)));
            rowNumber /= 26;
        }
        return sb.reverse().toString();
    }

    public boolean updateSeat(int seatId, SeatUpdateRequest seatUpdateRequest, UserContext userContext) throws SQLException, CustomException {

        Optional<SeatSummary> seatDetails = this.seatRepository.getSeatById(seatId);
        if (seatDetails.isEmpty()) {
            throw new ResourceNotFoundException("Seat with id " + seatId + " does not exist");
        }

        Integer screenId = seatDetails.get().getScreenId();
        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(screenId);

        if (screenDetails.isEmpty()) {
            throw new ResourceNotFoundException("Screen with id " + screenId + " does not exist");
        }

        Integer theatreId = screenDetails.get().getTheatreId();

        hasAccessToScreen(theatreId, userContext);

        return this.seatRepository.updateSeat(seatId, seatUpdateRequest);


    }

/*    public boolean deleteSeat(int seatId, UserContext userContext) throws SQLException, CustomException {

        Optional<SeatSummary> seatDetails = this.seatRepository.getSeatById(seatId);
        if (seatDetails.isEmpty()) {
            throw new ResourceNotFoundException("Seat with id " + seatId + " does not exist");
        }

        Integer screenId = seatDetails.get().getScreenId();
        Optional<ScreenDetails> screenDetails = screenRepository.getScreenByScreenId(screenId);

        if (screenDetails.isEmpty()) {
            throw new ResourceNotFoundException("Screen with id " + screenId + " does not exist");
        }

        Integer theatreId = screenDetails.get().getTheatreId();
        hasAccessToScreen(theatreId, userContext);

        return this.seatRepository.deleteSeat(seatId);
    }*/

    private void hasAccessToScreen(Integer theatreId, UserContext userContext) throws SQLException, CustomException {

        Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(theatreId);
        if (!userContext.getUserRole().equals("ADMIN") && theatreDetails.isPresent() &&
                !Objects.equals(theatreDetails.get().getTheatre().getOwnerId(), userContext.getUserId())) {
            throw new ForbiddenException("Access denied");
        }
    }

}
