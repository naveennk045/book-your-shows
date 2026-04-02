package org.bookyourshows.service;

import org.bookyourshows.dto.movie.*;
import org.bookyourshows.exceptions.ActionFailedException;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.cache.movie.MovieCacheRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bookyourshows.utils.MovieUtils.validateCreateRequest;
import static org.bookyourshows.utils.MovieUtils.validateUpdateRequest;


public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieCacheRepository movieCacheRepository;

    public MovieService() {
        this.movieRepository = new MovieRepository();
        this.movieCacheRepository = new MovieCacheRepository();

    }

    public Optional<MovieDetails> getMovieById(int movieId) throws SQLException {

        Optional<MovieDetails> movieDetails = movieCacheRepository.getById(movieId);
        if (movieDetails.isPresent()) {
            return movieDetails;
        }

        movieDetails = movieRepository.getMovieById(movieId);

        movieDetails.ifPresent(movieCacheRepository::save);
        return movieDetails;
    }

    public List<MovieSummary> getAllMovies(MovieQueryParameter params) throws SQLException {


        List<MovieDetails> redisResults = movieCacheRepository.search(params);

        if (!redisResults.isEmpty()) {

            List<MovieSummary> summaries = new ArrayList<>();

            for (MovieDetails m : redisResults) {
                MovieSummary summary = new MovieSummary();
                summary.setMovieId(m.getMovieId());
                summary.setTitle(m.getTitle());
                summary.setReleaseDate(m.getReleaseDate());

                summaries.add(summary);
            }
            return summaries;
        }

        return movieRepository.getAllMovies(params);
    }

    public MovieDetails createMovie(MovieCreateRequest request) throws SQLException, CustomException {

        // 1. Validate
        validateCreateRequest(request);

        int newId = movieRepository.createMovie(request);

        Optional<MovieDetails> created = movieRepository.getMovieById(newId);
        if (created.isEmpty()) {
            throw new ActionFailedException("Failed to created movie");
        }

        movieCacheRepository.save(created.get());

        return created.get();
    }


    public void updateMovie(int movieId, MovieUpdateRequest request) throws SQLException, CustomException {

        validateUpdateRequest(request);

        boolean updated = movieRepository.updateMovie(movieId, request);

        if (updated) {
            movieRepository.getMovieById(movieId).ifPresent(movieCacheRepository::update);
        }
    }

/*
    public boolean deleteMovie(int movieId) throws SQLException {

        boolean deleted = movieRepository.deleteMovie(movieId);

        if (deleted) {
            try {
                movieCacheRepository.delete(movieId);
                System.out.println("-- Created Movie by ID : " + movieId + " to movieCacheRepository -- ");
            } catch (Exception e) {
                System.err.println("[Service] Redis sync after delete failed: " + e.getMessage());
            }
        }

        return deleted;
    }
*/


}