package org.bookyourshows.dto.analytics;

public class MoviePerformanceResponse {

    private Integer movieId;
    private String movieName;
    private Integer noOfShows;
    private Integer seatsBooked;
    private Integer seatsNotBooked;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Integer getNoOfShows() {
        return noOfShows;
    }

    public void setNoOfShows(Integer noOfShows) {
        this.noOfShows = noOfShows;
    }

    public Integer getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(Integer seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public Integer getSeatsNotBooked() {
        return seatsNotBooked;
    }

    public void setSeatsNotBooked(Integer seatsNotBooked) {
        this.seatsNotBooked = seatsNotBooked;
    }
}
