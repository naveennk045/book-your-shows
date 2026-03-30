package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.bookyourshows.utils.JwtUtil;

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
            System.out.println("Incoming request : " + request.getRequestURI());
            System.out.println("Incoming path : " + request.getPathInfo());

            ServletDetails servletDetails = servletMapping.getServlet(request.getPathInfo());

            if (servletDetails == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
                return;
            }

            //  derive access level from method
            AccessLevel accessLevel = servletDetails.getAccessLevel(request.getMethod());

            if (accessLevel == AccessLevel.PUBLIC) {
                servletExecution.forwardRequest(servletDetails.getServlet(), request, response);
                return;
            }

            // From here on, token is mandatory
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
                return;
            }

            String token = header.substring(7);
            Claims claims;

            try {
                claims = JwtUtil.validateToken(token);
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            Integer userId = Integer.parseInt(claims.getSubject());
            String userRole = claims.get("role", String.class);

            if (!isAuthorized(accessLevel, userRole)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }

            request.setAttribute("user_id", userId);
            request.setAttribute("user_role", userRole);

            servletExecution.forwardRequest(servletDetails.getServlet(), request, response);

        } catch (RuntimeException e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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