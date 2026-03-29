package org.bookyourshows.dto.feedback.theatre;

public class TheatreFeedbackUpdateRequest {

    private Integer ratings;
    private String comment;

    public Integer getRatings() {
        return ratings;
    }

    public void setRatings(Integer ratings) {
        this.ratings = ratings;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}