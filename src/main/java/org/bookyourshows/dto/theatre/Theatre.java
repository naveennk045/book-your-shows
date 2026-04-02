package org.bookyourshows.dto.theatre;

import com.fasterxml.jackson.annotation.JsonView;
import org.bookyourshows.dto.Views;

import java.sql.Timestamp;

public class Theatre {

    @JsonView(Views.Public.class)
    private Integer theatreId;

    @JsonView(Views.Public.class)
    private Integer ownerId;

    @JsonView(Views.Public.class)
    private String theatreName;

    @JsonView(Views.Public.class)
    private String email;

    @JsonView(Views.Public.class)
    private String contactNumber;

    @JsonView(Views.Public.class)
    private int totalScreens;

    @JsonView(Views.TheatreOwner.class)
    private String status;

    @JsonView(Views.Admin.class)
    private Timestamp registrationDate;

    @JsonView(Views.Admin.class)
    private Timestamp approvalDate;

    @JsonView(Views.TheatreOwner.class)
    private String licenseDocument;

    public Theatre() {
    }

    public Integer getTheatreId() {
        return (int) theatreId;
    }

    public void setTheatreId(Integer theatreId) {
        this.theatreId = theatreId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getTotalScreens() {
        return totalScreens;
    }

    public void setTotalScreens(int totalScreens) {
        this.totalScreens = totalScreens;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Timestamp getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Timestamp approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getLicenseDocument() {
        return licenseDocument;
    }

    public void setLicenseDocument(String licenseDocument) {
        this.licenseDocument = licenseDocument;
    }
}
