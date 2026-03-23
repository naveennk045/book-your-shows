package org.nk.service;

import org.nk.dto.*;
import org.nk.repository.TheatreRepository;

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

    /*public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {
        ResultSet resultSet = theatreRepository.getTheatreById(theatreId);

        try {
            if (resultSet.next()) {
                TheatreDetails details = TheatreMapper.mapRowToTheatreDetails(resultSet);
                return Optional.of(details);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return Optional.empty();

    }

    public List<TheatreSummary> getAllTheatre(Integer limit,
                                              Integer offset,
                                              String theatreName,
                                              String city) throws SQLException {

        ResultSet resultSet = theatreRepository.getAllTheatres(limit, offset, theatreName, city);
        List<TheatreSummary> details = new ArrayList<>();

        try {

            while (resultSet.next()) {
                TheatreSummary detail = mapRowToTheatreSummary(resultSet);
                details.add(detail);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return details;
    }
*/
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

        int theatreId = theatreRepository.addTheatre(request);
        Optional<TheatreDetails> theatreDetails = this.getTheatreById(theatreId);
        if (theatreDetails.isPresent()) {
            return theatreDetails.get();
        }
        throw new SQLException("Theatre created but, not found");
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
