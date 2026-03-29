package org.bookyourshows.dto.booking;

public class BookingInfo {
    private final int bookingId;
    private final int userId;
    private final int theatreId;

    public BookingInfo(int bookingId, int userId, int theatreId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.theatreId = theatreId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public int getTheatreId() {
        return theatreId;
    }
}
