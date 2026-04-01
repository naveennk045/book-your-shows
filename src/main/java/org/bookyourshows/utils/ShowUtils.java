package org.bookyourshows.utils;

import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.exceptions.ShowCreationException;

import java.util.Calendar;


public class ShowUtils {


    public static void validateModificationAllowed(ShowDetails show) throws ShowCreationException {

        java.time.LocalDateTime showTime = java.time.LocalDateTime.of(
                show.getShowDate().toLocalDate(),
                show.getStartTime().toLocalTime()
        );

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (now.isAfter(showTime)) {
            throw new ShowCreationException("Show already started / completed");
        }


        if (java.time.Duration.between(now, showTime).toHours() < 5) {
            throw new ShowCreationException("Cannot modify show within 5 hours of start time");
        }
    }

    public static void validateShowCreationAllowed(ShowCreateRequest showCreateRequest) throws ShowCreationException {

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

        if (diffInDays < 1 || diffInDays > 2) {
            throw new ShowCreationException("Show can only be created for the next two days");
        }
    }
}
