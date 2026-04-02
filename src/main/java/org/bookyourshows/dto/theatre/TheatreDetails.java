package org.bookyourshows.dto.theatre;


import com.fasterxml.jackson.annotation.JsonView;
import org.bookyourshows.dto.Views;

public class TheatreDetails {

    @JsonView(Views.Public.class)
    private Theatre theatre;

    @JsonView(Views.Public.class)
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
