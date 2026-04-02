package org.bookyourshows.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bookyourshows.exceptions.CustomException;
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

    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String[] parts = splitPath(request);

        try {
            // /admin/theatre/{theatre_id}/aprrove
            Integer theatreId = Integer.valueOf(parts[3]);

            if (parts.length == 5 && parts[4].equals("approve")) {
                boolean isApproved = this.adminService.updateTheatreStatus(theatreId, "APPROVED");
                if (isApproved) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre approved successfully"));
                }
            }
            // /admin/theatre/{theatre_id}/reject
            if (parts.length == 5 && parts[4].equals("reject")) {
                boolean isReject = this.adminService.updateTheatreStatus(theatreId, "REJECTED");
                if (isReject) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writeValue(response.getWriter(), Map.of("message", "Theatre rejected successfully"));
                }
            }

        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid theater_id");
        } catch (SQLException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (CustomException e) {
            writeError(response, e.getStatusCode(), e.getMessage());
        }


    }


    private String[] splitPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo.split("/") : new String[]{""};
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of("error_message", message));
    }

}
