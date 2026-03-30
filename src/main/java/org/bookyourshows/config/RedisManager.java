package org.bookyourshows.config;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.ConnectionPoolConfig;


public class RedisManager {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    private static final RedisClient redisClient;

    static {
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);


        redisClient = RedisClient.builder()
                .hostAndPort(HOST, PORT)
                .poolConfig(poolConfig)
                .build();
    }

    public static RedisClient getClient() {
        return redisClient;
    }

    public static void close() {
        if (redisClient != null) {
            redisClient.close();
        }
    }

}