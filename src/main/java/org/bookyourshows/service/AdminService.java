package org.bookyourshows.service;

import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;

public class AdminService {

    private final TheatreRepository theatreRepository;

    public AdminService(){this.theatreRepository = new TheatreRepository();
    }

    public boolean updateTheatreStatus(Integer theatreId,String status) throws SQLException, ResourceNotFoundException {
        if(this.theatreRepository.getTheatreById(theatreId).isEmpty()){
            throw  new ResourceNotFoundException("Theatre with this id does not exist");
        }
        return this.theatreRepository.updateTheatreStatus(theatreId,status);
    }
}
