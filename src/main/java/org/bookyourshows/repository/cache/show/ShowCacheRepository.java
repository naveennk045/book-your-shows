package org.bookyourshows.repository.cache.show;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.show.ShowDetails;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.search.*;

import java.sql.Date;
import java.util.*;

import static org.bookyourshows.mapper.ShowMapper.mapHashToShowDetails;
import static org.bookyourshows.mapper.ShowMapper.mapShowToHashData;


public class ShowCacheRepository {


    public static void ensureIndex() {
        RedisClient redisClient = RedisManager.getClient();
        try {
            redisClient.ftInfo("idx:shows");

        } catch (Exception e) {
            System.out.println("Redis: index 'idx:shows' Updating....");

            Schema schema = new Schema()
                    .addTextField("theatre_location", 1.0)
                    .addNumericField("show_date")
                    .addNumericField("movie_id")
                    .addNumericField("theatre_id")
                    .addNumericField("start_time")
                    .addNumericField("end_time");

            IndexDefinition def = new IndexDefinition()
                    .setPrefixes(new String[]{"show:"});

            redisClient.ftCreate(
                    "idx:shows",
                    IndexOptions.defaultOptions().setDefinition(def),
                    schema
            );
        }
    }

    public static void bulkLoadShows(List<ShowDetails> showDetails) {
        for (ShowDetails showDetail : showDetails) {
            saveShowStatic(showDetail);
        }
    }

    public void save(ShowDetails showDetails) {
        try {
            saveShowStatic(showDetails);
        } catch (Exception e) {
            System.err.println("[Cache] save failed for shows " + showDetails.getShowId() + ": " + e.getMessage());
        }
    }

    public void update(ShowDetails showDetails) {
        save(showDetails);
    }

    public void delete(int showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del("show:" + showId);
        } catch (Exception e) {
            System.err.println("[Cache] delete failed for shows " + showId + ": " + e.getMessage());
        }
    }


    public Optional<ShowDetails> getById(int showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            Map<String, String> fields = redisClient.hgetAll("show:" + showId);
            if (fields == null || fields.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapHashToShowDetails(fields));
        } catch (Exception e) {
            System.err.println("[Cache] getById failed for show " + showId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<ShowDetails> search(Integer theatreId, String location, Date showDate, int movieId) {

        List<ShowDetails> showList = new ArrayList<>();
        try {
            RedisClient redisClient = RedisManager.getClient();
            Query query = ShowSearchQueryBuilder.buildQuery(theatreId, location, showDate, movieId);

            SearchResult result = redisClient.ftSearch("idx:shows", query);

            for (Document doc : result.getDocuments()) {
                Map<String, String> fields = new HashMap<>();
                for (Map.Entry<String, Object> entry : doc.getProperties()) {
                    fields.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                ShowDetails showDetails = mapHashToShowDetails(fields);
                showList.add(showDetails);
            }

        } catch (Exception e) {

            System.err.println("[Cache] Search failed: " + e.getMessage());
        }
        return showList;
    }


    private static void saveShowStatic(ShowDetails showDetail) {
        RedisClient redisClient = RedisManager.getClient();
        String key = "show:" + showDetail.getShowId();
        redisClient.hset(key, mapShowToHashData(showDetail));
    }
}
