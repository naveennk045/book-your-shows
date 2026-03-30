package org.bookyourshows.scheduler;


import org.bookyourshows.repository.ShowRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        System.out.println("Starting Seat Release");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running cleanup job");
                ShowRepository.releaseExpiredSeats();
                ShowRepository.updateCompletedShows();

            } catch (Exception e) {
                System.out.println(e.getMessage());

            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void stop() {
        System.out.println("Stopping Seat Release Scheduler");
        scheduler.shutdown();
    }
}
