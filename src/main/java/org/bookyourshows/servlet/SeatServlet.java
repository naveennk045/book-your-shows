package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.dto.screen.ScreenCreateRequest;
import org.bookyourshows.dto.screen.ScreenDetails;
import org.bookyourshows.dto.seat.SeatCreateRequest;
import org.bookyourshows.service.ScreenService;
import org.bookyourshows.service.SeatService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SeatServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private final SeatService seatService;

    public SeatServlet() {
        this.seatService = new SeatService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();


        int screenId;
        String screenIdParam = request.getParameter("screen_id");
        if (screenIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Screen id required"));
            return;
        }

        try {
            screenId = Integer.parseInt(screenIdParam);
            // screenId = Integer.parseInt(part[2]);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid screen id"));
            return;
        }
        List<SeatCreateRequest> seatCreateRequest;
        try {
            seatCreateRequest = objectMapper.readValue(
                    request.getReader(),
                    new TypeReference<List<SeatCreateRequest>>() {
                    }
            );

            /*
            StringBuilder stringBuilder = new StringBuilder();
            for (SeatCreateRequest scr : seatCreateRequest) {
                stringBuilder.append(objectMapper.writeValueAsString(scr.getRowNo()));
                stringBuilder.append(",");
            }*/

            seatService.createSeat(seatCreateRequest, screenId);

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "seats created"));

        } catch (JsonProcessingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("message", e.getMessage()));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }
    }

}
