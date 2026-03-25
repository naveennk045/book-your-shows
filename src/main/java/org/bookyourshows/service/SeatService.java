package org.bookyourshows.service;

import org.bookyourshows.dto.seat.SeatCreateRequest;
import org.bookyourshows.dto.seat.SeatCreateDetails;
import org.bookyourshows.dto.seat.SeatSegments;
import org.bookyourshows.repository.SeatRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService() {
        this.seatRepository = new SeatRepository();
    }


    public void createSeat(List<SeatCreateRequest> requests, int screenId) throws SQLException {


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
}
