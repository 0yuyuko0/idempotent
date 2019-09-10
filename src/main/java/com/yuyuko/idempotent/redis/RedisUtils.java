package com.yuyuko.idempotent.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class RedisUtils {
    private RedisTemplate<String, String> redisTemplate;

    public RedisUtils(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean setIfAbsent(String id, long durationMilis) {
        return redisTemplate.opsForValue().setIfAbsent(id, "", Duration.ofMillis(durationMilis));
    }

    public boolean setIfPresent(String id, long durationMilis) {
        return redisTemplate.opsForValue().setIfPresent(id, "", Duration.ofMillis(durationMilis));
    }

    public boolean delete(String id) {
        return redisTemplate.delete(id);
    }
}