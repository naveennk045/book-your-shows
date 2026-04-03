package org.bookyourshows.repository.cache.movie;

import org.bookyourshows.dto.movie.MovieQueryParameter;
import redis.clients.jedis.search.Query;


public class MovieSearchQueryBuilder {

    public static Query buildQuery(MovieQueryParameter params) {
        StringBuilder queryStr = new StringBuilder();

        if (params.getName() != null && !params.getName().isBlank()) {
            String escaped = escapeTextQuery(params.getName());
            queryStr.append(escaped).append("* ");
        }

        if (params.getLanguage() != null && !params.getLanguage().isBlank()) {
            queryStr.append("@language:{")
                    .append(escapeTag(params.getLanguage()))
                    .append("} ");
        }

        if (params.getGenre() != null && !params.getGenre().isBlank()) {
            queryStr.append("@genre:{")
                    .append(escapeTag(params.getGenre()))
                    .append("} ");
        }

        if (params.getReleaseYear() != null) {
            queryStr.append("@release_year:[")
                    .append(params.getReleaseYear())
                    .append(" ")
                    .append(params.getReleaseYear())
                    .append("] ");
        }

        String finalQuery = queryStr.toString().trim();
        if (finalQuery.isEmpty()) {
            finalQuery = "* ";
        }

        Query query = new Query(finalQuery);

        int limit = params.getLimit() != null ? params.getLimit() : 20;
        int offset = params.getOffset() != null ? params.getOffset() : 0;
        query.limit(offset, limit);

        if ("release_date".equalsIgnoreCase(params.getSort())) {
            query.setSortBy("release_year", false); // DESC
        }

        query.returnFields(
                "movie_id", "title", "language", "genre",
                "duration", "release_date", "release_year",
                "poster_url", "trailer_url", "description", "censor_rating"
        );

        return query;
    }


    private static String escapeTextQuery(String input) {
        return input.replaceAll("[,.<>{}\\[\\]\"':;!@#$%^&*()+=/|\\\\]", "\\\\$0");
    }


    private static String escapeTag(String input) {
        return input.replaceAll("[,.<>{}\\[\\]\"':;!@#$%^&*()+=/|\\\\\\-]", "\\\\$0");
    }
}