package org.bookyourshows.dto.booking;

import java.util.List;
public class BookingCreateRequest {
    private int showId;
    private List<Integer> showSeatIds;
    private Double clientTotalAmount;

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public List<Integer> getShowSeatIds() {
        return showSeatIds;
    }

    public void setShowSeatIds(List<Integer> showSeatIds) {
        this.showSeatIds = showSeatIds;
    }

    public Double getClientTotalAmount() {
        return clientTotalAmount;
    }

    public void setClientTotalAmount(Double clientTotalAmount) {
        this.clientTotalAmount = clientTotalAmount;
    }
}
