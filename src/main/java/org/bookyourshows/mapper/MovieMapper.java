package org.bookyourshows.mapper;

import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.movie.MovieSummary;

import java.sql.ResultSet;
import java.sql.SQLException;

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

        return movieDetails;
    }

    public static MovieSummary mapRowToMovieSummary(ResultSet resultSet) throws SQLException {
        MovieSummary movieSummary = new MovieSummary();

        movieSummary.setMovieId(resultSet.getInt("movie_id"));
        movieSummary.setTitle(resultSet.getString("title"));
        movieSummary.setReleaseDate(resultSet.getDate("release_date"));


        return movieSummary;
    }
}