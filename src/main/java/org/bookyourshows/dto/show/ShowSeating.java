package org.bookyourshows.dto.show;

public class ShowSeating {

    private Integer showSeatId;
    private Integer seatId;
    private String seatNumber;
    private String category;
    private String status;
    private Double finalPrice;

    public Integer getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Integer showSeatId) {
        this.showSeatId = showSeatId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
