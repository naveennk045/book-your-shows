package org.bookyourshows.service;

import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.*;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.*;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.repository.cache.theatre.TheatreCacheRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.TheatreUtils.*;

public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final TheatreCacheRepository theatreCacheRepository;

    public TheatreService() {
        this.theatreRepository = new TheatreRepository();
        this.theatreCacheRepository = new TheatreCacheRepository();
    }

    public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {

        Optional<TheatreDetails> theatreDetails = theatreCacheRepository.getById(theatreId);
        if (theatreDetails.isPresent()) {
            return theatreDetails;
        }

        theatreDetails = theatreRepository.getTheatreById(theatreId);

        theatreDetails.ifPresent(theatreCacheRepository::save);

        return theatreDetails;
    }

    public Optional<TheatreDetails> getTheatreByOwnerId(Integer ownerId) throws SQLException {
        return theatreRepository.getTheatreByOwnerId(ownerId);
    }

    public List<TheatreSummary> getAllTheatre(Integer limit,
                                              Integer offset,
                                              String theatreName,
                                              String city,
                                              String status,
                                              UserContext userContext
    ) throws SQLException, CustomException {

        if (status != null && !"ADMIN".equalsIgnoreCase(userContext.getUserRole())) {
            throw new ForbiddenException("Access denied");
        }

        status = "APPROVED";

        List<TheatreSummary> theatreSummaries = theatreCacheRepository.search(limit, offset, theatreName, city, status);

        if (!theatreSummaries.isEmpty()) {
            return theatreSummaries;
        }

        return theatreRepository.getAllTheatres(limit, offset, theatreName, city, status);
    }

    public Optional<Address> getTheatreAddress(int theatreId) throws SQLException {
        return theatreRepository.getTheatreAddress(theatreId);
    }

    public void updateTheatreAddress(int theatreId, int ownerId, String role, Address request) throws SQLException, CustomException {

        hasAccessToResource(theatreId, ownerId, role);
        validateTheatreAddress(request);

        boolean updated = theatreRepository.updateTheatreAddress(theatreId, request);
        if (!updated) throw new ResourceNotFoundException("Theatre address not found");

        theatreRepository.getTheatreById(theatreId).ifPresent(theatreCacheRepository::update);
    }

    public TheatreDetails createTheatre(TheatreCreateRequest request) throws CustomException, SQLException {

        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());
        validateTheatreRequest(request);

        if (theatreRepository.getTheatreByOwnerId(request.getOwnerId()).isPresent()) {
            throw new ResourceConflictException("You already have an existing theatre, Only one theatre can be created.");
        }
        request.setState("PENDING");
        int theatreId = theatreRepository.addTheatre(request);
        boolean isAddressAdded = theatreRepository.addTheatreAddress(request, theatreId);

        if (!isAddressAdded) {
            throw new PartialUpdateException("Theatre created but address creation failed");
        }

        Optional<TheatreDetails> theatreDetails = this.getTheatreById(theatreId);

        if (theatreDetails.isPresent()) {
            theatreCacheRepository.save(theatreDetails.get());
        } else {
            throw new ResourceNotFoundException("No theatre found with id: " + theatreId);
        }
        return theatreDetails.get();
    }

    public boolean updateTheatre(int theatreId, int ownerId, String role, TheatreUpdateRequest request) throws SQLException, CustomException {

        hasAccessToResource(theatreId, ownerId, role);

        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());
        validateTheatreUpdateRequest(request);

        boolean theatreUpdated = theatreRepository.updateTheatre(theatreId, request);
        boolean addressUpdated = theatreRepository.updateTheatreAddress(theatreId, request);

        if (!theatreUpdated) throw new TheatreCreationException("Failed to update the theatre");
        if (!addressUpdated) throw new PartialUpdateException("Failed to update theatre address");


        theatreRepository.getTheatreById(theatreId).ifPresent(theatreCacheRepository::update);
        return true;
    }
/*

    public boolean deleteTheatre(int theatreId, int ownerId, String role) throws SQLException, CustomException {

        hasAccessToResource(theatreId, ownerId, role);

        if (!screenRepository.getScreensByTheatreId(theatreId).isEmpty()) {
            throw new ResourceConflictException("Delete screens/shows under this theatre first");
        }

        boolean deleted = theatreRepository.deleteTheatre(theatreId);

        if (deleted) theatreCacheRepository.delete(theatreId);

        return deleted;
    }
*/

    private void hasAccessToResource(int theatreId, int ownerId, String role) throws SQLException, CustomException {
        Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(theatreId);
        if (theatreDetails.isEmpty()) throw new ResourceNotFoundException("Theatre not found with id: " + theatreId);

        if (!"ADMIN".equals(role) && theatreDetails.get().getTheatre().getOwnerId() != ownerId) {
            throw new ForbiddenException("Access denied");
        }
    }
}