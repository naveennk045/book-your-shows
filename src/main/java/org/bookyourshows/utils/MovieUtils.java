package org.bookyourshows.utils;

import org.bookyourshows.dto.movie.MovieCreateRequest;
import org.bookyourshows.dto.movie.MovieUpdateRequest;

public class MovieUtils {


    public static void validateCreateRequest(MovieCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new IllegalArgumentException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new IllegalArgumentException("Censor rating is required");
        }
    }

    public static void validateUpdateRequest(MovieUpdateRequest request) {
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Language is required");
        }
        if (request.getGenre() == null || request.getGenre().isBlank()) {
            throw new IllegalArgumentException("Genre is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be a positive number");
        }
        if (request.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
        if (request.getCensorRating() == null || request.getCensorRating().isBlank()) {
            throw new IllegalArgumentException("Censor rating is required");
        }
    }
}
