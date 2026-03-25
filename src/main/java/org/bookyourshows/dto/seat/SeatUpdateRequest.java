package org.bookyourshows.dto.seat;

public class SeatUpdateRequest {

    private Integer seatCategoryId;
    private String status;


    public Integer getSeatCategoryId() {
        return seatCategoryId;
    }

    public void setSeatCategoryId(Integer seatCategoryId) {
        this.seatCategoryId = seatCategoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
