package org.bookyourshows.service;

import org.bookyourshows.dto.booking.*;
import org.bookyourshows.dto.show.ShowSeating;
import org.bookyourshows.repository.BookingRepository;
import org.bookyourshows.repository.ShowRepository;

import java.sql.SQLException;
import java.util.*;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;

    public BookingService() {
        this.bookingRepository = new BookingRepository();
        this.showRepository = new ShowRepository();
    }

    public Optional<BookingDetails> getBookingById(int bookingId) throws SQLException {
        return bookingRepository.getBookingById(bookingId);
    }

    public int createBooking(int userId, BookingCreateRequest request) throws SQLException {
        if (request.getShowId() <= 0) {
            throw new IllegalArgumentException("show_id is required");
        }
        if (request.getShowSeatIds() == null || request.getShowSeatIds().isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }


        Map<Integer, ShowSeating> showSeating = showRepository.getShowSeatsByShowId(request.getShowId());
        double totalAmount = 0;


        for (Integer showSeatId : request.getShowSeatIds()) {
            totalAmount += showSeating.get(showSeatId).getFinalPrice();
        }


        if (request.getClientTotalAmount() == null) {
            throw new IllegalArgumentException("total amount is required");
        } else if(totalAmount != request.getClientTotalAmount()) {
            throw new IllegalArgumentException("calculated total amount is not equal to the requested total amount");
        }

        return bookingRepository.createBookingWithSeats(userId, request, totalAmount);
    }
}
