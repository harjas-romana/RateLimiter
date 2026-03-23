package com.ratelimiter.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Define the rules: 10 requests per 60 seconds
    private final int MAX_REQUESTS = 10;
    private final int WINDOW_IN_SECONDS = 60;

    /**
     * THE SDE FLEX: Sliding Window via Redis Sorted Sets (ZSET)
     * 1. Remove all records older than (current_time - window).
     * 2. Count the remaining records in the current window.
     * 3. If count < limit, add the new request and update the TTL. Return 1.
     * 4. Else, return 0 (Blocked).
     */
    private final String SLIDING_WINDOW_LUA = 
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window_ms = tonumber(ARGV[2])\n" +
            "local current_time = tonumber(ARGV[3])\n" +
            "local unique_id = ARGV[4]\n" +
            "local window_start = current_time - window_ms\n" +
            "redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)\n" +
            "local current_requests = redis.call('ZCARD', key)\n" +
            "if current_requests < limit then\n" +
            "    redis.call('ZADD', key, current_time, unique_id)\n" +
            "    redis.call('PEXPIRE', key, window_ms)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";

    public boolean isAllowed(String userId) {
        String redisKey = "rate_limit:sliding:" + userId;

        // Current time in milliseconds
        long currentTimeMs = System.currentTimeMillis();
        long windowInMs = WINDOW_IN_SECONDS * 1000L;
        // A unique identifier for this specific request
        String uniqueRequestId = currentTimeMs + "-" + UUID.randomUUID().toString();

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(SLIDING_WINDOW_LUA);
        redisScript.setResultType(Long.class);

        try {
            Long result = redisTemplate.execute(
                    redisScript, 
                    Collections.singletonList(redisKey), 
                    String.valueOf(MAX_REQUESTS), 
                    String.valueOf(windowInMs),
                    String.valueOf(currentTimeMs),
                    uniqueRequestId
            );

            System.out.println("DEBUG: Sliding Window result -> " + result);
            return result != null && result == 1L;
            
        } catch (Exception e) {
            System.err.println("REDIS ERROR: " + e.getMessage());
            e.printStackTrace();
            return false; // Fail-closed for testing, so we see if it breaks
        }
    }
}