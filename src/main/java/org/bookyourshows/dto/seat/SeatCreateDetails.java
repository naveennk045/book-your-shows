package org.bookyourshows.dto.seat;

public class SeatCreateDetails {

    private Integer screenId;
    private Integer seatCategoryId;
    private Integer rowNo;
    private String seatNumber;


    public Integer getScreenId() {
        return screenId;
    }

    public void setScreenId(Integer screenId) {
        this.screenId = screenId;
    }

    public Integer getSeatCategoryId() {
        return seatCategoryId;
    }

    public void setSeatCategoryId(Integer seatCategoryId) {
        this.seatCategoryId = seatCategoryId;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
