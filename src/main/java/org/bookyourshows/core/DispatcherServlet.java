package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private final ServletExecution servletExecution;
    private final ServletMapping servletMapping;
    private final ObjectMapper objectMapper;

    public DispatcherServlet() {
        this.servletExecution = new ServletExecution();
        this.servletMapping = new ServletMapping();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            if ((method.equals("POST") || method.equals("PUT")) && !isJsonContentType(request)) {

                response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "Content-Type must be application/json"));
                return;
            }

            ServletDetails servletDetails = servletMapping.getServlet(request.getPathInfo());

            if (servletDetails == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "No route found");
                return;
            }

            AccessLevel accessLevel = servletDetails.getAccessLevel(method);

            if (accessLevel == AccessLevel.PUBLIC) {
                log.info("Processing public request: {} {}", method, uri);
                servletExecution.forwardRequest(servletDetails.getServlet(), request, response);
                return;
            }

            Integer userId = (Integer) request.getAttribute("user_id");
            String userRole = (String) request.getAttribute("user_role");

            if (userRole == null || userRole.isEmpty() || userRole.equals("null")) {

                log.warn("Authorization missing: {} {}", method, uri);
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Authorization required");
                return;
            }

            if (!AuthenticationService.isAuthorized(accessLevel, userRole)) {
                log.warn("Access denied: {} {}", method, uri);
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }

            log.info("Processing protected request: {} {}", method, uri);
            servletExecution.forwardRequest(servletDetails.getServlet(), request, response);

        } catch (SecurityException e) {
            log.error("Security error: {} {}", method, uri, e);
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied");

        } catch (NumberFormatException e) {
            log.warn("Invalid input: {} {}", method, uri);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request");

        } catch (RuntimeException e) {
            log.error("Unexpected error: {} {}", method, uri, e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");

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