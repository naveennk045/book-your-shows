package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.screen.ScreenUpdateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ScreenTypeRepository;
import org.bookyourshows.repository.ShowRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.utils.ScreenUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenTypeRepository screenTypeRepository;
    private final ShowRepository showRepository;

    public ScreenService() {
        this.screenRepository = new ScreenRepository();
        this.theatreRepository = new TheatreRepository();
        this.screenTypeRepository = new ScreenTypeRepository();
        this.showRepository = new ShowRepository();
    }

    public List<ScreenDetails> getScreensByTheatreId(Integer theatreId) throws SQLException {

        if (theatreRepository.getTheatreById(theatreId).isEmpty()) {
            throw new RuntimeException("Theatre not found");
        }

        return screenRepository.getScreensByTheatreId(theatreId);

    }

    public Optional<ScreenDetails> getScreensByScreenId(Integer screenId, Integer theatreId) throws SQLException {
        return screenRepository.getScreenById(screenId, theatreId);
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

    public boolean updateScreen(ScreenUpdateRequest screenUpdateRequest, int screenId, int theatreId) throws SQLException {


        if (!screenRepository.getScreenById(screenId, theatreId).get().getTheatreId().equals(theatreId)) {
            throw new IllegalArgumentException("Screen not found");
        }

        if (screenRepository.getScreenById(screenId, theatreId).isEmpty()) {
            throw new IllegalArgumentException("Screen is not found");
        }
        ScreenUtils.validateScreenName(screenUpdateRequest.getScreenName());


        if (screenUpdateRequest.getScreenTypeId() == null) {
            throw new IllegalArgumentException("Screen Type ID is required");
        }
        if (screenTypeRepository.getScreenTypeById(screenUpdateRequest.getScreenTypeId()).isEmpty()) {
            throw new IllegalArgumentException("Screen Type is not found");
        }

        return screenRepository.updateScreen(screenUpdateRequest, screenId);
    }

    public boolean deleteScreen(Integer screenId, Integer theatreId) throws SQLException {

        List<ShowDetails> showDetails = this.showRepository.getShowsByScreenId(screenId);
        if (!showDetails.isEmpty()) {
            throw new IllegalArgumentException("There are active shows found in this screen");
        }

        return screenRepository.deleteScreen(screenId);
    }

    private void hasAccessToResources(Integer screenId, Integer theatreId, UserContext userContext) throws SQLException {

    }
}
