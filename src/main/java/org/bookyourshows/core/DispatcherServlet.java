package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.bookyourshows.service.AuthenticationService;
import org.bookyourshows.utils.JwtUtil;

import java.io.IOException;
import java.util.Map;

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

            Integer userId = (Integer) request.getAttribute("user_id");
            String userRole = (String) request.getAttribute("user_role");
            if (userRole == null || userRole.isEmpty() || userRole.equals("null")) {

                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Authorization required");
            }

            if (!AuthenticationService.isAuthorized(accessLevel, userRole)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }


            request.setAttribute("user_id", userId);
            request.setAttribute("user_role", userRole);

            servletExecution.forwardRequest(servletDetails.getServlet(), request, response);

        } catch (NumberFormatException e) {
            System.out.println("Thambi");
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        } catch (RuntimeException e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}