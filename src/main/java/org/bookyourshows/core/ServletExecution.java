package org.bookyourshows.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ServletExecution {

    public ServletExecution() {
    }

    public void forwardRequest(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }
}
