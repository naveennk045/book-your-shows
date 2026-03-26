package org.bookyourshows.dto.show;


import java.util.List;

public class ShowSeatingResponse {
    private int rowNo;
    private List<ShowSeating> seats;

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public List<ShowSeating> getSeats() {
        return seats;
    }

    public void setSeats(List<ShowSeating> seats) {
        this.seats = seats;
    }
}