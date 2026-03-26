package org.bookyourshows.service;

import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.dto.user.UserSummary;
import org.bookyourshows.dto.user.UserUpdateRequest;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.UserUtils.*;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public Optional<UserDetails> getUserById(int userId) throws SQLException {
        return userRepository.getUserByUserId(userId);
    }

    public Optional<UserDetails> getUserByEmail(String email) throws SQLException {
        validateEmail(email);
        return userRepository.getUserByEmail(email);
    }

    public Optional<UserDetails> getUserByMobile(String mobile) throws SQLException {
        validateMobile(mobile);
        return userRepository.getUserByMobileNumber(mobile);
    }
    public List<UserSummary> getAllUsers(Integer limit,
                                         Integer offset,
                                         String email,
                                         String role) throws SQLException {

        if (limit > 100 || limit < 0 || offset < 0) {
            throw new IllegalArgumentException("Invalid pagination values");
        }

        return userRepository.getAllUsers(limit, offset, email, role);
    }

    public UserDetails createUser(UserCreateRequest request) throws SQLException {

        validateEmail(request.getEmail());
        validateMobile(request.getMobileNumber());
        validatePassword(request.getPassword());
        validateName(request.getFirstName(), "First name");

        if (userRepository.getUserByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.getUserByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new RuntimeException("Mobile number already exists");
        }

        int userId = userRepository.createUser(request);

        boolean isAddressCreated = userRepository.createUserAddress(request, userId);
        if (!isAddressCreated) {
            throw new RuntimeException("User created but address creation failed");
        }

        Optional<UserDetails> user = userRepository.getUserByUserId(userId);
        if (user.isPresent()) {
            return user.get();
        }

        throw new RuntimeException("User created but not found");
    }

    public boolean updateUser(int userId, UserUpdateRequest request) throws SQLException {

        validateName(request.getFirstName(), "First name");

        boolean updated = userRepository.updateUser(request, userId);
        if (!updated) {
            throw new IllegalArgumentException("User not found");
        }

        boolean addressUpdated = userRepository.updateUserAddress(request, userId);
        if (!addressUpdated) {
            throw new RuntimeException("User updated but address update failed");
        }

        return true;
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userRepository.deleteUser(userId);
    }
}