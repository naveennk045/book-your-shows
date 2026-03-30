package org.bookyourshows.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
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

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
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

        System.out.println("userRole: " + userRole + " userId: " + userId);

        request.setAttribute("user_id", userId);
        request.setAttribute("user_role", userRole);



        UserContext context = new UserContext();
        context.setUserRole(userRole);
        context.setUserId(userId);

        request.setAttribute("userContext", context);

        chain.doFilter(servletRequest, servletResponse);


    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
