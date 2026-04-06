package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.service.AuthenticationService;
import org.bookyourshows.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class AuthenticationServlet extends HttpServlet {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServlet.class);

    public AuthenticationServlet() {
        this.authenticationService = new AuthenticationService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        response.setContentType("application/json");

        String path = request.getPathInfo().substring("/auth".length());


        try {

            switch (path) {
                case "/register" -> {
                    // /auth/register
                    UserCreateRequest req = objectMapper.readValue(request.getReader(), UserCreateRequest.class);
                    UserDetails created = authenticationService.registerUser(req);

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "User registered successfully",
                                    "user_id", created.getUserId()));
                }

                // login
                case "/login" -> {


                    Map body = objectMapper.readValue(request.getReader(), Map.class);

                    String token = authenticationService.login(
                            String.valueOf(body.get("email")),
                            String.valueOf(body.get("password"))
                    );

                    objectMapper.writeValue(response.getWriter(), Map.of("token", token));
                }

                // logout
                case "/logout" -> {

                    String authHeader = request.getHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        response.setStatus(401);
                        objectMapper.writeValue(response.getWriter(), Map.of("message", "Missing token"));
                        return;
                    }

                    String token = authHeader.substring(7);
                    Claims claims = JwtUtil.validateToken(token);
                    String jti = claims.getId();

                    authenticationService.logout(jti);

                    objectMapper.writeValue(response.getWriter(),
                            Map.of("message", "Logged out successfully"));
                }


                //  refresh
                case "/refresh" -> {

                    String authHeader = request.getHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        response.setStatus(401);
                        return;
                    }

                    String token = authHeader.substring(7);
                    String newToken = authenticationService.refreshToken(token);
                    objectMapper.writeValue(response.getWriter(), Map.of("token", newToken));
                }
            }

        } catch (SQLException | RuntimeException e) {
            log.error("Error while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }catch (Exception e) {
            log.error("Error occurred while processing the request, error : ", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }
}
