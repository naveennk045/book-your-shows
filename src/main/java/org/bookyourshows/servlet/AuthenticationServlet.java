package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.user.UserCreateRequest;
import org.bookyourshows.dto.user.UserDetails;
import org.bookyourshows.service.AuthenticationService;
import org.bookyourshows.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (request.getContentType() == null ||
                !request.getContentType().contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        try {
            // /auth/register
            UserCreateRequest req = objectMapper.readValue(request.getReader(), UserCreateRequest.class);
            UserDetails created = authenticationService.registerUser(req);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", "User registered successfully",
                            "user_id", created.getUserId()));

        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }
}
