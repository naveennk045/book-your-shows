package org.bookyourshows.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.jsonwebtoken.Claims;
import org.bookyourshows.dto.user.UserAuth;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.repository.UserRepository;
import org.bookyourshows.utils.JwtUtil;

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

        if(request.getUserRole().equals("ADMIN")){
            throw new RuntimeException("You can't register an administrator");
        }

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
    public String login(String email, String password) throws SQLException {

        Optional<UserAuth> userOptional = userRepository.getUserAuthByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid credentials");

        }

        UserAuth user = userOptional.get();

        BCrypt.Result result = BCrypt.verifyer()
                .verify(password.toCharArray(), user.getPassword());

        if (!result.verified) {
            throw new RuntimeException("Invalid credentials");
        }

        return JwtUtil.generateToken(user.getUserId(), user.getRole());
    }

    public String refreshToken(String token) {
        Claims claims = JwtUtil.validateToken(token);

        Integer userId = Integer.parseInt(claims.getSubject());
        String role = claims.get("role", String.class);

        return JwtUtil.generateToken(userId, role);
    }
}
