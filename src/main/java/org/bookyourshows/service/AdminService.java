package org.bookyourshows.service;

import org.bookyourshows.repository.AdminRepository;
import org.bookyourshows.repository.TheatreRepository;

import java.sql.SQLException;

public class AdminService {

    private final AdminRepository adminRepository;
    private final TheatreRepository theatreRepository;

    public AdminService(){
        this.adminRepository = new AdminRepository();
        this.theatreRepository = new TheatreRepository();
    }

    public boolean updateTheatreStatus(Integer theatreId,String status) throws SQLException {
        if(this.theatreRepository.getTheatreById(theatreId).isEmpty()){
            throw  new SQLException("theatre not found");
        }
        return this.adminRepository.updateTheatreStatus(theatreId,status);
    }
}
