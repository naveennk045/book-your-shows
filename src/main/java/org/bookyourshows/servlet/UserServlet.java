package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.*;

import org.bookyourshows.dto.Views;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.dto.user.UserSummary;
import org.bookyourshows.dto.user.UserUpdateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.exceptions.CustomException;
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

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        UserContext userContext = (UserContext) request.getAttribute("userContext");

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

                Optional<UserDetails> user = userService.getUserById(userId, userContext);

                if (user.isPresent()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    writeWithView(response, user.get(), userContext.getUserRole());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeMessage(response, "User not found");
                }
                return;
            }
            if (parts.length == 4 && "address".equals(parts[3])) {

                int userId = Integer.parseInt(parts[2]);

                Optional<Address> address = userService.getUserAddress(userId, userContext);

                if (address.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeMessage(response, "Address not found");
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), address.get());
                return;
            }

            if (!request.getAttribute("user_role").equals("ADMIN")) {
                throw new RuntimeException("Access denied");
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

        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeMessage(response, e.getMessage());
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, e.getMessage());

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, "Database error");
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }

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

        UserContext userContext = (UserContext) request.getAttribute("userContext");


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

            Address req;
            try {
                req = objectMapper.readValue(request.getReader(), Address.class);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeMessage(response, "Invalid JSON body");
                return;
            }

            try {
                userService.updateUserAddress(userId, req, userContext);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "User address updated successfully"));

            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeMessage(response, e.getMessage());
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeMessage(response, "Database error");
            } catch (CustomException e) {
                response.setStatus(e.getStatusCode());
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", e.getMessage()));
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
            userService.updateUser(userId, req, userContext);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "User updated successfully"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeMessage(response, e.getMessage());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeMessage(response, "Database error");
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        UserContext userContext = (UserContext) request.getAttribute("userContext");


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

            boolean deleted = userService.deleteUser(userId, userContext);

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
        } catch (CustomException e) {
            response.setStatus(e.getStatusCode());
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
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

    private void writeWithView(HttpServletResponse response, Object data, String role)
            throws IOException {
        Class<?> view = Views.resolveView(role);
        System.out.println("[JsonView] role='" + role + "' view=" + view.getSimpleName()); // add this
        objectMapper.writerWithView(view).writeValue(response.getWriter(), data);
    }

}