package org.bookyourshows.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.show.ShowDetails;
import org.bookyourshows.dto.theatre.TheatreAddress;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.repository.MovieRepository;
import org.bookyourshows.repository.ShowRepository;
import org.bookyourshows.repository.TheatreRepository;
import org.bookyourshows.repository.cache.movie.MovieCacheRepository;
import org.bookyourshows.repository.cache.show.ShowCacheRepository;
import org.bookyourshows.repository.cache.theatre.TheatreCacheRepository;
import org.bookyourshows.scheduler.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@WebListener
public class AppInitializer implements ServletContextListener {

    private final MovieRepository mysqlMovieRepository;
    private final TheatreRepository mysqlTheatreRepository;
    private final ShowRepository mysqlShowRepository;
    private static final Logger log = LoggerFactory.getLogger(AppInitializer.class);


    public AppInitializer() {
        this.mysqlMovieRepository = new MovieRepository();
        this.mysqlTheatreRepository = new TheatreRepository();
        this.mysqlShowRepository = new ShowRepository();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("App started → initializing scheduler and Redis cache for databases");
        TaskScheduler.start();
        initializeMovieRedisCache();
        initializeTheatreRedisCache();
        initializeShowRedisCache();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("App stopping → shutting down scheduler");
        TaskScheduler.stop();
    }

    private void initializeMovieRedisCache() {
        try {

            MovieCacheRepository.ensureIndex();
            log.info("Redis: index 'idx:movieDetails' ensured");

            List<MovieDetails> movieDetails = mysqlMovieRepository.getAllMovies();
            log.info("MySQL: loaded {} movieDetails", movieDetails.size());

            MovieCacheRepository.bulkLoadMovies(movieDetails);
            log.info("Redis: bulk loaded {} movieDetails (upsert-only)", movieDetails.size());

        } catch (Exception e) {
            log.error("Failed to initialize Redis cache for movies, error : {}", e.getMessage());
        }
    }

    private void initializeTheatreRedisCache() {
        try {

            TheatreCacheRepository.ensureIndex();
            log.info("Redis: index 'idx:theatres' ensured");

            List<TheatreDetails> theatreDetails = mysqlTheatreRepository.getAllTheatre();
            log.info("MySQL: loaded {} theatres", theatreDetails.size());

            TheatreCacheRepository.bulkLoadTheatres(theatreDetails);
            log.info("Redis: bulk loaded {} theatres (upsert-only)", theatreDetails.size());

        } catch (Exception e) {
            log.error("Failed to initialize Redis cache for theatres, {}", e.getMessage());
        }
    }

    private void initializeShowRedisCache() {
        try {

            ShowCacheRepository.ensureIndex();
            log.info("Redis: index 'idx:shows' ensured");

            List<ShowDetails> showDetails = mysqlShowRepository.getAllShows();
            log.info("MySQL: loaded {} shows", showDetails.size());

            ShowCacheRepository.bulkLoadShows(showDetails);
            log.info("Redis: bulk loaded {} shows (upsert-only)", showDetails.size());

        } catch (Exception e) {
            log.error("Failed to initialize Redis cache for shows: {}", e.getMessage());
        }
    }


}