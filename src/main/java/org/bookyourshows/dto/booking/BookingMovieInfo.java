package org.bookyourshows.dto.booking;

public class BookingMovieInfo {
    private final int bookingId;
    private final int userId;
    private final int movieId;

    public BookingMovieInfo(int bookingId, int userId, int movieId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.movieId = movieId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMovieId() {
        return movieId;
    }
}
