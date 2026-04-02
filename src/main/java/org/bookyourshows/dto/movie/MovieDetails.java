package org.bookyourshows.dto.movie;

import com.fasterxml.jackson.annotation.JsonView;
import org.bookyourshows.dto.Views;

import java.sql.Date;
import java.sql.Timestamp;

public class MovieDetails {

    @JsonView(Views.Public.class)
    private Integer movieId;
    @JsonView(Views.Public.class)
    private String title;
    @JsonView(Views.Public.class)
    private String language;
    @JsonView(Views.Public.class)
    private String genre;
    @JsonView(Views.Public.class)
    private Integer duration;
    @JsonView(Views.Public.class)
    private Date releaseDate;
    @JsonView(Views.Public.class)
    private String posterUrl;
    @JsonView(Views.Public.class)
    private String trailerUrl;
    @JsonView(Views.Public.class)
    private String description;
    @JsonView(Views.Public.class)
    private String censorRating;
    @JsonView(Views.Admin.class)
    private Timestamp createdAt;
    @JsonView(Views.Admin.class)
    private Timestamp updatedAt;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCensorRating() {
        return censorRating;
    }

    public void setCensorRating(String censorRating) {
        this.censorRating = censorRating;
    }
}
