package org.bookyourshows.repository.cache.theatre;

import redis.clients.jedis.search.Query;

public class TheatreQueryBuilder {
    public static Query buildQuery(
            Integer limit,
            Integer offset,
            String theatreName,
            String city,
            String status
    ) {
        StringBuilder queryStr = new StringBuilder();

        if (theatreName != null && !theatreName.isBlank()) {
            queryStr.append("@theatre_name:").append(escapeTextQuery(theatreName)).append("* ");
        }

        if (city != null && !city.isBlank()) {
            queryStr.append("@city:").append(escapeTextQuery(city)).append("* ");
        }

        if (status != null && !status.isBlank()) {
            queryStr.append("@status:").append(escapeTextQuery(status)).append(" ");
        }

        String finalQuery = queryStr.toString().trim();
        if (finalQuery.isEmpty()) {
            finalQuery = "*";
        }

        Query query = new Query(finalQuery);
        query.limit(offset, limit);

        query.returnFields(
                "theatre_id", "theatre_name", "total_screens", "city"
        );

        return query;
    }

    private static String escapeTextQuery(String input) {
        return input.replaceAll("[,.<>{}\\[\\]\"':;!@#$%^&*()+=/|\\\\?-]", "\\\\$0");
    }
}
