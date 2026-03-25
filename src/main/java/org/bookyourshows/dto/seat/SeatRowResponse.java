package org.bookyourshows.dto.seat;

import java.util.List;

public class SeatRowResponse {
    private int rowNo;
    private List<SeatSummary> seats;

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public List<SeatSummary> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatSummary> seats) {
        this.seats = seats;
    }
}
