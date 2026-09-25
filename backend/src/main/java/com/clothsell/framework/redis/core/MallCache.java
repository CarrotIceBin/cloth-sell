package com.clothsell.framework.redis.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class MallCache {
    private final ObjectProvider<StringRedisTemplate> redis;
    private final ObjectMapper mapper;

    public MallCache(ObjectProvider<StringRedisTemplate> redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public <T> T get(String key, Class<T> type) {
        return read(key, json -> mapper.readValue(json, type));
    }

    public <T> T get(String key, TypeReference<T> type) {
        return read(key, json -> mapper.readValue(json, type));
    }

    public void set(String key, Object value, Duration ttl) {
        StringRedisTemplate template = redis.getIfAvailable();
        if (template == null || value == null) {
            return;
        }
        try {
            template.opsForValue().set(key, mapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
    }

    public void evictProducts() {
        StringRedisTemplate template = redis.getIfAvailable();
        if (template == null) {
            return;
        }
        try {
            Set<String> keys = template.keys("mall:product:*");
            if (keys != null && !keys.isEmpty()) {
                template.delete(keys);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private <T> T read(String key, Reader<T> reader) {
        StringRedisTemplate template = redis.getIfAvailable();
        if (template == null) {
            return null;
        }
        try {
            String json = template.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return null;
            }
            return reader.read(json);
        } catch (Exception ignored) {
            return null;
        }
    }

    private interface Reader<T> {
        T read(String json) throws Exception;
    }
}
