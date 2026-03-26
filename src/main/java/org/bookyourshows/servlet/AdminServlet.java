package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.service.AdminService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class AdminServlet extends HttpServlet {

    private final ObjectMapper objectMapper;
    private final AdminService adminService;

    public AdminServlet() {
        this.adminService = new AdminService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        String[] parts = path.split("/");


        try{
            Integer theatreId = Integer.valueOf(parts[3]);

            if (parts.length == 5 && parts[4].equals("approve")) {
                boolean isApproved = this.adminService.updateTheatreStatus(theatreId,"APPROVED");
                if(isApproved){
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),Map.of("message","Theatre approved successfully"));
                }
            }
            if (parts.length == 5 && parts[4].equals("reject")) {
                boolean isReject = this.adminService.updateTheatreStatus(theatreId,"REJECTED");
                if(isReject){
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(),Map.of("message","Theatre rejected successfully"));
                }
            }

        }catch (NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Invalid theatre_id"));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }


    }
}
