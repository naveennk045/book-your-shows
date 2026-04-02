package org.bookyourshows.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.jsonwebtoken.Claims;
import org.bookyourshows.config.RedisManager;
import org.bookyourshows.core.AccessLevel;
import org.bookyourshows.dto.user.UserAuth;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.repository.UserRepository;
import org.bookyourshows.utils.JwtUtil;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.exceptions.JedisException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.bookyourshows.utils.UserUtils.*;
import static org.bookyourshows.utils.UserUtils.validateName;


public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService() {
        this.userRepository = new UserRepository();
    }


    public UserDetails registerUser(UserCreateRequest request) throws SQLException, CustomException {

        if (request.getUserRole().equals("ADMIN")) {
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

        String token = JwtUtil.generateToken(user.getUserId(), user.getRole());

        Claims claims = JwtUtil.validateToken(token);
        String jti = claims.getId();

        RedisClient redisClient = RedisManager.getClient();

        String key = "auth:token:" + jti;
        System.out.println("Key : " + key);

        userRepository.updateLoginTimeStamp(Timestamp.valueOf(LocalDateTime.now()), user.getUserId());

        redisClient.setex(
                key.getBytes(),
                3600,
                "valid".getBytes()
        );

        return token;
    }

    public void logout(String jti) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "auth:token:" + jti;
            redisClient.del(key);
        } catch (JedisException e) {
            System.err.println("[Authentication Service] Jedis failed to delete token");
            throw new JedisException(e.getMessage());
        }
    }


    public String refreshToken(String token) throws SQLException {
        Claims claims = JwtUtil.validateToken(token);

        Integer userId = Integer.parseInt(claims.getSubject());
        String role = claims.get("role", String.class);

        userRepository.updateLoginTimeStamp(Timestamp.valueOf(LocalDateTime.now()), userId);

        return JwtUtil.generateToken(userId, role);
    }

    public static boolean isAuthorized(AccessLevel accessLevel, String userRole) {

        if (accessLevel == AccessLevel.PUBLIC) {
            return true;
        }

        if (accessLevel == AccessLevel.CUSTOMER) {
            return userRole.equals("CUSTOMER") ||
                    userRole.equals("ADMIN") ||
                    userRole.equals("THEATRE_OWNER");
        }

        if (accessLevel == AccessLevel.THEATRE_OWNER) {
            return userRole.equals("THEATRE_OWNER") ||
                    userRole.equals("ADMIN");
        }

        if (accessLevel == AccessLevel.ADMIN) {
            return userRole.equals("ADMIN");
        }

        return false;
    }
}
