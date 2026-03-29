package org.bookyourshows.dto.analytics;

public class PeakShowTimeResponse {
    private String showCategory;
    private int seatsBooked;
    private int seatsNotBooked;

    public String getShowCategory() {
        return showCategory;
    }

    public void setShowCategory(String showCategory) {
        this.showCategory = showCategory;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public int getSeatsNotBooked() {
        return seatsNotBooked;
    }

    public void setSeatsNotBooked(int seatsNotBooked) {
        this.seatsNotBooked = seatsNotBooked;
    }
}
