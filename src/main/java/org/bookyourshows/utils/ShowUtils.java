package org.bookyourshows.utils;

import org.bookyourshows.dto.show.ShowCreateRequest;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.CreationException;

import java.time.*;


public class ShowUtils {


    public static void validateModificationAllowed(ShowDetails show) throws CreationException {

        java.time.LocalDateTime showTime = java.time.LocalDateTime.of(
                show.getShowDate().toLocalDate(),
                show.getStartTime().toLocalTime()
        );

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (now.isAfter(showTime)) {
            throw new CreationException("Show already started / completed");
        }


        if (java.time.Duration.between(now, showTime).toHours() < 5) {
            throw new CreationException("Cannot modify show within 5 hours of start time");
        }
    }


    public static void validateShowCreationAllowed(ShowCreateRequest showCreateRequest)
            throws CustomException {

        try {
            if (showCreateRequest.getShowDate() == null) {
                throw new CreationException("ShowDate cannot be null");
            }
            if (showCreateRequest.getStartTime() == null) {
                throw new CreationException("StartTime cannot be null");
            }

            ZoneId zone = ZoneId.of("Asia/Kolkata");

            // Correct conversion for java.sql.Date
            LocalDate showDate = showCreateRequest.getShowDate().toLocalDate();
            LocalTime startTime = showCreateRequest.getStartTime().toLocalTime();

            LocalDateTime showDateTime = LocalDateTime.of(showDate, startTime);
            LocalDateTime now = LocalDateTime.now(zone);

            Duration diff = Duration.between(now, showDateTime);

            boolean isInPast = diff.isNegative() || diff.isZero();
            boolean isMoreThan48Hours = diff.toHours() > 48;

            if (isInPast || isMoreThan48Hours) {
                throw new CreationException("Show must be scheduled within the next 48 hours");
            }

        } catch (RuntimeException e) {
            throw new RuntimeException("Error validating show creation: " + e.getMessage());
        }
    }
}
