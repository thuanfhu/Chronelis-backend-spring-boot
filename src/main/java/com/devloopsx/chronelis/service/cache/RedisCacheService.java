package com.devloopsx.chronelis.service.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheService {
    RedisTemplate<String, String> redisTemplate;
    ObjectMapper objectMapper;

    public <T> Optional<T> getJson(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(value)) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(value, type));
        } catch (Exception ex) {
            log.warn("Redis cache read failed for key {}", key, ex);
            return Optional.empty();
        }
    }

    public <T> Optional<T> getJson(String key, TypeReference<T> typeRef) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(value)) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(value, typeRef));
        } catch (Exception ex) {
            log.warn("Redis cache read failed for key {}", key, ex);
            return Optional.empty();
        }
    }

    public void setJson(String key, Object value, Duration ttl) {
        if (value == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            log.warn("Redis cache write failed for key {}", key, ex);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Redis cache delete failed for key {}", key, ex);
        }
    }

    public long incrementVersion(String versionKey) {
        try {
            Long value = redisTemplate.opsForValue().increment(versionKey);
            return value == null ? 0L : value;
        } catch (Exception ex) {
            log.warn("Redis version increment failed for key {}", versionKey, ex);
            return 0L;
        }
    }

    public long getVersion(String versionKey) {
        try {
            String value = redisTemplate.opsForValue().get(versionKey);
            if (!StringUtils.hasText(value)) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (Exception ex) {
            log.warn("Redis version read failed for key {}", versionKey, ex);
            return 0L;
        }
    }

    public String versionedKey(String prefix, String versionKey, Object... parts) {
        String suffix = parts == null || parts.length == 0 ? "" : ":" + String.join(":",
                Arrays.stream(parts).map(String::valueOf).toList());
        return "%s%s:v%d".formatted(prefix, suffix, getVersion(versionKey));
    }

    public boolean isAvailable() {
        try {
            String response = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            return "PONG".equalsIgnoreCase(response);
        } catch (Exception ex) {
            log.warn("Redis availability check failed", ex);
            return false;
        }
    }
}
