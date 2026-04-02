package org.bookyourshows.mapper;

import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.movie.MovieSummary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MovieMapper {

    public static MovieDetails mapRowToMovieDetail(ResultSet resultSet) throws SQLException {
        MovieDetails movieDetails = new MovieDetails();

        movieDetails.setMovieId(resultSet.getInt("movie_id"));
        movieDetails.setTitle(resultSet.getString("title"));
        movieDetails.setLanguage(resultSet.getString("language"));
        movieDetails.setGenre(resultSet.getString("genre"));
        movieDetails.setDuration(resultSet.getInt("duration"));
        movieDetails.setReleaseDate(resultSet.getDate("release_date"));
        movieDetails.setPosterUrl(resultSet.getString("poster_url"));
        movieDetails.setTrailerUrl(resultSet.getString("trailer_url"));
        movieDetails.setDescription(resultSet.getString("description"));
        movieDetails.setCensorRating(resultSet.getString("censor_rating"));
        movieDetails.setCreatedAt(resultSet.getTimestamp("created_at"));
        movieDetails.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        return movieDetails;
    }

    public static MovieSummary mapRowToMovieSummary(ResultSet resultSet) throws SQLException {
        MovieSummary movieSummary = new MovieSummary();

        movieSummary.setMovieId(resultSet.getInt("movie_id"));
        movieSummary.setTitle(resultSet.getString("title"));
        movieSummary.setReleaseDate(resultSet.getDate("release_date"));


        return movieSummary;
    }


    public static Map<String, String> mapMovieDeatilsToHashMap(MovieDetails movie) {
        Map<String, String> movieMap = new HashMap<>();

        movieMap.put("movie_id", String.valueOf(movie.getMovieId()));
        movieMap.put("title", nullSafe(movie.getTitle()));
        movieMap.put("language", nullSafe(movie.getLanguage()));
        movieMap.put("genre", nullSafe(movie.getGenre()));
        movieMap.put("duration", String.valueOf(movie.getDuration()));
        movieMap.put("censor_rating", nullSafe(movie.getCensorRating()));
        movieMap.put("poster_url", nullSafe(movie.getPosterUrl()));
        movieMap.put("trailer_url", nullSafe(movie.getTrailerUrl()));
        movieMap.put("description", nullSafe(movie.getDescription()));

        if (movie.getReleaseDate() != null) {
            movieMap.put("release_date", movie.getReleaseDate().toString());
            movieMap.put("release_year", String.valueOf(
                    movie.getReleaseDate().toLocalDate().getYear()));
        }
        movieMap.put("created_at", movie.getCreatedAt().toString());
        movieMap.put("updated_at", movie.getUpdatedAt().toString());
        return movieMap;
    }

    public static MovieDetails mapHashMaptoMovieDetails(Map<String, String> movieDetailsMap) {
        MovieDetails movieDetails = new MovieDetails();
        movieDetails.setMovieId(parseIntSafe(movieDetailsMap.get("movie_id")));
        movieDetails.setTitle(movieDetailsMap.get("title"));
        movieDetails.setLanguage(movieDetailsMap.get("language"));
        movieDetails.setGenre(movieDetailsMap.get("genre"));
        movieDetails.setDuration(parseIntSafe(movieDetailsMap.get("duration")));
        movieDetails.setCensorRating(movieDetailsMap.get("censor_rating"));
        movieDetails.setPosterUrl(movieDetailsMap.get("poster_url"));
        movieDetails.setTrailerUrl(movieDetailsMap.get("trailer_url"));
        movieDetails.setDescription(movieDetailsMap.get("description"));

        String releaseDateStr = movieDetailsMap.get("release_date");
        if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
            movieDetails.setReleaseDate(java.sql.Date.valueOf(releaseDateStr));
        }
        movieDetails.setCreatedAt(Timestamp.valueOf(movieDetailsMap.get("created_at")));
        movieDetails.setUpdatedAt(Timestamp.valueOf(movieDetailsMap.get("updated_at")));
        return movieDetails;
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}