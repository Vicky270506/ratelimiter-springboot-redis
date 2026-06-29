package com.vignesh.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript<Long> rateLimitScript;

    @Value("${rate.limiter.max-requests}")
    private int maxRequests;

    @Value("${rate.limiter.window-seconds}")
    private int windowSeconds;

    public boolean isAllowed(String ipAddress){
        String key = "rate_limit:" + ipAddress;

        Long requestCount = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(windowSeconds));
        return requestCount != null && requestCount <= maxRequests;
    }

    public long getCurrentCount(String ipAddress)
    {
        String key = "rate_limit:" + ipAddress;
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0 : Long.parseLong(val);
    }
}
