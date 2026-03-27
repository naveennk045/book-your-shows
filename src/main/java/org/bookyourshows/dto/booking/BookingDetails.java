package org.bookyourshows.dto.booking;

import java.util.List;

public class BookingDetails {

    private BookingSummary booking;
    private BookingShowInfo show;
    private List<BookingSeatInfo> seats;
    private List<BookingPaymentInfo> payments;

    public BookingSummary getBooking() {
        return booking;
    }

    public void setBooking(BookingSummary booking) {
        this.booking = booking;
    }

    public BookingShowInfo getShow() {
        return show;
    }

    public void setShow(BookingShowInfo show) {
        this.show = show;
    }

    public List<BookingSeatInfo> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeatInfo> seats) {
        this.seats = seats;
    }

    public List<BookingPaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<BookingPaymentInfo> payments) {
        this.payments = payments;
    }
}
