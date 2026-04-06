package org.bookyourshows.service;

import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.screen.ScreenUpdateRequest;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ForbiddenException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.exceptions.CreationException;
import org.bookyourshows.repository.ScreenRepository;
import org.bookyourshows.repository.ScreenTypeRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.utils.ScreenUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
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

    public List<ScreenDetails> getScreensByTheatreId(Integer theatreId) throws SQLException, CustomException {

        if (theatreRepository.getTheatreById(theatreId).isEmpty()) {
            throw new ResourceNotFoundException("Theatre not found");
        }

        return screenRepository.getScreensByTheatreId(theatreId);

    }

    public Optional<ScreenDetails> getScreensByScreenId(Integer screenId, Integer theatreId) throws SQLException {
        return screenRepository.getScreenByTheatreIdScreenId(screenId, theatreId);
    }

    public int createScreen(ScreenCreateRequest screenCreateRequest, UserContext userContext) throws SQLException, CustomException {

        Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(screenCreateRequest.getTheatreId());
        if (theatreDetails.isEmpty()) {
            throw new ResourceNotFoundException("No theatre found");
        }

        if (!userContext.getUserRole().equals("ADMIN") && !Objects.equals(theatreDetails.get().getTheatre().getOwnerId(), userContext.getUserId())) {
            throw new ForbiddenException("Access denied");
        }

        ScreenUtils.validateScreenName(screenCreateRequest.getScreenName());

        if (screenCreateRequest.getScreenTypeId() == null) {
            throw new CreationException("Screen Type ID is required");
        }
        if (screenTypeRepository.getScreenTypeById(screenCreateRequest.getScreenTypeId()).isEmpty()) {
            throw new CreationException("Screen Type is not found");
        }

        return screenRepository.addScreen(screenCreateRequest);
    }

    public boolean updateScreen(ScreenUpdateRequest screenUpdateRequest, int screenId, int theatreId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToScreenDetails(screenId, theatreId, userContext);

        if (!screenRepository.getScreenByTheatreIdScreenId(screenId, theatreId).get().getTheatreId().equals(theatreId)) {
            throw new CreationException("Screen not found");
        }

        if (screenRepository.getScreenByTheatreIdScreenId(screenId, theatreId).isEmpty()) {
            throw new CreationException("Screen is not found");
        }
        ScreenUtils.validateScreenName(screenUpdateRequest.getScreenName());


        if (screenUpdateRequest.getScreenTypeId() == null) {
            throw new CreationException("Screen Type ID is required");
        }
        if (screenTypeRepository.getScreenTypeById(screenUpdateRequest.getScreenTypeId()).isEmpty()) {
            throw new CreationException("Screen Type is not found");
        }

        return screenRepository.updateScreen(screenUpdateRequest, screenId);
    }

/*    public boolean deleteScreen(Integer screenId, Integer theatreId, UserContext userContext) throws SQLException {

        hasAccessToResources(screenId, theatreId, userContext);

        List<ShowDetails> showDetails = this.showRepository.getShowsByScreenId(screenId);
        if (!showDetails.isEmpty()) {
            throw new CreationException("There are active shows found in this screen");
        }

        return screenRepository.deleteScreen(screenId);
    }*/

    private void hasAccessToScreenDetails(Integer screenId, Integer theatreId, UserContext userContext) throws SQLException, CustomException {
        if (!userContext.getUserRole().equals("ADMIN")) {
            Optional<ScreenDetails> screenDetails = screenRepository.getScreenByTheatreIdScreenId(screenId, theatreId);
            if (screenDetails.isEmpty()) {
                throw new ResourceNotFoundException("Screen not found");
            }

            Optional<TheatreDetails> theatreDetails = theatreRepository.getTheatreById(theatreId);

            if (!userContext.getUserRole().equals("ADMIN") && !Objects.equals(theatreDetails.get().getTheatre().getOwnerId(), userContext.getUserId())) {
                throw new ForbiddenException("Access denied");
            }

        }
    }
}
