package org.bookyourshows.dto.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.bookyourshows.dto.Views;

import java.sql.Date;
import java.sql.Timestamp;

public class UserDetails {

    @JsonView(Views.Public.class)
    private Integer userId;
    @JsonView(Views.Public.class)
    private String firstName;
    @JsonView(Views.Public.class)
    private String lastName;
    @JsonView(Views.Public.class)
    private String mobileNumber;
    @JsonView(Views.Public.class)
    private String email;
    @JsonView(Views.Public.class)
    private String userRole;
    @JsonView(Views.Public.class)
    private Date dateOfBirth;
    @JsonView(Views.Public.class)
    private String profilePictureUrl;
    @JsonView(Views.Admin.class)
    private String accountStatus;
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

    @JsonView(Views.Admin.class)
    private Timestamp lastLogin;
    @JsonView(Views.Admin.class)
    private Timestamp createdAt;
    @JsonView(Views.Admin.class)
    private Timestamp updatedAt;

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
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
