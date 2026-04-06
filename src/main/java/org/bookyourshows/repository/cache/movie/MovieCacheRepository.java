package org.bookyourshows.repository.cache.movie;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.movie.MovieQueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.search.*;

import java.util.*;

import static org.bookyourshows.mapper.MovieMapper.mapMovieDeatilsToHashMap;
import static org.bookyourshows.mapper.MovieMapper.mapHashMaptoMovieDetails;

public class MovieCacheRepository {

    private static final Logger log = LoggerFactory.getLogger(MovieCacheRepository.class);


    public static void ensureIndex() {
        RedisClient redisClient = RedisManager.getClient();
        try {
            redisClient.ftInfo("idx:movies");
        } catch (Exception e) {
            log.info("Redis: index 'idx:movies' Updating....");
            try {
                Schema schema = new Schema()
                        .addTextField("title", 1.0)
                        .addTagField("language")
                        .addTagField("genre")
                        .addNumericField("release_year")
                        .addNumericField("duration")
                        .addTagField("censor_rating");

                IndexDefinition def = new IndexDefinition()
                        .setPrefixes(new String[]{"movie:"});

                redisClient.ftCreate(
                        "idx:movies",
                        IndexOptions.defaultOptions().setDefinition(def),
                        schema
                );
            } catch (JedisException je) {
                log.error("[Movie Cache] : {}", je.getMessage());
            }
        }
    }

    public static void bulkLoadMovies(List<MovieDetails> movieDetailsList) {
        for (MovieDetails movieDetails : movieDetailsList) {
            saveMovieStatic(movieDetails);
        }
    }

    public void save(MovieDetails movieDetails) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "movie:" + movieDetails.getMovieId();
            redisClient.hset(key, mapMovieDeatilsToHashMap(movieDetails));
        } catch (JedisException e) {
            log.error("[Movie Cache] save failed for movie : {}, \n error : {}", movieDetails.getMovieId(), e.getMessage());
        }
    }

    public void update(MovieDetails movieDetails) {
        save(movieDetails);
    }

    public void delete(int movieId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del("movie:" + movieId);
        } catch (JedisException e) {
            log.error("[Movie Cache] delete failed for movie {}, \n error : {}", movieId, e.getMessage());
        }
    }


    public Optional<MovieDetails> getById(int movieId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            Map<String, String> fields = redisClient.hgetAll("movie:" + movieId);
            if (fields == null || fields.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapHashMaptoMovieDetails(fields));
        } catch (JedisException e) {
            log.error("[Movie Cache] getById failed for movie {}, \n error : {}", movieId, e.getMessage());
            return Optional.empty();
        }
    }

    public List<MovieDetails> search(MovieQueryParameter params) {

        try {
            RedisClient redisClient = RedisManager.getClient();

            Query query = MovieSearchQueryBuilder.buildQuery(params);

            SearchResult result = redisClient.ftSearch("idx:movies", query);

            List<MovieDetails> movieDetailsList = new ArrayList<>();

            for (Document doc : result.getDocuments()) {
                Map<String, String> fields = new HashMap<>();

                for (Map.Entry<String, Object> entry : doc.getProperties()) {
                    fields.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                MovieDetails movieDetails = mapHashMaptoMovieDetails(fields);
                movieDetailsList.add(movieDetails);
            }
            return movieDetailsList;

        } catch (JedisException e) {
            log.error("[Movie Cache] search failed for movie.\n error :  {}", e.getMessage());
        }
        return null;
    }

    private static void saveMovieStatic(MovieDetails movieDetails) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "movie:" + movieDetails.getMovieId();
            redisClient.hset(key, mapMovieDeatilsToHashMap(movieDetails));
        } catch (Exception e) {
            log.error("[Movie Cache ] save failed for movie {}, \n error : {}", movieDetails.getMovieId(), e.getMessage());
        }
    }
}