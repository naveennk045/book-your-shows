package org.bookyourshows.service;

import org.bookyourshows.dto.address.AddressDTO;
import org.bookyourshows.dto.theatre.TheatreCreateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.dto.theatre.TheatreUpdateRequest;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ScreenTypeRepository;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.TheatreUtils.*;

public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    public TheatreService() {

        this.theatreRepository = new TheatreRepository();
        this.screenRepository = new ScreenRepository();
    }

    public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {
        return theatreRepository.getTheatreById(theatreId);
    }

    public Optional<TheatreDetails> getTheatreByOwnerId(Integer ownerId) throws SQLException {
        return theatreRepository.getTheatreByOwnerId(ownerId);
    }

    public List<TheatreSummary> getAllTheatre(Integer limit,
                                              Integer offset,
                                              String theatreName,
                                              String city,
                                              String status
    ) throws SQLException {
        return theatreRepository.getAllTheatres(limit, offset, theatreName, city, status);
    }

    public Optional<AddressDTO> getTheatreAddress(int theatreId) throws SQLException {
        return theatreRepository.getTheatreAddress(theatreId);
    }

    public boolean updateTheatreAddress(int theatreId, AddressDTO req) throws SQLException {

        if (req.getAddressLine1() == null || req.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("address_line1 is required");
        }

        if (req.getCity() == null || req.getCity().isBlank()) {
            throw new IllegalArgumentException("city is required");
        }

        if (req.getLatitude() == null || req.getLongitude() == null) {
            throw new IllegalArgumentException("latitude and longitude are required");
        }

        boolean updated = theatreRepository.updateTheatreAddress(theatreId, req);

        if (!updated) {
            throw new IllegalArgumentException("Theatre address not found");
        }

        return true;
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
//        request.setState("APPROVED");
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
            throw new IllegalArgumentException("Theatre not found");
        }
        boolean isTheatreAddressUpdated = theatreRepository.updateTheatreAddress(theatreId, request);
        if (!isTheatreAddressUpdated) {
            throw new IllegalArgumentException("Theatre is Updated, Failed to update the theatre address.");
        }
        return true;
    }

    public boolean deleteTheatre(int theatreId) throws SQLException {

        if (!screenRepository.getScreensByTheatreId(theatreId).isEmpty()) {
            throw new RuntimeException("First delete the screens or shows, those are active under this theatre.");
        }
        return theatreRepository.deleteTheatre(theatreId);
    }
}
