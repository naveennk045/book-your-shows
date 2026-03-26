package org.bookyourshows.dto.show;

import java.util.List;

public class TheatreShowsResponse {

    private Integer theatreId;
    private List<ShowDetails> shows;

    public Integer getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Integer theatreId) {
        this.theatreId = theatreId;
    }

    public List<ShowDetails> getShows() {
        return shows;
    }

    public void setShows(List<ShowDetails> shows) {
        this.shows = shows;
    }
}
