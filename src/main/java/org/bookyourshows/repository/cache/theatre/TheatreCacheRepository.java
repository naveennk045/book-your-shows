package org.bookyourshows.repository.cache.theatre;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.mapper.TheatreMapper;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.search.*;

import java.util.*;


public class TheatreCacheRepository {

    public static void ensureIndex() {
        RedisClient redisClient = RedisManager.getClient();
        try {
            redisClient.ftInfo("idx:theatres");

        } catch (JedisException e) {
            try {
                System.out.println("Redis: index 'idx:movies' Updating....");
                Schema schema = new Schema()
                        .addTextField("theatre_name", 1.0)
                        .addTextField("city", 1.0);


                IndexDefinition def = new IndexDefinition()
                        .setPrefixes(new String[]{"theatre:"});

                redisClient.ftCreate(
                        "idx:theatres",
                        IndexOptions.defaultOptions().setDefinition(def),
                        schema
                );
            } catch (JedisException exception) {
                System.err.println("[Theatre Cache] index creation failed for theatre.z" + e.getMessage());
            }
        }
    }

    public void save(TheatreDetails theatreDetails) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "theatre:" + theatreDetails.getTheatre().getTheatreId();
            redisClient.hset(key, TheatreMapper.mapTheatreToHash(theatreDetails));
        } catch (JedisException e) {
            System.err.println("[Theatre Cache] save failed for theatre " + theatreDetails.getTheatre().getTheatreId() + ": " + e.getMessage());
        }
    }

    public void update(TheatreDetails theatreDetails) {
        save(theatreDetails);
    }

    public void delete(int theatreId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del("theatre:" + theatreId);
        } catch (JedisException e) {
            System.err.println("[Theatre Cache] delete failed for theatre " + theatreId + ": " + e.getMessage());
        }
    }

    public Optional<TheatreDetails> getById(int theatreId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            Map<String, String> fields = redisClient.hgetAll("theatre:" + theatreId);
            if (fields == null || fields.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of((TheatreMapper.mapHashMapToTheatreDetails(fields)));
        } catch (JedisException e) {
            System.err.println("[Theatre Cache] getById failed for theatre " + theatreId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<TheatreSummary> search(Integer limit,
                                       Integer offset,
                                       String theatreName,
                                       String city,
                                       String status) {

        try {
            RedisClient redisClient = RedisManager.getClient();
            Query query = TheatreQueryBuilder.buildQuery(limit, offset, theatreName, city, status);
            SearchResult result = redisClient.ftSearch("idx:theatres", query);

            List<TheatreSummary> theatreSummaries = new ArrayList<>();
            for (Document doc : result.getDocuments()) {
                Map<String, String> fields = new HashMap<>();

                for (Map.Entry<String, Object> entry : doc.getProperties()) {
                    fields.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                theatreSummaries.add(TheatreMapper.mapHashMapToTheatreSummary(fields));
            }
            return theatreSummaries;
        } catch (JedisException e) {
            System.err.println("[Theatre Cache] search failed " + e.getMessage());
        }
        return null;
    }


    public static void bulkLoadTheatres(List<TheatreDetails> theatreDetailsList) {
        try {
            for (TheatreDetails theatreDetails : theatreDetailsList) {
                saveTheatreStatic(theatreDetails);
            }
        } catch (JedisException e) {
            System.err.println("[Theatre Cache] bulkLoadTheatres failed: " + e.getMessage());
        }
    }

    private static void saveTheatreStatic(TheatreDetails theatreDetails) {
        RedisClient redisClient = RedisManager.getClient();
        String key = "theatre:" + theatreDetails.getTheatre().getTheatreId();
        redisClient.hset(key, TheatreMapper.mapTheatreToHash(theatreDetails));
    }

}
