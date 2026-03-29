package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.*;

import org.bookyourshows.dto.address.AddressDTO;
import org.bookyourshows.dto.user.address.AddressResponse;
import org.bookyourshows.dto.user.address.AddressUpdateRequest;
import org.bookyourshows.dto.user.UserSummary;
import org.bookyourshows.dto.user.UserUpdateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserServlet extends HttpServlet {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserServlet() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        try {
            // /users/{user_id}
            if (parts.length == 3) {
                int userId;
                try {
                    userId = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeMessage(response, "Invalid user id");
                    return;
                }

                Optional<UserDetails> user = userService.getUserById(userId);

                if (user.isPresent()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), user.get());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeMessage(response, "User not found");
                }
                return;
            }
            if (parts.length == 4 && "address".equals(parts[3])) {

                int userId = Integer.parseInt(parts[2]);

                Optional<AddressDTO> address = userService.getUserAddress(userId);

                if (address.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeMessage(response, "Address not found");
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), address.get());
                return;
            }

            // /users?email=&mobile=&role=&

            String email = request.getParameter("email");
            String mobile = request.getParameter("mobile");

            if (email != null && !email.isBlank()) {
                Optional<UserDetails> user = userService.getUserByEmail(email);
                writeOptionalResponse(response, user);
                return;
            }

            if (mobile != null && !mobile.isBlank()) {
                Optional<UserDetails> user = userService.getUserByMobile(mobile);
                writeOptionalResponse(response, user);
                return;
            }

            String role = request.getParameter("role");
            Integer limit = parseIntOrDefault(request.getParameter("limit"), 20);
            Integer offset = parseIntOrDefault(request.getParameter("offset"), 0);

            List<UserSummary> users = userService.getAllUsers(limit, offset, null, role);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), users);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, e.getMessage());

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, "Database error");
        }
    }
/*

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if(request.getPathInfo().equals("/users")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        if (request.getContentType() == null ||
                !request.getContentType().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            writeMessage(response, "Content-Type must be application/json");
            return;
        }

        try {
            UserCreateRequest req = objectMapper.readValue(request.getReader(), UserCreateRequest.class);

            UserDetails created = userService.createUser(req);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "User created successfully",
                            "user_id", created.getUserId()));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, e.getMessage());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, e.getMessage());
        }
    }
*/

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();

        if (path == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "Invalid path");
            return;
        }

        String[] parts = path.split("/");

        // 1. UPDATE ADDRESS → /users/{id}/address
        if (parts.length == 4 && "address".equals(parts[3])) {

            int userId;
            try {
                userId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeMessage(response, "Invalid user id");
                return;
            }

            AddressDTO req;
            try {
                req = objectMapper.readValue(request.getReader(), AddressDTO.class);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeMessage(response, "Invalid JSON body");
                return;
            }

            try {
                userService.updateUserAddress(userId, req);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "User address updated successfully"));

            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeMessage(response, e.getMessage());
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeMessage(response, "Database error");
            }

            return;
        }

        // 2. UPDATE USER → /users/{id}
        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "User id required");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "Invalid user id");
            return;
        }

        UserUpdateRequest req;
        try {
            req = objectMapper.readValue(request.getReader(), UserUpdateRequest.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "Invalid JSON body");
            return;
        }

        try {
            userService.updateUser(userId, req);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "User updated successfully"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, e.getMessage());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, "Database error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        // DELETE :  /users/{user_id}


        String path = request.getPathInfo();
        String[] parts = path.split("/");

        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "User id required");
            return;
        }

        try {
            int userId = Integer.parseInt(parts[2]);

            boolean deleted = userService.deleteUser(userId);

            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeMessage(response, "User not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            writeMessage(response, "User deleted successfully");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, "Invalid user id");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, "Database error");
        }
    }

    private void writeMessage(HttpServletResponse response, String message) throws IOException {
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }

    private Integer parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void writeOptionalResponse(HttpServletResponse response, Optional<?> data) throws IOException {
        if (data.isPresent()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), data.get());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeMessage(response, "User not found");
        }
    }

}