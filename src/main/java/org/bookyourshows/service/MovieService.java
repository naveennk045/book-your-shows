package org.bookyourshows.service;

import org.bookyourshows.dto.movie.*;
import org.bookyourshows.repository.MovieRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.MovieUtils.*;


public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService() {
        this.movieRepository = new MovieRepository();
    }

    public Optional<MovieDetails> getMovieById(int movieId) throws SQLException {
        return movieRepository.getMovieById(movieId);
    }

    public List<MovieSummary> getAllMovies(MovieQueryParameter query) throws SQLException {
        return movieRepository.getAllMovies(query);
    }

    public MovieDetails createMovie(MovieCreateRequest request) throws SQLException {

        validateCreateRequest(request);

        int movieId = movieRepository.createMovie(request);

        return movieRepository.getMovieById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie created but not found"));
    }

    public boolean updateMovie(int movieId, MovieUpdateRequest request) throws SQLException {

        if (movieRepository.getMovieById(movieId).isEmpty()) {
            throw new IllegalArgumentException("Movie not found");
        }

        validateUpdateRequest(request);

        boolean updated = movieRepository.updateMovie(movieId, request);

        if (!updated) {
            throw new RuntimeException("Failed to update movie");
        }

        return true;
    }

    public boolean deleteMovie(int movieId) throws SQLException {

        if (movieRepository.getMovieById(movieId).isEmpty()) {
            throw new IllegalArgumentException("Movie not found");
        }

        return movieRepository.deleteMovie(movieId);
    }

}