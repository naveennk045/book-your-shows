package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.AuthenticationService;

import java.io.IOException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private final ServletExecution servletExecution;
    private final ServletMapping  servletMapping;
    private final ObjectMapper objectMapper;

    public DispatcherServlet() {
        this.servletExecution = new ServletExecution();
        this.servletMapping = new ServletMapping();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ((request.getMethod().equals("POST") || request.getMethod().equals("PUT")) && !isJsonContentType(request)) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Content-Type must be application/json"));
            return;
        }

        try {
            System.out.println("Incoming request : " + request.getRequestURI());

            ServletDetails servletDetails = servletMapping.getServlet(request.getPathInfo());

            if (servletDetails == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
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

                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Authorization required");
            }

            if (!AuthenticationService.isAuthorized(accessLevel, userRole)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }


            request.setAttribute("user_id", userId);
            request.setAttribute("user_role", userRole);

            servletExecution.forwardRequest(servletDetails.getServlet(), request, response);

        } catch (SecurityException e) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        } catch (RuntimeException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }

    private boolean isJsonContentType(HttpServletRequest request) {
        String ct = request.getContentType();
        return ct != null && ct.toLowerCase().contains("application/json");
    }

}