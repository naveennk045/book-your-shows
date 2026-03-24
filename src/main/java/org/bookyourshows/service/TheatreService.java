package org.bookyourshows.service;

import org.bookyourshows.dto.*;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService() {
        this.theatreRepository = new TheatreRepository();
    }

    public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {
        return theatreRepository.getTheatreById(theatreId);
    }

    public List<TheatreSummary> getAllTheatre(Integer limit,
                                              Integer offset,
                                              String theatreName,
                                              String city) throws SQLException {
        return theatreRepository.getAllTheatres(limit, offset, theatreName, city);
    }


    public TheatreDetails createTheatre(TheatreCreateRequest request) throws SQLException {


        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());

        if (request.getTotalScreens() <= 0) {
            throw new IllegalArgumentException("totalScreens must be greater than 0");
        }
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("addressLine1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("city is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new IllegalArgumentException("state is required");
        }
        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new IllegalArgumentException("pincode is required");
        }

        /*TODO
            Owner validation
            - Role verification
            - Whether User is active.
         */

        if (theatreRepository.getTheatreByOwnerId(request.getOwnerId()).isPresent()) {
            throw new RuntimeException("There is already an existing theatre,One user can able to create one theatre");
        }
        request.setState("APPROVED");
        int theatreId = theatreRepository.addTheatre(request);
        boolean isTheatreAddressAdded = theatreRepository.addTheatreAddress(request, theatreId);
        if (!isTheatreAddressAdded) {
            throw new RuntimeException("Theatre created successfully, but address creation failed. Please update the address.");
        }
        Optional<TheatreDetails> theatreDetails = this.getTheatreById(theatreId);
        if (theatreDetails.isPresent()) {
            return theatreDetails.get();
        }
        throw new RuntimeException("Theatre created but, not found");
    }


    public boolean updateTheatre(int theatreId,
                                 TheatreUpdateRequest request) throws SQLException {

        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());

        if (request.getTotalScreens() <= 0) {
            throw new IllegalArgumentException("totalScreens must be greater than 0");
        }
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("addressLine1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("city is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new IllegalArgumentException("state is required");
        }
        if (request.getPincode() == null || request.getPincode().isBlank()) {
            throw new IllegalArgumentException("pincode is required");
        }


        boolean isTheatreUpdated = theatreRepository.updateTheatre(theatreId, request);
        if (!isTheatreUpdated) {
            throw new RuntimeException("Failed to update the theatre");
        }
        boolean isTheatreAddressUpdated = theatreRepository.updateTheatreAddress(theatreId, request);
        if (!isTheatreAddressUpdated) {
            throw new RuntimeException("Theatre is Updated, Failed to update the theatre address.");
        }
        return true;
    }

    public boolean deleteTheatre(int theatreId) throws SQLException {
        /*
         * TODO :
         *   Is there any existing show under this theatre.
         * */
        return theatreRepository.deleteTheatre(theatreId);
    }


    private void validateTheatreName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("theatreName is required");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException("theatreName too long");
        }

        if (name.matches("\\d+")) {
            throw new IllegalArgumentException("theatreName cannot be only numbers");
        }

        if (!name.matches("^[a-zA-Z0-9\\s&().-]+$")) {
            throw new IllegalArgumentException("Invalid characters in theatreName");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(regex)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validateContactNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("contactNumber is required");
        }

        number = number.trim();

        if (!number.matches("^(\\+91)?[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Invalid contact number");
        }
    }


}
