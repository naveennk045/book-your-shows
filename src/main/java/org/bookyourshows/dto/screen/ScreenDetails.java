package org.bookyourshows.dto.screen;

import java.sql.Timestamp;

public class ScreenDetails {

    private Integer screenId;
    private String screenName;
    private Integer theatreId;
    private Integer screenTypeId;
    private String ScreenTypeName;
    private Double priceMultiplier;
    private Integer totalRows;
    private Integer NoOfSeats;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Integer getScreenId() {
        return screenId;
    }

    public void setScreenId(Integer screenId) {
        this.screenId = screenId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public Integer getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Integer theatreId) {
        this.theatreId = theatreId;
    }

    public Integer getScreenTypeId() {
        return screenTypeId;
    }

    public void setScreenTypeId(Integer screenTypeId) {
        this.screenTypeId = screenTypeId;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getNoOfSeats() {
        return NoOfSeats;
    }

    public void setNoOfSeats(Integer noOfSeats) {
        NoOfSeats = noOfSeats;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getScreenTypeName() {
        return ScreenTypeName;
    }

    public void setScreenTypeName(String screenTypeName) {
        ScreenTypeName = screenTypeName;
    }

    public Double getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(Double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }
}
