package org.bookyourshows.core;

import org.bookyourshows.servlet.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServletMapping {

    private static final Map<String, ServletDetails> SERVLET_REGISTRY = new LinkedHashMap<>();

    static {

        SERVLET_REGISTRY.put("^/theatres$", new ServletDetails(new TheatreServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/$", new ServletDetails(new TheatreServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+$", new ServletDetails(new TheatreServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens$", new ServletDetails(new ScreenServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/$", new ServletDetails(new ScreenServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+$", new ServletDetails(new ScreenServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/shows$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/shows/$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/shows$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/shows/$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/shows/[^/]+$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats/$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats/[^/]+$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/seats/$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/seats/[^/]+$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/seats$", new ServletDetails(new SeatServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/movies$", new ServletDetails(new MovieServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/movies/$", new ServletDetails(new MovieServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/movies/[^/]+$", new ServletDetails(new MovieServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/shows/[^/]+/seats$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/shows/[^/]+/seats/$", new ServletDetails(new ShowServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/users/$", new ServletDetails(new UserServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/users$", new ServletDetails(new UserServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/users/[^/]+$", new ServletDetails(new UserServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/auth/register$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/auth/register/$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/bookings$", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/$", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+$", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/payments$", new ServletDetails(new PaymentServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/payments$/", new ServletDetails(new PaymentServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/status$/", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/status$", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/cancel$/", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/cancel$", new ServletDetails(new BookingServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/fluxpay/[^/]+", new ServletDetails(new PaymentServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/fluxpay/[^/]+/", new ServletDetails(new PaymentServlet(), AccessLevel.PUBLIC));

        SERVLET_REGISTRY.put("^/refunds/[^/]+", new ServletDetails(new RefundServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/refunds/[^/]+/", new ServletDetails(new RefundServlet(), AccessLevel.PUBLIC));

        // Admin routes
        SERVLET_REGISTRY.put("^/admin/theatres/[^/]+/approve$", new ServletDetails(new AdminServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/admin/theatres/[^/]+/reject$", new ServletDetails(new AdminServlet(), AccessLevel.PUBLIC));

    }


    public ServletDetails getServlet(String requestUri) {
        if (requestUri == null) return null;

        for (Map.Entry<String, ServletDetails> entry : SERVLET_REGISTRY.entrySet()) {
            if (requestUri.matches(entry.getKey())) {
                System.out.println("Matched request URI: " + entry.getKey());
                return entry.getValue();
            }
        }
        throw new RuntimeException("No servlet found for request URI: " + requestUri);
    }
}