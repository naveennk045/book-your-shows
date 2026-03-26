package org.bookyourshows.core;

import jakarta.servlet.http.HttpServlet;

public class ServletDetails {
    private final HttpServlet servlet;
    private final AccessLevel accessLevel;

    public ServletDetails(HttpServlet servlet,AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        this.servlet = servlet;
    }

    public HttpServlet getServlet() {
        return servlet;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }


}