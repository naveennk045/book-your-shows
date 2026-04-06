package org.bookyourshows.repository.cache.show;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.show.ShowSeating;
import org.bookyourshows.exceptions.CustomException;
import org.bookyourshows.exceptions.ResourceConflictException;
import org.bookyourshows.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;


import java.util.*;
import java.time.Duration;

import static org.bookyourshows.mapper.ShowMapper.mapHashMapToShowSeating;
import static org.bookyourshows.mapper.ShowMapper.mapShowSeatingToHashData;


public class ShowSeatCacheRepository {

    private static final long SHOW_SEAT_TTL = (int) Duration.ofDays(3).getSeconds();
    private static final Logger log = LoggerFactory.getLogger(ShowSeatCacheRepository.class);


    public void save(ShowSeating showSeating, Integer showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "show:" + showId + ":seating:" + showSeating.getShowSeatId();
            redisClient.hset(key, mapShowSeatingToHashData(showSeating));
            redisClient.expire(key, SHOW_SEAT_TTL);
        } catch (Exception e) {
            log.error("[ Show Cache Repository ], error : {}", e.getMessage());
        }
    }

    public void saveAll(List<ShowSeating> showSeatingList, Integer showId) {
        for (ShowSeating showSeating : showSeatingList) {
            save(showSeating, showId);
        }
    }

    public Map<Integer, List<ShowSeating>> getShowSeats(Integer showId) {
        RedisClient redisClient = RedisManager.getClient();
        String keyPattern = "show:" + showId + ":seating:*";

        Set<String> keys = new HashSet<>();
        String cursor = "0";
        do {
            ScanResult<String> result = redisClient.scan(cursor,
                    new ScanParams().match(keyPattern).count(200));
            keys.addAll(result.getResult());
            cursor = result.getCursor();
        } while (!cursor.equals("0"));

        Map<Integer, List<ShowSeating>> showSeatLayoutMap = new HashMap<>();

        for (String key : keys) {

            ShowSeating showSeat = getShowSeat(key);
            int rowNumber = showSeat.getRowNo();

            List<ShowSeating> toBeUpdated = showSeatLayoutMap.getOrDefault(rowNumber, new ArrayList<>());
            toBeUpdated.add(showSeat);
            showSeatLayoutMap.put(rowNumber, toBeUpdated);

        }
        return showSeatLayoutMap;
    }

    private ShowSeating getShowSeat(String key) {
        RedisClient redisClient = RedisManager.getClient();
        Map<String, String> map = redisClient.hgetAll(key);
        if (map == null || map.isEmpty()) {
            log.warn("[ Show Cache Repository ] There are no seats in the cache with this show id:- {}", key);
            return null;
        }
        String lockedKey = "locked:" + key;
        boolean locked = redisClient.exists(lockedKey);

        if (locked && !Objects.equals(map.get("status"), "BOOKED")) {
            map.put("status", "LOCKED");
        }
        return mapHashMapToShowSeating(map);
    }

    public void delete(String key) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            redisClient.del(key);
        } catch (Exception e) {
            log.error("[ Show Cache Repository ] error : {}", e.getMessage());
        }
    }


    public void deleteAllSeatsByShowId(Integer showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String keyPattern = "show:" + showId + ":seating:*";

            Set<String> keys = redisClient.keys(keyPattern);

            if (keys == null || keys.isEmpty()) {
                log.warn("[ Show Cache Repository ] There are no seats in the cache with this show id: {}", showId);
                return;
            }

            for (String key : keys) {
                delete(key);
            }

            log.info("[ Show Cache Repository ] Deleted {} seats for showId {}", keys.size(), showId);

        } catch (Exception e) {
            log.error("[ Show Cache Repository ] Error deleting show seats from cache for showId {}", showId, e);
        }
    }

    public void updateBookingStatus(Integer showId, List<Integer> showSeatIdList, Integer userId) {
        RedisClient redisClient = RedisManager.getClient();
        for (Integer showSeatId : showSeatIdList) {
            String key = "show:" + showId + ":seating:" + showSeatId;

            Map<String, String> showSeating = redisClient.hgetAll(key);
            if (showSeating == null || showSeating.isEmpty()) {
                log.warn("[ Show Cache Repository ] Show seating not found");
                return;
            }
            showSeating.put("status", "BOOKED");
            showSeating.put("locked-by", userId.toString());
            redisClient.hset(key, showSeating);
        }

    }

    public void lockShowSeats(List<Integer> showSeatIdToBeBooked, Integer showId, Integer userId) throws CustomException {
        RedisClient redisClient = RedisManager.getClient();

        for (Integer showSeatId : showSeatIdToBeBooked) {
            Map<String, String> showSeating = redisClient.hgetAll("show:" + showId + ":seating:" + showSeatId);
            if (showSeating == null || showSeating.isEmpty()) {
                throw new ResourceNotFoundException("Show seating not found");
            }
            if (showSeating.get("status").equals("BOOKED")) {
                throw new ResourceConflictException("Show seating already locked or booked");
            }
            String key = "locked:show:" + showId + ":seating:" + showSeatId;

            String existingUser = redisClient.get(key);
            if (existingUser != null && !userId.toString().equals(existingUser)) {
                throw new ResourceConflictException("Seat already locked");
            }

            String result = redisClient.set(key, userId.toString(),
                    SetParams.setParams().nx().ex(180));

            if (result == null) {
                throw new ResourceConflictException("Seat already locked");
            }
        }
    }
}
