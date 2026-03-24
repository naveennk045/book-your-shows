package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetail;
import org.bookyourshows.dto.screen.ScreenUpdateRequest;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ScreenTypeRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.utils.ScreenUtils;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenTypeRepository screenTypeRepository;

    public ScreenService() {
        this.screenRepository = new ScreenRepository();
        this.theatreRepository = new TheatreRepository();
        this.screenTypeRepository = new ScreenTypeRepository();
    }

    public List<ScreenDetail> getScreensByTheatreId(Integer theatreId) throws SQLException {

        if (theatreRepository.getTheatreById(theatreId).isEmpty()) {
            throw new RuntimeException("Theatre not found");
        }

        return screenRepository.getScreensByTheatreId(theatreId);

    }

    public Optional<ScreenDetail> getScreensByScreenId(Integer screenId) throws SQLException {
        return screenRepository.getScreenById(screenId);
    }

    public int createScreen(ScreenCreateRequest screenCreateRequest) throws SQLException {

        ScreenUtils.validateScreenName(screenCreateRequest.getScreenName());

        if (screenCreateRequest.getScreenTypeId() == null) {
            throw new IllegalArgumentException("Screen Type ID is required");
        }
        if (screenTypeRepository.getScreenTypeById(screenCreateRequest.getScreenTypeId()).isEmpty()) {
            throw new IllegalArgumentException("Screen Type is not found");
        }
        /*

        if (screenCreateRequest.getNoOfSeats() == null) {
            throw new IllegalArgumentException("No of Seats is required");
        }
        if (screenCreateRequest.getTotalRows() == null) {
            throw new IllegalArgumentException("Total Rows is required");
        }
        if (screenCreateRequest.getNoOfSeats() < 1){
            throw new IllegalArgumentException("Minimum 1 seat is required");
        }
        if (screenCreateRequest.getTotalRows() < 1){
            throw new IllegalArgumentException("Minimum 1 row is required");
        }*/

        return screenRepository.addScreen(screenCreateRequest);
    }
     public boolean updateScreen(ScreenUpdateRequest screenUpdateRequest,int screenId,int theatreId) throws SQLException, AccessDeniedException {
         if(!screenRepository.getScreenById(screenId).get().getTheatreId().equals(theatreId)){
             throw new AccessDeniedException("Access denied");
         }

         if (screenRepository.getScreenById(screenId).isEmpty()) {
            throw new IllegalArgumentException("Screen is not found");
        }
        ScreenUtils.validateScreenName(screenUpdateRequest.getScreenName());


        if (screenUpdateRequest.getScreenTypeId() == null) {
            throw new IllegalArgumentException("Screen Type ID is required");
        }
        if (screenTypeRepository.getScreenTypeById(screenUpdateRequest.getScreenTypeId()).isEmpty()) {
            throw new IllegalArgumentException("Screen Type is not found");
        }
        /*
        if (screenCreateRequest.getNoOfSeats() == null) {
            throw new IllegalArgumentException("No of Seats is required");
        }
        if (screenCreateRequest.getTotalRows() == null) {
            throw new IllegalArgumentException("Total Rows is required");
        }
        if (screenCreateRequest.getNoOfSeats() < 1){
            throw new IllegalArgumentException("Minimum 1 seat is required");
        }
        if (screenCreateRequest.getTotalRows() < 1){
            throw new IllegalArgumentException("Minimum 1 row is required");
        }*/


        return screenRepository.updateScreen(screenUpdateRequest, screenId);
    }

    public boolean deleteScreen(Integer screenId,Integer theatreId) throws SQLException, AccessDeniedException {
        if(!screenRepository.getScreenById(screenId).get().getTheatreId().equals(theatreId)){
            throw new AccessDeniedException("Access denied");
        }


        return screenRepository.deleteScreen(screenId);
    }



}
