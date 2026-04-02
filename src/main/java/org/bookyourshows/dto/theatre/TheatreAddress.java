package org.bookyourshows.dto.theatre;

import com.fasterxml.jackson.annotation.JsonView;
import org.bookyourshows.dto.Views;

import java.sql.Timestamp;

public class TheatreAddress {

    @JsonView(Views.Public.class)
    private Integer addressId;
    @JsonView(Views.Public.class)
    private Integer theatreId;

    @JsonView(Views.Public.class)
    private String addressLine1;

    @JsonView(Views.Public.class)
    private String addressLine2;

    @JsonView(Views.Public.class)
    private String city;

    @JsonView(Views.Public.class)
    private String state;

    @JsonView(Views.Public.class)
    private String country;

    @JsonView(Views.Public.class)
    private String pincode;

    @JsonView(Views.Public.class)
    private double latitude;

    @JsonView(Views.Public.class)
    private double longitude;

    @JsonView(Views.Admin.class)
    private Timestamp createdAt;

    @JsonView(Views.Admin.class)
    private Timestamp updatedAt;

    public TheatreAddress() {
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public Integer getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Integer theatreId) {
        this.theatreId = theatreId;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
}
