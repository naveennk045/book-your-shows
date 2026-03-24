package org.bookyourshows.dto.screen;

public class ScreenType {
    private Integer screenTypeId;
    private String screenTypeName;
    private double priceMultiplier;
    private String screenTypeDescription;

    public Integer getScreenTypeId() {
        return screenTypeId;
    }

    public void setScreenTypeId(Integer screenTypeId) {
        this.screenTypeId = screenTypeId;
    }

    public String getScreenTypeName() {
        return screenTypeName;
    }

    public void setScreenTypeName(String screenTypeName) {
        this.screenTypeName = screenTypeName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public String getScreenTypeDescription() {
        return screenTypeDescription;
    }

    public void setScreenTypeDescription(String screenTypeDescription) {
        this.screenTypeDescription = screenTypeDescription;
    }
}
