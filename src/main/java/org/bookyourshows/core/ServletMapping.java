package org.bookyourshows.core;

import org.bookyourshows.servlet.*;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("StringTemplateMigration")
public class ServletMapping {

    private static final Map<String, ServletDetails> SERVLET_REGISTRY = new LinkedHashMap<>();

    static {

        // /theatres
        SERVLET_REGISTRY.put("^/theatres$", new ServletDetails(
                new TheatreServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

        SERVLET_REGISTRY.put("^/theatres/$", new ServletDetails(
                new TheatreServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

// /theatres/{id}
        SERVLET_REGISTRY.put("^/theatres/[^/]+$", new ServletDetails(
                new TheatreServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "PUT", AccessLevel.THEATRE_OWNER,    // update theatre
                        "DELETE", AccessLevel.THEATRE_OWNER  // delete theatre
                )
        ));


        // /theatres/{id}/screens
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens$", new ServletDetails(
                new ScreenServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/$", new ServletDetails(
                new ScreenServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

// /theatres/{id}/screens/{screenId}
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+$", new ServletDetails(
                new ScreenServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "PUT", AccessLevel.THEATRE_OWNER,
                        "DELETE", AccessLevel.THEATRE_OWNER
                )
        ));

        // /theatres/{id}/screens/{screenId}/shows
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/shows$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER     // create show
                )
        ));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/shows/$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

// /shows
        SERVLET_REGISTRY.put("^/shows$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER
                )
        ));

        SERVLET_REGISTRY.put("^/shows/$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER
                )
        ));

// /shows/{id}
        SERVLET_REGISTRY.put("^/shows/[^/]+$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "PUT", AccessLevel.THEATRE_OWNER,
                        "DELETE", AccessLevel.THEATRE_OWNER
                )
        ));

// /shows/{id}/seats
        SERVLET_REGISTRY.put("^/shows/[^/]+/seats$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER
                )
        ));

        SERVLET_REGISTRY.put("^/shows/[^/]+/seats/$", new ServletDetails(
                new ShowServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER
                )
        ));

        // /theatres/{id}/screens/{screenId}/seats
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats/$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

        // /theatres/{id}/screens/{screenId}/seats/{seatId}
        SERVLET_REGISTRY.put("^/theatres/[^/]+/screens/[^/]+/seats/[^/]+$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "PUT", AccessLevel.THEATRE_OWNER,      // update seat
                        "DELETE", AccessLevel.THEATRE_OWNER    // delete seat
                )
        ));

        // /seats and /seats/{id}
        SERVLET_REGISTRY.put("^/seats$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

        SERVLET_REGISTRY.put("^/seats/$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "POST", AccessLevel.THEATRE_OWNER
                )
        ));

        SERVLET_REGISTRY.put("^/seats/[^/]+$", new ServletDetails(
                new SeatServlet(),
                AccessLevel.CUSTOMER,
                Map.of(
                        "GET", AccessLevel.CUSTOMER,
                        "PUT", AccessLevel.THEATRE_OWNER,
                        "DELETE", AccessLevel.THEATRE_OWNER
                )
        ));


        SERVLET_REGISTRY.put("^/movies$", new ServletDetails(
                new MovieServlet(),
                AccessLevel.PUBLIC,
                Map.of(
                        "GET", AccessLevel.PUBLIC,
                        "POST", AccessLevel.ADMIN
                )
        ));

        SERVLET_REGISTRY.put("^/movies/$", new ServletDetails(
                new MovieServlet(),
                AccessLevel.PUBLIC,
                Map.of(
                        "GET", AccessLevel.PUBLIC,
                        "POST", AccessLevel.ADMIN
                )
        ));

        // /movies/{id}
        SERVLET_REGISTRY.put("^/movies/[^/]+$", new ServletDetails(
                new MovieServlet(),
                AccessLevel.PUBLIC,
                Map.of(
                        "GET", AccessLevel.PUBLIC,
                        "PUT", AccessLevel.ADMIN,
                        "DELETE", AccessLevel.ADMIN
                )
        ));


        SERVLET_REGISTRY.put("^/users/$", new ServletDetails(new UserServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/users$", new ServletDetails(new UserServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/users/[^/]+$", new ServletDetails(new UserServlet(), AccessLevel.CUSTOMER));


        SERVLET_REGISTRY.put("^/bookings$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/payments$", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/payments$/", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/status$/", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/status$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/cancel$/", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/bookings/[^/]+/cancel$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));

        SERVLET_REGISTRY.put("^/users/[^/]+/bookings$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/users/[^/]+/bookings/$", new ServletDetails(new BookingServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/bookings$", new ServletDetails(new BookingServlet(), AccessLevel.THEATRE_OWNER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/bookings/$", new ServletDetails(new BookingServlet(), AccessLevel.THEATRE_OWNER));

        SERVLET_REGISTRY.put("^/fluxpay/[^/]+", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/fluxpay/[^/]+/", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));


        SERVLET_REGISTRY.put("^/users/[^/]+/address$", new ServletDetails(new UserServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/users/[^/]+/address/$", new ServletDetails(new UserServlet(), AccessLevel.CUSTOMER));

        SERVLET_REGISTRY.put("^/theatres/[^/]+/address$", new ServletDetails(new TheatreServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/address/$", new ServletDetails(new TheatreServlet(), AccessLevel.CUSTOMER));


        SERVLET_REGISTRY.put("^/theatres/[^/]+/feedbacks$", new ServletDetails(new TheatreFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/feedbacks/$", new ServletDetails(new TheatreFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/feedbacks/[^/]+$", new ServletDetails(new TheatreFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/theatres/[^/]+/feedbacks/[^/]+/$", new ServletDetails(new TheatreFeedbackServlet(), AccessLevel.CUSTOMER));

        SERVLET_REGISTRY.put("^/movies/[^/]+/feedbacks$", new ServletDetails(new MovieFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/movies/[^/]+/feedbacks/$", new ServletDetails(new MovieFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/movies/[^/]+/feedbacks/[^/]+$", new ServletDetails(new MovieFeedbackServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/movies/[^/]+/feedbacks/[^/]+/$", new ServletDetails(new MovieFeedbackServlet(), AccessLevel.CUSTOMER));


        SERVLET_REGISTRY.put("^/auth/register$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/auth/register/$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/auth/login$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/auth/login/$", new ServletDetails(new AuthenticationServlet(), AccessLevel.PUBLIC));
        SERVLET_REGISTRY.put("^/auth/refresh$", new ServletDetails(new AuthenticationServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/auth/refresh/$", new ServletDetails(new AuthenticationServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/auth/logout$", new ServletDetails(new AuthenticationServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/auth/logout/$", new ServletDetails(new AuthenticationServlet(), AccessLevel.CUSTOMER));

        // Admin routes

        SERVLET_REGISTRY.put("^/refunds/[^/]+$", new ServletDetails(new RefundServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/refunds/[^/]+$/", new ServletDetails(new RefundServlet(), AccessLevel.CUSTOMER));

        SERVLET_REGISTRY.put("^/payments/[^/]+$", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));
        SERVLET_REGISTRY.put("^/payments/[^/]+/$", new ServletDetails(new PaymentServlet(), AccessLevel.CUSTOMER));

        SERVLET_REGISTRY.put("^/refunds/$", new ServletDetails(new RefundServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/refunds$", new ServletDetails(new RefundServlet(), AccessLevel.ADMIN));

        SERVLET_REGISTRY.put("^/payments/$", new ServletDetails(new PaymentServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/payments$", new ServletDetails(new PaymentServlet(), AccessLevel.ADMIN));


        SERVLET_REGISTRY.put("^/admin/theatres/[^/]+/approve$", new ServletDetails(new AdminServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/admin/theatres/[^/]+/reject$", new ServletDetails(new AdminServlet(), AccessLevel.ADMIN));

        SERVLET_REGISTRY.put("^/analytics/movie-performance$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/analytics/movie-performance/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));

        SERVLET_REGISTRY.put("^/analytics/peak-show-times$", new ServletDetails(new AnalyticsServlet(), AccessLevel.THEATRE_OWNER));
        SERVLET_REGISTRY.put("^/analytics/peak-show-times/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.THEATRE_OWNER));

        SERVLET_REGISTRY.put("^/analytics/users-bookings$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/analytics/users-bookings/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));

        SERVLET_REGISTRY.put("^/analytics/theatres-bookings$", new ServletDetails(new AnalyticsServlet(), AccessLevel.THEATRE_OWNER));
        SERVLET_REGISTRY.put("^/analytics/theatres-bookings/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.THEATRE_OWNER));

        SERVLET_REGISTRY.put("^/analytics/top-spent$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/analytics/top-spent/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));

        SERVLET_REGISTRY.put("^/analytics/theatres-revenue$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));
        SERVLET_REGISTRY.put("^/analytics/theatres-revenue/$", new ServletDetails(new AnalyticsServlet(), AccessLevel.ADMIN));

    }


    public ServletDetails getServlet(String requestUri) {
        if (requestUri == null) return null;

        for (Map.Entry<String, ServletDetails> entry : SERVLET_REGISTRY.entrySet()) {
            if (requestUri.matches(entry.getKey())) {
//                System.out.println("Matched request URI: " + entry.getKey());
                return entry.getValue();
            }
        }
        throw new RuntimeException("No servlet found for request URI: " + requestUri);
    }
}