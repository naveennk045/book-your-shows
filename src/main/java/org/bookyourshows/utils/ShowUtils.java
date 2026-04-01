package org.bookyourshows.utils;

import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;

import java.util.Calendar;


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

    public static void validateShowCreationAllowed(ShowCreateRequest showCreateRequest) {

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar showDate = Calendar.getInstance();
        showDate.setTime(showCreateRequest.getShowDate());
        showDate.set(Calendar.HOUR_OF_DAY, 0);
        showDate.set(Calendar.MINUTE, 0);
        showDate.set(Calendar.SECOND, 0);
        showDate.set(Calendar.MILLISECOND, 0);

        long diffInMillis = showDate.getTimeInMillis() - today.getTimeInMillis();
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

        if (diffInDays < 2) {
            throw new IllegalArgumentException("Show must be created at least 2 days before the show date");
        }
    }
}
