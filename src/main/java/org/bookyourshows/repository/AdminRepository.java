package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminRepository {

    public  AdminRepository() {

    }

    public boolean updateTheatreStatus(Integer theatreId, String status) throws SQLException {
        String query = "UPDATE theatres SET status = ? WHERE theatre_id = ?";

        try(Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, theatreId);
            return preparedStatement.executeUpdate() != 0;

        }
    }

}
