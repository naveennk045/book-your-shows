package org.bookyourshows.repository.cache.theatre;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.theatre.TheatreDetails;
import org.bookyourshows.dto.theatre.TheatreSummary;
import org.bookyourshows.mapper.TheatreMapper;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.search.*;

import java.util.*;


public class TheatreCacheRepository {

    public static void ensureIndex() {
        RedisClient redisClient = RedisManager.getClient();
        try {
            redisClient.ftInfo("idx:theatres");

        } catch (Exception e) {
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
        }
    }

    public void save(TheatreDetails theatreDetails) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "theatre:" + theatreDetails.getTheatre().getTheatreId();
            redisClient.hset(key, TheatreMapper.mapTheatreToHash(theatreDetails));
        } catch (Exception e) {
            System.err.println("[Cache] save failed for movie " + theatreDetails.getTheatre().getTheatreId() + ": " + e.getMessage());
        }
    }

    public void update(TheatreDetails theatreDetails) {
        save(theatreDetails);
    }

    public void delete(int theatreId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del("theatre:" + theatreId);
        } catch (Exception e) {
            System.err.println("[Cache] delete failed for movie " + theatreId + ": " + e.getMessage());
        }
    }

    public Optional<TheatreDetails> getById(int theatreId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            Map<String, String> fields = redisClient.hgetAll("theatre:" + theatreId);
            if (fields == null || fields.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of((TheatreMapper.mapHashToTheatreDetails(fields)));
        } catch (Exception e) {
            System.err.println("[Cache] getById failed for theatre " + theatreId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<TheatreSummary> search(Integer limit,
                                       Integer offset,
                                       String theatreName,
                                       String city,
                                       String status) {

        RedisClient redisClient = RedisManager.getClient();

        Query query = TheatreQueryBuilder.buildQuery(limit, offset, theatreName, city, status);

        SearchResult result = redisClient.ftSearch("idx:theatres", query);

        List<TheatreSummary> theatreSummaries = new ArrayList<>();

        for (Document doc : result.getDocuments()) {
            Map<String, String> fields = new HashMap<>();

            for (Map.Entry<String, Object> entry : doc.getProperties()) {
                fields.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            theatreSummaries.add(TheatreMapper.mapHashToTheatreSummary(fields));
        }
        return theatreSummaries;
    }


    public static void bulkLoadTheatres(List<TheatreDetails> theatreDetailsList) {
        for (TheatreDetails theatreDetails : theatreDetailsList) {
            saveTheatreStatic(theatreDetails);
        }
    }

    private static void saveTheatreStatic(TheatreDetails theatreDetails) {
        RedisClient redisClient = RedisManager.getClient();
        String key = "theatreDetails:" + theatreDetails.getTheatre().getTheatreId();
        redisClient.hset(key, TheatreMapper.mapTheatreToHash(theatreDetails));
    }

}
