package org.nk.service;

import org.nk.dto.TheatreDetails;
import org.nk.dto.TheatreSummary;
import org.nk.dto.TheatreCreateRequest;
import org.nk.dto.TheatreUpdateRequest;
import org.nk.repository.TheatreRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService() {
        this.theatreRepository = new TheatreRepository();
    }

    public Optional<TheatreDetails> findTheatreById(int theatreId) {
        return theatreRepository.findTheatreById(theatreId);
    }

    public List<TheatreSummary> findAllTheatre(Integer limit,
                                               Integer offset,
                                               String theatreName,
                                               String city) {
        return theatreRepository.findAllTheatres(limit, offset, theatreName, city);
    }

    public TheatreDetails createTheatre(TheatreCreateRequest request) throws SQLException {


        if (request.getTheatreName() == null || request.getTheatreName().isBlank()) {
            throw new IllegalArgumentException("theatreName is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (request.getContactNumber() == null || request.getContactNumber().isBlank()) {
            throw new IllegalArgumentException("contactNumber is required");
        }
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
        request.setOwnerId(9);
        request.setState("APPROVED");

        return theatreRepository.addTheatre(request);
    }

    public boolean updateTheatre(int theatreId,
                                 TheatreUpdateRequest request) throws SQLException {

        if (request.getTheatreName() == null || request.getTheatreName().isBlank()) {
            throw new IllegalArgumentException("theatreName is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (request.getContactNumber() == null || request.getContactNumber().isBlank()) {
            throw new IllegalArgumentException("contactNumber is required");
        }
        if (request.getTotalScreens() <= 0) {
            throw new IllegalArgumentException("totalScreens must be greater  0");
        }
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("addressLine1 is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("city is required");
        }

        return theatreRepository.updateTheatreWithAddress(theatreId, request);
    }

    public boolean deleteTheatre(int theatreId) throws SQLException {
        return theatreRepository.deleteTheatre(theatreId);
    }
}
