package org.bookyourshows.dto.analytics;

public class TheatreRevenueAnalytics {
    private Integer theatreId;
    private String theatreName;
    private Double revenue;

    public Integer getTheatreId() { return theatreId; }
    public void setTheatreId(Integer theatreId) { this.theatreId = theatreId; }

    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
}