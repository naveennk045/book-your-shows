package org.bookyourshows.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.config.RedisManager;
import org.bookyourshows.utils.JwtUtil;
import redis.clients.jedis.RedisClient;

import java.io.IOException;
import java.util.Map;

public class TokenWhitelistFilter implements Filter {

    private final ObjectMapper objectMapper;

    public TokenWhitelistFilter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = JwtUtil.validateToken(token);

            String jti = claims.getId();

            RedisClient redisClient = RedisManager.getClient();

            String key = "auth:token:" + jti;

            byte[] val = redisClient.get(key.getBytes());

            if (val == null) {
                sendError(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }


            chain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            sendError(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}