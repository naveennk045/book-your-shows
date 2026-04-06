package org.bookyourshows.utils;

import org.bookyourshows.dto.movie.MovieCreateRequest;
import org.bookyourshows.dto.movie.MovieUpdateRequest;
import org.bookyourshows.exceptions.CreationException;

public class MovieUtils {


    public static void validateCreateRequest(MovieCreateRequest request) throws CreationException {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CreationException("Title is required");
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new CreationException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new CreationException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new CreationException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new CreationException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new CreationException("Censor rating is required");
        }
    }

    public static void validateUpdateRequest(MovieUpdateRequest request) throws CreationException {
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new CreationException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new CreationException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new CreationException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new CreationException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new CreationException("Censor rating is required");
        }
    }
}
