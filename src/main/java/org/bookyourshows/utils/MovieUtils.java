package org.bookyourshows.utils;

import org.bookyourshows.dto.movie.MovieCreateRequest;
import org.bookyourshows.dto.movie.MovieUpdateRequest;

public class MovieUtils {


    public static void validateCreateRequest(MovieCreateRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (request.getLanguage() == null) {
            throw new IllegalArgumentException("Language is required");
        }

        if (request.getGenre() == null) {
            throw new IllegalArgumentException("Genre is required");
        }

        if (request.getDuration() == null) {
            throw new IllegalArgumentException("Duration is required");
        }

        if (request.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
    }

    public static void validateUpdateRequest(MovieUpdateRequest request) {

        if (request.getLanguage() == null) {
            throw new IllegalArgumentException("Language is required");
        }

        if (request.getGenre() == null) {
            throw new IllegalArgumentException("Genre is required");
        }

        if (request.getDuration() == null) {
            throw new IllegalArgumentException("Duration is required");
        }

        if (request.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
    }
}
