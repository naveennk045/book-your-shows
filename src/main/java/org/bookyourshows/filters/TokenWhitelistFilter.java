package org.bookyourshows.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.config.RedisManager;
import org.bookyourshows.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.Map;

public class TokenWhitelistFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TokenWhitelistFilter.class);

    private final ObjectMapper objectMapper;

    public TokenWhitelistFilter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("Request received: {} {}", method,  uri);

        String authHeader = request.getHeader("Authorization");

        // No token → skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No token present: {} {}", method, uri);
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
                log.warn("Token not whitelisted: {} {}", method, uri);
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            chain.doFilter(servletRequest, servletResponse);

        } catch (JwtException e) {
            log.warn("Invalid JWT: {} {}", method, uri);
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");

        } catch (JedisException e) {
            log.error("Redis error during auth: {} {}", method, uri, e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: Error in authentication");
        } catch (Exception e) {
            log.error("Unexpected error in auth filter: {} {}", method, uri, e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);

        if (message == null) {
            message = "Unexpected error occurred";
        }
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}