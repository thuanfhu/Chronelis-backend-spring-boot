package com.devloopsx.chronelis.service.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheService {
  static final long WARN_THROTTLE_MILLIS = 30_000L;

  RedisTemplate<String, String> redisTemplate;
  ObjectMapper objectMapper;

  @NonFinal
  @Value("${redis.cache.max-payload-bytes:65536}")
  int maxPayloadBytes;

  @NonFinal ConcurrentMap<String, Long> lastWarningAtByCode = new ConcurrentHashMap<>();

  public <T> Optional<T> getJson(String key, Class<T> type) {
    try {
      String value = redisTemplate.opsForValue().get(key);
      if (!StringUtils.hasText(value)) {
        return Optional.empty();
      }
      return Optional.ofNullable(objectMapper.readValue(value, type));
    } catch (Exception ex) {
      warnFailure("read", key, ex);
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
      warnFailure("read", key, ex);
      return Optional.empty();
    }
  }

  public void setJson(String key, Object value, Duration ttl) {
    if (value == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
      return;
    }

    try {
      String payload = objectMapper.writeValueAsString(value);
      int payloadSize = payload.getBytes(StandardCharsets.UTF_8).length;
      if (payloadSize > Math.max(1, maxPayloadBytes)) {
        log.debug(
            "Redis cache payload skipped for key {} because size {} exceeds limit {}",
            key,
            payloadSize,
            maxPayloadBytes);
        return;
      }
      redisTemplate.opsForValue().set(key, payload, ttl);
    } catch (Exception ex) {
      warnFailure("write", key, ex);
    }
  }

  public void delete(String key) {
    try {
      redisTemplate.delete(key);
    } catch (Exception ex) {
      warnFailure("delete", key, ex);
    }
  }

  public long incrementVersion(String versionKey) {
    try {
      Long value = redisTemplate.opsForValue().increment(versionKey);
      return value == null ? 0L : value;
    } catch (Exception ex) {
      warnFailure("version increment", versionKey, ex);
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
      warnFailure("version read", versionKey, ex);
      return 0L;
    }
  }

  public String versionedKey(String prefix, String versionKey, Object... parts) {
    String suffix =
        parts == null || parts.length == 0
            ? ""
            : ":" + String.join(":", Arrays.stream(parts).map(String::valueOf).toList());
    return "%s%s:v%d".formatted(prefix, suffix, getVersion(versionKey));
  }

  public boolean isAvailable() {
    try {
      String response =
          redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
      return "PONG".equalsIgnoreCase(response);
    } catch (Exception ex) {
      warnFailure("availability check", "PING", ex);
      return false;
    }
  }

  private void warnFailure(String operation, String key, Exception ex) {
    String code = operation + ":" + ex.getClass().getName();
    long now = System.currentTimeMillis();
    Long previous = lastWarningAtByCode.get(code);
    if (previous == null || now - previous >= WARN_THROTTLE_MILLIS) {
      lastWarningAtByCode.put(code, now);
      log.warn("Redis cache {} failed for key {}: {}", operation, key, ex.getMessage());
    }
    log.debug("Redis cache {} failed for key {}", operation, key, ex);
  }
}
