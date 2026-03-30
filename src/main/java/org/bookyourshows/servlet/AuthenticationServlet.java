package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.service.AuthenticationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class AuthenticationServlet extends HttpServlet {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

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

            if (path.equals("/register")) {
                // /auth/register
                UserCreateRequest req = objectMapper.readValue(request.getReader(), UserCreateRequest.class);
                UserDetails created = authenticationService.registerUser(req);

                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "User registered successfully",
                                "user_id", created.getUserId()));
            }

            // LOGIN
            else if (path.equals("/login")) {


                Map<String, String> body = objectMapper.readValue(request.getReader(), Map.class);

                String token = authenticationService.login(
                        body.get("email"),
                        body.get("password")
                );

                objectMapper.writeValue(response.getWriter(), Map.of("token", token));
            }

            // LOGIN
            else if (path.equals("/logout")) {


                Map<String, String> body = objectMapper.readValue(request.getReader(), Map.class);

                String token = authenticationService.login(
                        body.get("email"),
                        body.get("password")
                );

                objectMapper.writeValue(response.getWriter(), Map.of("token", token));
            }

            //  REFRESH
            else if (path.equals("/refresh")) {

                String header = request.getHeader("Authorization");

                if (header == null || !header.startsWith("Bearer ")) {
                    response.setStatus(401);
                    return;
                }

                String token = header.substring(7);

                String newToken = authenticationService.refreshToken(token);

                objectMapper.writeValue(response.getWriter(), Map.of("token", newToken));
            }

        } catch (RuntimeException e) {
            response.setStatus(400);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(500);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }
}
