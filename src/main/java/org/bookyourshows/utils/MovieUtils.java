package org.bookyourshows.utils;

import org.bookyourshows.dto.movie.MovieCreateRequest;
import org.bookyourshows.dto.movie.MovieUpdateRequest;
import org.bookyourshows.exceptions.MovieCreationException;

public class MovieUtils {


    public static void validateCreateRequest(MovieCreateRequest request) throws MovieCreationException {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new MovieCreationException("Title is required");
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new MovieCreationException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new MovieCreationException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new MovieCreationException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new MovieCreationException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new MovieCreationException("Censor rating is required");
        }
    }

    public static void validateUpdateRequest(MovieUpdateRequest request) throws MovieCreationException {
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new MovieCreationException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new MovieCreationException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new MovieCreationException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new MovieCreationException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new MovieCreationException("Censor rating is required");
        }
    }
}
