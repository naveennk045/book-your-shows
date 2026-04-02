package org.bookyourshows.repository.cache.show;

import org.bookyourshows.config.RedisManager;
import org.bookyourshows.dto.show.ShowSeating;
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


    public void save(ShowSeating showSeating, Integer showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String key = "show:" + showId + ":seating:" + showSeating.getShowSeatId();
            redisClient.hset(key, mapShowSeatingToHashData(showSeating));
            redisClient.expire(key, SHOW_SEAT_TTL);
        } catch (Exception e) {
            System.err.println("[ Show Cache Repository ] : " + e.getMessage());
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
            throw new RuntimeException("There are no seats in the cache with this show id: " + key);
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
            System.err.println("[ Show Cache Repository ] : " + e.getMessage());
        }
    }


    public void deleteAllSeatsByShowId(Integer showId) {
        try {
            RedisClient redisClient = RedisManager.getClient();
            String keyPattern = "show:" + showId + ":seating:*";

            Set<String> keys = redisClient.keys(keyPattern);

            if (keys == null || keys.isEmpty()) {
                throw new RuntimeException("There are no seats in the cache with this show id: " + showId);
            }

            for (String key : keys) {
                delete(key);
            }

            System.out.println("Deleted " + keys.size() + " seats for showId " + showId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting show seats from cache for showId " + showId, e);
        }
    }

    public void updateBookingStatus(Integer showId, List<Integer> showSeatIdList, String seatStatus, Integer userId) {
        RedisClient redisClient = RedisManager.getClient();
        if (seatStatus.equals("BOOKED")) {
            for (Integer showSeatId : showSeatIdList) {
                String key = "show:" + showId + ":seating:" + showSeatId;

                Map<String, String> showSeating = redisClient.hgetAll(key);
                if (showSeating == null || showSeating.isEmpty()) {
                    throw new RuntimeException("Show seating not found");
                }
                showSeating.put("status", seatStatus);
                showSeating.put("locked-by", userId.toString());
                redisClient.hset(key, showSeating);
            }
        } else {
            throw new RuntimeException("Invalid seat_status: " + seatStatus);
        }
    }

    public void lockShowSeats(List<Integer> showSeatIdToBeBooked, Integer showId, Integer userId) {
        RedisClient redisClient = RedisManager.getClient();

        for (Integer showSeatId : showSeatIdToBeBooked) {
            Map<String, String> showSeating = redisClient.hgetAll("show:" + showId + ":seating:" + showSeatId);
            if (showSeating == null || showSeating.isEmpty()) {
                throw new RuntimeException("Show seating not found");
            }
            if (showSeating.get("status").equals("BOOKED")) {
                throw new RuntimeException("Show seating already locked or booked");
            }
            String key = "locked:show:" + showId + ":seating:" + showSeatId;

            String existingUser = redisClient.get(key);
            if (existingUser != null && !userId.toString().equals(existingUser)) {
                throw new RuntimeException("Seat already locked");
            }

            String result = redisClient.set(key, userId.toString(),
                    SetParams.setParams().nx().ex(180));

            if (result == null) {
                throw new RuntimeException("Seat already locked");
            }
        }
    }
}
