package org.bookyourshows.utils;

import org.bookyourshows.dto.show.ShowDetails;

public class ShowUtils {

    public static void validateModificationAllowed(ShowDetails show) {

        java.time.LocalDateTime showTime = java.time.LocalDateTime.of(
                show.getShowDate().toLocalDate(),
                show.getStartTime().toLocalTime()
        );

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (now.isAfter(showTime)) {
            throw new RuntimeException("Show already started / completed");
        }

        if (java.time.Duration.between(now, showTime).toHours() < 5) {
            throw new RuntimeException("Cannot modify show within 5 hours of start time");
        }
    }
}
