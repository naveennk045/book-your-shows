package org.bookyourshows.service;

import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.theatre.*;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.repository.cache.theatre.TheatreCacheRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.TheatreUtils.*;

public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final TheatreCacheRepository cache;

    public TheatreService() {
        this.theatreRepository = new TheatreRepository();
        this.screenRepository = new ScreenRepository();
        this.cache = new TheatreCacheRepository();
    }

    public Optional<TheatreDetails> getTheatreById(int theatreId) throws SQLException {

        Optional<TheatreDetails> cached = cache.getById(theatreId);
        if (cached.isPresent()) {
            System.out.println("-- Get Theatre by ID : " + theatreId + " from cache --");
            return cached;
        }

        Optional<TheatreDetails> fromDb = theatreRepository.getTheatreById(theatreId);

        fromDb.ifPresent(theatre -> {
            try {
                cache.save(theatre);
            } catch (Exception e) {
                System.err.println("[Service] " + e.getMessage());
            }
        });

        return fromDb;
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
    ) throws SQLException {

        if ("pending".equalsIgnoreCase(status) && !"ADMIN".equalsIgnoreCase(userContext.getUserRole())) {
            throw new SQLException("You are not allowed to perform this action");
        }

        try {
            List<TheatreSummary> redisResults = cache.search(limit, offset, theatreName, city, status);

            if (!redisResults.isEmpty()) {
                System.out.println("-- Get theatres from cache --");
                return redisResults;
            }
        } catch (Exception e) {
            System.err.println("[Service] Redis search failed, falling back to DB: " + e.getMessage());
        }

        return theatreRepository.getAllTheatres(limit, offset, theatreName, city, status);
    }

    public Optional<Address> getTheatreAddress(int theatreId) throws SQLException {
        return theatreRepository.getTheatreAddress(theatreId);
    }

    public void updateTheatreAddress(int theatreId, int ownerId, String role, Address req) throws SQLException {
        hasAccessToResource(theatreId, ownerId, role);

        if (req.getAddressLine1() == null || req.getAddressLine1().isBlank())
            throw new IllegalArgumentException("address_line1 is required");
        if (req.getCity() == null || req.getCity().isBlank()) throw new IllegalArgumentException("city is required");
        if (req.getLatitude() == null || req.getLongitude() == null)
            throw new IllegalArgumentException("latitude and longitude are required");

        boolean updated = theatreRepository.updateTheatreAddress(theatreId, req);
        if (!updated) throw new IllegalArgumentException("Theatre address not found");


        try {
            theatreRepository.getTheatreById(theatreId).ifPresent(theatre -> {
                try {
                    cache.update(theatre);
                    System.out.println("-- Created Theatre by ID : " + theatre + " to cache -- ");
                } catch (Exception ex) {
                    System.err.println("[Service] " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Service] " + e.getMessage());
        }
    }

    public TheatreDetails createTheatre(TheatreCreateRequest request) throws SQLException {

        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());

        if (request.getTotalScreens() <= 0) throw new IllegalArgumentException("totalScreens must be greater than 0");
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank())
            throw new IllegalArgumentException("addressLine1 is required");
        if (request.getCity() == null || request.getCity().isBlank())
            throw new IllegalArgumentException("city is required");
        if (request.getState() == null || request.getState().isBlank())
            throw new IllegalArgumentException("state is required");
        if (request.getPincode() == null || request.getPincode().isBlank())
            throw new IllegalArgumentException("pincode is required");

        if (theatreRepository.getTheatreByOwnerId(request.getOwnerId()).isPresent()) {
            throw new RuntimeException("User can only create one theatre");
        }

        request.setState("PENDING");
        int theatreId = theatreRepository.addTheatre(request);
        boolean isAddressAdded = theatreRepository.addTheatreAddress(request, theatreId);

        if (!isAddressAdded) {
            throw new RuntimeException("Theatre created but address creation failed");
        }

        Optional<TheatreDetails> theatreDetails = this.getTheatreById(theatreId);

        if (theatreDetails.isPresent()) {
            cache.save(theatreDetails.get());
        } else {
            throw new RuntimeException("No theatre found with id: " + theatreId);
        }
        return theatreDetails.get();
    }

    // -------------------- UPDATE THEATRE --------------------
    public boolean updateTheatre(int theatreId, int ownerId, String role, TheatreUpdateRequest request) throws SQLException {

        hasAccessToResource(theatreId, ownerId, role);

        validateTheatreName(request.getTheatreName());
        validateEmail(request.getEmail());
        validateContactNumber(request.getContactNumber());

        if (request.getTotalScreens() <= 0) throw new IllegalArgumentException("totalScreens must be > 0");
        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank())
            throw new IllegalArgumentException("addressLine1 is required");
        if (request.getCity() == null || request.getCity().isBlank())
            throw new IllegalArgumentException("city is required");
        if (request.getState() == null || request.getState().isBlank())
            throw new IllegalArgumentException("state is required");
        if (request.getPincode() == null || request.getPincode().isBlank())
            throw new IllegalArgumentException("pincode is required");

        boolean theatreUpdated = theatreRepository.updateTheatre(theatreId, request);
        boolean addressUpdated = theatreRepository.updateTheatreAddress(theatreId, request);

        if (!theatreUpdated) throw new IllegalArgumentException("Theatre not found");
        if (!addressUpdated) throw new IllegalArgumentException("Failed to update theatre address");

        try {
            theatreRepository.getTheatreById(theatreId).ifPresent(theatre -> {
                try {
                    cache.update(theatre);
                    System.out.println("-- Created Theatre by ID : " + theatre + " to cache -- ");
                } catch (Exception ex) {
                    System.err.println("[Service] " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[Service] " + e.getMessage());
        }

        return true;
    }

    public boolean deleteTheatre(int theatreId, int ownerId, String role) throws SQLException {

        hasAccessToResource(theatreId, ownerId, role);

        if (!screenRepository.getScreensByTheatreId(theatreId).isEmpty()) {
            throw new RuntimeException("Delete screens/shows under this theatre first");
        }

        boolean deleted = theatreRepository.deleteTheatre(theatreId);

        if (deleted) cache.delete(theatreId);

        return deleted;
    }

    private void hasAccessToResource(int theatreId, int ownerId, String role) throws SQLException {
        Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(theatreId);
        if (theatreDetails.isEmpty()) throw new RuntimeException("The theatre does not exist");

        if (!"ADMIN".equals(role) && theatreDetails.get().getTheatre().getOwnerId() != ownerId) {
            throw new RuntimeException("The theatre does not belong to the user");
        }
    }
}