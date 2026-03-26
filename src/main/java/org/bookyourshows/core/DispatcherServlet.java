package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.repository.UserRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class DispatcherServlet extends HttpServlet {

    private final ServletExecution servletExecution;
    private final ServletMapping servletMapping;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public DispatcherServlet() {
        this.servletExecution = new ServletExecution();
        this.servletMapping = new ServletMapping();
        this.objectMapper = new ObjectMapper();
        this.userRepository = new UserRepository();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            ServletDetails servletDetails = servletMapping.getServlet(request.getPathInfo());

            if (servletDetails == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
                return;
            }

            if (servletDetails.getAccessLevel() == AccessLevel.PUBLIC) {
                servletExecution.forwardRequest(servletDetails.getServlet(), request, response);
                return;
            }

            String token = request.getHeader("token");

            if (token == null || token.isEmpty()) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            Optional<UserDetails> userOptional = userRepository.getUserByEmail(token);

            if (userOptional.isEmpty()) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            UserDetails user = userOptional.get();
            String userRole = user.getUserRole();
            Integer userId = user.getUserId();

            if (!isAuthorized(servletDetails.getAccessLevel(), userRole)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }

            request.setAttribute("user_id", userId);
            request.setAttribute("user_role", userRole);

            servletExecution.forwardRequest(servletDetails.getServlet(), request, response);

        } catch (RuntimeException e) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
        } catch (SQLException e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }


    private boolean isAuthorized(AccessLevel accessLevel, String userRole) {

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

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}