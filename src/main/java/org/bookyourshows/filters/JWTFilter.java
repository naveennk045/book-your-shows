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

import java.io.IOException;
import java.util.Map;

public class JWTFilter implements Filter {


    private final ObjectMapper objectMapper;

    public JWTFilter() {
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;


        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        String token = authHeader.substring(7);

        Claims claims;

        try {
            claims = JwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            sendError(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            sendError(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception e) {
            sendError(httpServletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            return;
        }


        Integer userId = Integer.parseInt(claims.getSubject());
        String userRole = claims.get("role", String.class);

        UserContext context = new UserContext();
        context.setUserRole(userRole);
        context.setUserId(userId);

        httpServletRequest.setAttribute("userContext", context);

        httpServletRequest.setAttribute("user_id", userId);
        httpServletRequest.setAttribute("user_role", userRole);


        chain.doFilter(servletRequest, servletResponse);

    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
