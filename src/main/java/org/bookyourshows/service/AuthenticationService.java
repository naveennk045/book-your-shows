package org.bookyourshows.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.bookyourshows.utils.UserUtils.*;
import static org.bookyourshows.utils.UserUtils.validateName;


public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService() {
        this.userRepository = new UserRepository();
    }


    public UserDetails registerUser(UserCreateRequest request) throws SQLException {

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

        String hashedPassword = BCrypt.withDefaults()
                .hashToString(12, request.getPassword().toCharArray());

        request.setPassword(hashedPassword);

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
}
