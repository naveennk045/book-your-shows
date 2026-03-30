package org.bookyourshows.core;

import jakarta.servlet.http.HttpServlet;

import java.util.Map;

public class ServletDetails {
    private final HttpServlet servlet;
    private final AccessLevel defaultAccessLevel;
    private final Map<String, AccessLevel> methodAccessLevels; // e.g. GET/POST/PUT/DELETE

    public ServletDetails(HttpServlet servlet,
                          AccessLevel defaultAccessLevel,
                          Map<String, AccessLevel> methodAccessLevels) {
        this.servlet = servlet;
        this.defaultAccessLevel = defaultAccessLevel;
        this.methodAccessLevels = methodAccessLevels;
    }

    public ServletDetails(HttpServlet servlet, AccessLevel defaultAccessLevel) {
        this(servlet, defaultAccessLevel, null);
    }

    public HttpServlet getServlet() {
        return servlet;
    }

    public AccessLevel getAccessLevel(String httpMethod) {
        if (methodAccessLevels == null) {
            return defaultAccessLevel;
        }
        return methodAccessLevels.getOrDefault(httpMethod, defaultAccessLevel);
    }
}