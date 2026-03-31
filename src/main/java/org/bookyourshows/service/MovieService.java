package org.bookyourshows.service;

import org.bookyourshows.dto.movie.*;
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
    private final MovieCacheRepository cache;

    public MovieService() {
        this.movieRepository = new MovieRepository();
        this.cache = new MovieCacheRepository();

    }

    public Optional<MovieDetails> getMovieById(int movieId) throws SQLException {

        Optional<MovieDetails> cached = cache.getById(movieId);
        if (cached.isPresent()) {
            System.out.println("-- Get Movie by ID : " + movieId + " from cache -- ");
            return cached;
        }

        Optional<MovieDetails> fromDb = movieRepository.getMovieById(movieId);

        fromDb.ifPresent(movie -> {
            try {
                cache.save(movie);
            } catch (Exception e) {
                System.err.println("[Service] Redis warm after getById failed: " + e.getMessage());
            }
        });

        return fromDb;
    }

    public List<MovieSummary> getAllMovies(MovieQueryParameter params) throws SQLException {

        try {
            List<MovieDetails> redisResults = cache.search(params);

            if (!redisResults.isEmpty()) {

                List<MovieSummary> summaries = new ArrayList<>();

                for (MovieDetails m : redisResults) {
                    MovieSummary summary = new MovieSummary();
                    summary.setMovieId(m.getMovieId());
                    summary.setTitle(m.getTitle());
                    summary.setReleaseDate(m.getReleaseDate());

                    summaries.add(summary);
                }
                System.out.println("-- Get movies from to cache -- ");
                return summaries;
            }

        } catch (Exception e) {
            System.err.println("[Service] Redis Search failed, falling back to MySQL: " + e.getMessage());
        }

        return movieRepository.getAllMovies(params);
    }

    public MovieDetails createMovie(MovieCreateRequest request) throws SQLException {

        // 1. Validate
        validateCreateRequest(request);

        int newId = movieRepository.createMovie(request);

        Optional<MovieDetails> created = movieRepository.getMovieById(newId);
        if (created.isEmpty()) {
            throw new RuntimeException("Failed to retrieve created movie");
        }

        try {
            cache.save(created.get());
            System.out.println("-- Created Movie by ID : " + newId + " to cache -- ");

        } catch (Exception e) {
            System.err.println("[Service] Redis sync after create failed: " + e.getMessage());
        }

        return created.get();
    }


    public boolean updateMovie(int movieId, MovieUpdateRequest request) throws SQLException {

        validateUpdateRequest(request);

        boolean updated = movieRepository.updateMovie(movieId, request);

        if (updated) {
            try {
                movieRepository.getMovieById(movieId).ifPresent(movie -> {
                    try {
                        cache.update(movie);
                        System.out.println("-- Created Movie by ID : " + movieId + " to cache -- ");

                    } catch (Exception ex) {
                        System.err.println("[Service] Redis update failed: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("[Service] Redis sync after update failed: " + e.getMessage());
            }
        }

        return updated;
    }

    public boolean deleteMovie(int movieId) throws SQLException {

        boolean deleted = movieRepository.deleteMovie(movieId);

        if (deleted) {
            try {
                cache.delete(movieId);
                System.out.println("-- Created Movie by ID : " + movieId + " to cache -- ");
            } catch (Exception e) {
                System.err.println("[Service] Redis sync after delete failed: " + e.getMessage());
            }
        }

        return deleted;
    }


}