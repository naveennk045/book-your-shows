package org.bookyourshows.utils;

import org.bookyourshows.dto.seat.SeatCreateRequest;
import org.bookyourshows.dto.seat.SeatSegments;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.CreationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatUtils {

    public static void validateSeatCreateRequests(List<SeatCreateRequest> requests) throws CustomException {

        if (requests == null || requests.isEmpty()) {
            throw new CreationException("Seat request list cannot be empty");
        }

        Set<Integer> seenRows = new HashSet<>();

        for (SeatCreateRequest request : requests) {

            if (request.getRowNo() == null) {
                throw new CreationException("row_no is required");
            }
            if (request.getRowNo() <= 0) {
                throw new CreationException("row_no must be greater than 0");
            }
            if (!seenRows.add(request.getRowNo())) {
                throw new CreationException("Duplicate row_no: " + request.getRowNo());
            }

            int totalSegmentsInRow = getTotalSeatsInRow(request);

            if (totalSegmentsInRow > 100) {
                throw new CreationException("Total seats in row " + request.getRowNo() + " cannot exceed 100");
            }
        }
    }

    private static int getTotalSeatsInRow(SeatCreateRequest request) throws CustomException {
        if (request.getSegments() == null || request.getSegments().isEmpty()) {
            throw new CreationException("segments cannot be empty for row " + request.getRowNo());
        }

        Set<Integer> seenCategories = new HashSet<>();
        int totalSeatsInRow = 0;

        for (SeatSegments segment : request.getSegments()) {

            if (segment.getSeatCategoryId() == null) {
                throw new CreationException("seat_category_id is required in row " + request.getRowNo());
            }
            if (segment.getSeatCount() == null) {
                throw new CreationException("seat_count is required in row " + request.getRowNo());
            }
            if (segment.getSeatCount() <= 0) {
                throw new CreationException("seat_count must be greater than 0 in row " + request.getRowNo());
            }
            if (segment.getSeatCount() > 50) {
                throw new CreationException("seat_count cannot exceed 50 in row " + request.getRowNo());
            }
            if (!seenCategories.add(segment.getSeatCategoryId())) {
                throw new CreationException("Duplicate seat_category_id " + segment.getSeatCategoryId() + " in row " + request.getRowNo());
            }

            totalSeatsInRow += segment.getSeatCount();
        }
        return totalSeatsInRow;
    }
}
