package org.bookyourshows.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.theatre.TheatreAddress;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.repository.cache.movie.MovieCacheRepository;
import org.bookyourshows.repository.cache.theatre.TheatreCacheRepository;
import org.bookyourshows.scheduler.TaskScheduler;

import java.util.List;

@WebListener
public class AppInitializer implements ServletContextListener {

    private final MovieRepository mysqlMovieRepository;
    private final TheatreRepository mysqlTheatreRepository;


    public AppInitializer() {
        this.mysqlMovieRepository = new MovieRepository();
        this.mysqlTheatreRepository = new TheatreRepository();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("App started → initializing scheduler and Redis cache");
        TaskScheduler.start();
        initializeMovieRedisCache();
        initializeTheatreRedisCache();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("App stopping → shutting down scheduler");
        TaskScheduler.stop();
    }

    private void initializeMovieRedisCache() {
        try {

            MovieCacheRepository.ensureIndex();
            System.out.println("Redis: index 'idx:movieDetails' ensured");

            MovieRepository mysqlMovieRepository = new MovieRepository();
            List<MovieDetails> movieDetails = mysqlMovieRepository.getAllMovies();
            System.out.println("MySQL: loaded " + movieDetails.size() + " movieDetails");

            MovieCacheRepository.bulkLoadMovies(movieDetails);
            System.out.println("Redis: bulk loaded " + movieDetails.size() + " movieDetails (up sert-only)");

        } catch (Exception e) {
            System.err.println("Failed to initialize Redis cache : " + e.getMessage());
        }
    }

    private void initializeTheatreRedisCache() {
        try {

            TheatreCacheRepository.ensureIndex();
            System.out.println("Redis: index 'idx:theatres' ensured");

            List<TheatreDetails> theatreDetails = mysqlTheatreRepository.getAllTheatre();
            System.out.println("MySQL: loaded " + theatreDetails.size() + " theatres");

            TheatreCacheRepository.bulkLoadTheatres(theatreDetails);
            System.out.println("Redis: bulk loaded " + theatreDetails.size() + " theatres (upsert-only)");

        } catch (Exception e) {
            System.err.println("Failed to initialize Redis cache : " + e.getMessage());
        }
    }


}