package org.bookyourshows.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserContext;
import org.bookyourshows.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class JWTFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JWTFilter.class);

    private final ObjectMapper objectMapper;

    public JWTFilter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String method = request.getMethod();
        String uri = request.getRequestURI();

        String authHeader = request.getHeader("Authorization");

        // No token → skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No auth token: {} {}", method, uri);
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        Claims claims;

        try {
            claims = JwtUtil.validateToken(token);

        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {} {}", method, uri);
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {} {}", method, uri);
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;

        } catch (Exception e) {
            log.error("Auth error: {} {}", method, uri, e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            return;
        }

        Integer userId = Integer.parseInt(claims.getSubject());
        String userRole = claims.get("role", String.class);

        UserContext context = new UserContext();
        context.setUserRole(userRole);
        context.setUserId(userId);

        request.setAttribute("userContext", context);
        request.setAttribute("user_id", userId);
        request.setAttribute("user_role", userRole);

        log.debug("User authenticated: {} {}", method, uri);

        chain.doFilter(servletRequest, servletResponse);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);

        if (message == null) {
            message = "Unknown error";
        }

        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}