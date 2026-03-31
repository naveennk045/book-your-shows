package org.bookyourshows.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.cache.MovieCacheRepository;
import org.bookyourshows.scheduler.TaskScheduler;

import java.util.List;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("App started → initializing scheduler and Redis cache");
        TaskScheduler.start();
        initializeRedisCache();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("App stopping → shutting down scheduler");
        TaskScheduler.stop();
    }

    private void initializeRedisCache() {
        try {

            MovieCacheRepository.ensureIndex();
            System.out.println("Redis: index 'idx:movies' ensured");

            MovieRepository mysqlMovieRepository = new MovieRepository();
            List<MovieDetails> movies = mysqlMovieRepository.getAllMovies();
            System.out.println("MySQL: loaded " + movies.size() + " movies");

            MovieCacheRepository.bulkLoadMovies(movies);
            System.out.println("Redis: bulk loaded " + movies.size() + " movies (up sert-only)");

        } catch (Exception e) {
            System.err.println("Failed to initialize Redis cache : " + e.getMessage());
        }
    }
}