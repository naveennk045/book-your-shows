package org.bookyourshows.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private final ServletExecution servletExecution;
    private final ServletMapping servletMapping;
    private final ObjectMapper objectMapper;

    public DispatcherServlet() {
        servletExecution = new ServletExecution();
        servletMapping = new ServletMapping();
        objectMapper = new ObjectMapper();
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpServlet servlet = servletMapping.getServlet(request.getPathInfo());
            if (servlet != null) {
                servletExecution.forwardRequest(servlet, request, response);
            }
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("message", e.getMessage()));
        }

    }
}

