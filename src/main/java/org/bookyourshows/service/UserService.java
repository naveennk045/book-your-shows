package org.bookyourshows.service;

import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.dto.user.UserSummary;
import org.bookyourshows.dto.user.UserUpdateRequest;
import org.bookyourshows.exceptions.*;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.bookyourshows.utils.UserUtils.*;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public Optional<UserDetails> getUserById(Integer userId, UserContext userContext) throws SQLException, CustomException {

        hasAccessToUserDetails(userContext, userId);
        return userRepository.getUserByUserId(userId);
    }

    public Optional<UserDetails> getUserByEmail(String email) throws SQLException, CustomException {
        validateEmail(email);
        return userRepository.getUserByEmail(email);
    }

    public Optional<UserDetails> getUserByMobile(String mobile) throws SQLException, CustomException {
        validateMobile(mobile);
        return userRepository.getUserByMobileNumber(mobile);
    }

    public List<UserSummary> getAllUsers(Integer limit,
                                         Integer offset,
                                         String email,
                                         String role) throws SQLException, CustomException {

        if (limit > 100 || limit < 0 || offset < 0) {
            throw new BadRequestException("Invalid pagination values");
        }

        return userRepository.getAllUsers(limit, offset, email, role);
    }

    public Optional<Address> getUserAddress(Integer userId, UserContext userContext) throws SQLException, CustomException {
        hasAccessToUserDetails(userContext, userId);
        return userRepository.getUserAddress(userId);
    }

    public void updateUserAddress(int userId, Address request, UserContext userContext) throws SQLException, CustomException {

        hasAccessToUserDetails(userContext, userId);

        validateAddress(request);

        boolean updated = userRepository.updateUserAddress(request, userId);

        if (!updated) {
            throw new ResourceNotFoundException("User address not found");
        }

    }

    public void updateUser(int userId, UserUpdateRequest request, UserContext userContext) throws SQLException, CustomException {
        hasAccessToUserDetails(userContext, userId);
        validateName(request.getFirstName(), "First name");

        boolean updated = userRepository.updateUser(request, userId);
        if (!updated) {
            throw new ResourceNotFoundException("User not found");
        }

        boolean addressUpdated = userRepository.updateUserAddress(request, userId);
        if (!addressUpdated) {
            throw new PartialUpdateException("User updated but address update failed");
        }

    }

    public boolean deleteUser(int userId, UserContext userContext) throws SQLException, CustomException {
        hasAccessToUserDetails(userContext, userId);
        return userRepository.deleteUser(userId);
    }


    public void hasAccessToUserDetails(UserContext userContext, Integer userId) throws CustomException {
        if (!userContext.getUserRole().equals("ADMIN") && !Objects.equals(userContext.getUserId(), userId)) {
            throw new ForbiddenException("Access denied");
        }
    }
}