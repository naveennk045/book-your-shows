package org.bookyourshows.core;

import jakarta.servlet.http.HttpServlet;
import org.bookyourshows.servlet.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServletMapping {

    private static final Map<String, HttpServlet> SERVLET_REGISTRY = new LinkedHashMap<>();

    static {

        SERVLET_REGISTRY.put("^/theatres$", new TheatreServlet());
        SERVLET_REGISTRY.put("^/theatres/$", new TheatreServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]$", new TheatreServlet());

        SERVLET_REGISTRY.put("^/theatres/[^/]/screens$", new ScreenServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/$", new ScreenServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]$", new ScreenServlet());

        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]/shows$", new ShowServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]/shows/$", new ShowServlet());
        SERVLET_REGISTRY.put("^/shows$", new ShowServlet());
        SERVLET_REGISTRY.put("^/shows/$", new ShowServlet());
        SERVLET_REGISTRY.put("^/shows/[^/]$", new ShowServlet());

        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]/seats$", new SeatServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]/seats/$", new SeatServlet());
        SERVLET_REGISTRY.put("^/theatres/[^/]/screens/[^/]/seats/[^/]$", new SeatServlet());
        SERVLET_REGISTRY.put("^/shows/[^/]/seats$", new SeatServlet());
        SERVLET_REGISTRY.put("^/shows/[^/]/seats/$", new SeatServlet());

        SERVLET_REGISTRY.put("^/movies$", new MovieServlet());
        SERVLET_REGISTRY.put("^/movies/$", new MovieServlet());
        SERVLET_REGISTRY.put("^/movies/[^/]$", new MovieServlet());

        SERVLET_REGISTRY.put("^/seats/$", new SeatServlet());
        SERVLET_REGISTRY.put("^/seats$", new SeatServlet());


    }

    public HttpServlet getServlet(String requestUri) {
        if (requestUri == null) return null;

        for (Map.Entry<String, HttpServlet> entry : SERVLET_REGISTRY.entrySet()) {
            if (requestUri.matches(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new RuntimeException("No servlet found for request URI: " + requestUri);
    }
}