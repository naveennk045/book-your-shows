package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.Views;
import org.bookyourshows.dto.address.Address;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.dto.user.UserSummary;
import org.bookyourshows.dto.user.UserUpdateRequest;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserServlet extends HttpServlet {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(UserServlet.class);


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


        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // /users/{userId}/address
            if (parts.length == 4 && "address".equals(parts[3])) {
                handleGetUserAddress(parts[2], response, userContext);
                return;
            }

            // /users/{userId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleGetUserById(parts[2], response, userContext);
                return;
            }

            // /users
            handleListUsers(request, response, userContext);

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // /users/{userId}/address
            if (parts.length == 4 && "address".equals(parts[3])) {
                handleUpdateUserAddress(parts[2], request, response, userContext);
                return;
            }

            // /users/{userId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleUpdateUser(parts[2], request, response, userContext);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "user_id is required");

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] parts = splitPath(request);
        UserContext userContext = getUserContext(request);

        try {
            // /users/{userId}
            if (parts.length == 3 && !parts[2].isBlank()) {
                handleDeleteUser(parts[2], response, userContext);
                return;
            }

            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "user_id is required");

        } catch (SQLException e) {
            log.error("DB failure while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleGetUserById(String userIdStr, HttpServletResponse response,
                                   UserContext userContext)
            throws IOException, SQLException, CustomException {

        int userId = parseId(userIdStr, "Invalid user id", response);
        if (userId == -1) return;

        Optional<UserDetails> user = userService.getUserById(userId, userContext);

        if (user.isEmpty()) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        writeWithView(response, user.get(), userContext.getUserRole());
    }

    private void handleGetUserAddress(String userIdStr, HttpServletResponse response,
                                      UserContext userContext)
            throws IOException, SQLException, CustomException {

        int userId = parseId(userIdStr, "Invalid user id", response);
        if (userId == -1) return;

        Optional<Address> address = userService.getUserAddress(userId, userContext);

        if (address.isEmpty()) {
            writeError(response, HttpServletResponse.SC_OK, "Address not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), address.get());
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response,
                                 UserContext userContext)
            throws IOException, SQLException, CustomException {

        if (!"ADMIN".equals(userContext.getUserRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String email = request.getParameter("email");
        String mobile = request.getParameter("mobile");

        if (email != null && !email.isBlank()) {
            Optional<UserDetails> user = userService.getUserByEmail(email);
            writeOptionalUser(response, user);
            return;
        }

        if (mobile != null && !mobile.isBlank()) {
            Optional<UserDetails> user = userService.getUserByMobile(mobile);
            writeOptionalUser(response, user);
            return;
        }

        String role = request.getParameter("role");
        int limit = parseIntOrDefault(request.getParameter("limit"), 20);
        int offset = parseIntOrDefault(request.getParameter("offset"), 0);

        List<UserSummary> users = userService.getAllUsers(limit, offset, null, role);

        if (users.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "No records found"));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), users);
    }

    private void handleUpdateUser(String userIdStr, HttpServletRequest request,
                                  HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int userId = parseId(userIdStr, "Invalid user id", response);
        if (userId == -1) return;

        UserUpdateRequest req;
        try {
            req = objectMapper.readValue(request.getReader(), UserUpdateRequest.class);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        userService.updateUser(userId, req, userContext);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "User updated successfully"));
    }

    private void handleUpdateUserAddress(String userIdStr, HttpServletRequest request,
                                         HttpServletResponse response, UserContext userContext)
            throws IOException, SQLException, CustomException {

        int userId = parseId(userIdStr, "Invalid user id", response);
        if (userId == -1) return;

        Address req;
        try {
            req = objectMapper.readValue(request.getReader(), Address.class);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        userService.updateUserAddress(userId, req, userContext);

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "User address updated successfully"));
    }

    private void handleDeleteUser(String userIdStr, HttpServletResponse response,
                                  UserContext userContext)
            throws IOException, SQLException, CustomException {

        int userId = parseId(userIdStr, "Invalid user id", response);
        if (userId == -1) return;

        boolean deleted = userService.deleteUser(userId, userContext);

        if (!deleted) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of("message", "User deleted successfully"));
    }

    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }

    private UserContext getUserContext(HttpServletRequest request) {
        return (UserContext) request.getAttribute("userContext");
    }

    private int parseId(String idStr, String errorMessage, HttpServletResponse response)
            throws IOException {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            return -1;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void writeOptionalUser(HttpServletResponse response, Optional<UserDetails> data)
            throws IOException {
        if (data.isPresent()) {
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), data.get());
        } else {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private void writeWithView(HttpServletResponse response, Object data, String role)
            throws IOException {
        Class<?> view = Views.resolveView(role);
        objectMapper.writerWithView(view).writeValue(response.getWriter(), data);
    }
}