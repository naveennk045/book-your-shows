package org.bookyourshows.listener;


import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.bookyourshows.scheduler.TaskScheduler;

public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("App started → initializing scheduler");
        TaskScheduler.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("App stopping → shutting down scheduler");
        TaskScheduler.stop();
    }
}