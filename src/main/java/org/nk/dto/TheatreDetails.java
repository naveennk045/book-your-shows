package org.nk.dto;


public class TheatreDetails {

    private Theatre theatre;
    private TheatreAddress address;

    public TheatreDetails() {
    }

    public TheatreDetails(Theatre theatre, TheatreAddress address) {
        this.theatre = theatre;
        this.address = address;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public TheatreAddress getAddress() {
        return address;
    }

    public void setAddress(TheatreAddress address) {
        this.address = address;
    }
}
