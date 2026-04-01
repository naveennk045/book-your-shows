package org.bookyourshows.repository.cache.show;

import redis.clients.jedis.search.Query;

import java.util.Date;

public class ShowSearchQueryBuilder {

    public static Query buildQuery(
            Integer theatreId,
            String location,
            Date showDate,
            Integer movieId
            ) {
        StringBuilder queryStr = new StringBuilder();

        if (location != null && !location.isBlank()) {
            String escaped = escapeTextQuery(location);
            queryStr.append("@theatre_location:").append(escaped).append("* ");
        }

        if (theatreId != null) {
            queryStr.append("@theatre_id:[").append(theatreId)
                    .append(" ").append(theatreId).append("] ");
        }

        if (movieId != null) {
            queryStr.append("@movie_id:[").append(movieId)
                    .append(" ").append(movieId).append("] ");
        }

        if (showDate != null) {
            long millis = showDate.getTime();
            queryStr.append("@show_date:[").append(millis).append(" ").append(millis).append("] ");
        }

        String finalQuery = queryStr.toString().trim();
        if (finalQuery.isEmpty()) {
            finalQuery = "*";
        }

        Query query = new Query(finalQuery);

        query.setSortBy("start_time", true);

        query.returnFields(
                "show_id", "theatre_id", "theatre_location",
                "screen_id", "screen_name", "movie_id",
                "movie_name", "show_date", "start_time",
                "end_time", "base_price", "status"
        );

        return query;
    }


    private static String escapeTextQuery(String input) {
        return input.replaceAll("[,.<>{}\\[\\]\"':;!@#$%^&*()+=/|\\\\?-]", "\\\\$0");
    }
}