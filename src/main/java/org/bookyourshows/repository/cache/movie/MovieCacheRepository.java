package org.bookyourshows.repository.cache.movie;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.movie.MovieDetails;
import org.bookyourshows.dto.movie.MovieQueryParameter;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.search.*;

import java.util.*;

import static org.bookyourshows.mapper.MovieMapper.mapToHashData;
import static org.bookyourshows.mapper.MovieMapper.maptoMovieDetails;

public class MovieCacheRepository {


    public static void ensureIndex() {
        RedisClient redisClient = RedisManager.getClient();
        try {
            redisClient.ftInfo("idx:movies");

        } catch (Exception e) {
            System.out.println("Redis: index 'idx:movies' Updating....");
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
        }
    }

    public static void bulkLoadMovies(List<MovieDetails> movies) {
        for (MovieDetails movie : movies) {
            saveMovieStatic(movie);
        }
    }

    public void save(MovieDetails movie) {
        try {
            saveMovieStatic(movie);
        } catch (Exception e) {
            System.err.println("[Cache] save failed for movie " + movie.getMovieId() + ": " + e.getMessage());
        }
    }

    public void update(MovieDetails movie) {
        save(movie);
    }

    public void delete(int movieId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del("movie:" + movieId);
        } catch (Exception e) {
            System.err.println("[Cache] delete failed for movie " + movieId + ": " + e.getMessage());
        }
    }


    public Optional<MovieDetails> getById(int movieId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            Map<String, String> fields = redisClient.hgetAll("movie:" + movieId);
            if (fields == null || fields.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(maptoMovieDetails(fields));
        } catch (Exception e) {
            System.err.println("[Cache] getById failed for movie " + movieId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<MovieDetails> search(MovieQueryParameter params) {

        RedisClient redisClient = RedisManager.getClient();

        Query query = MovieSearchQueryBuilder.buildQuery(params);

        SearchResult result = redisClient.ftSearch("idx:movies", query);

        List<MovieDetails> movieDetailsList = new ArrayList<>();

        for (Document doc : result.getDocuments()) {
            Map<String, String> fields = new HashMap<>();

            for (Map.Entry<String, Object> entry : doc.getProperties()) {
                fields.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            MovieDetails movieDetails = maptoMovieDetails(fields);
            movieDetailsList.add(movieDetails);
        }

        return movieDetailsList;
    }

    private static void saveMovieStatic(MovieDetails movie) {
        RedisClient redisClient = RedisManager.getClient();
        String key = "movie:" + movie.getMovieId();
        redisClient.hset(key, mapToHashData(movie));
    }


}