package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.service.TokenBlacklistService;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

  RedisTemplate<String, String> redisTemplate;

  private static final String BLACKLIST_PREFIX = "token:blacklist:";

  @Override
  public void blacklistToken(String token, long expirationTimeInSeconds) {
    if (token == null || token.trim().isEmpty()) {
      return;
    }

    try {
      String key = BLACKLIST_PREFIX + token;
      redisTemplate
          .opsForValue()
          .set(key, "blacklisted", expirationTimeInSeconds, TimeUnit.SECONDS);
      log.info("Token blacklisted successfully with TTL: {} seconds", expirationTimeInSeconds);
    } catch (Exception e) {
      log.error("Error blacklisting token: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to blacklist token", e);
    }
  }

  @Override
  public boolean isTokenBlacklisted(String token) {
    if (token == null || token.trim().isEmpty()) {
      return false;
    }

    try {
      String key = BLACKLIST_PREFIX + token;
      return redisTemplate.hasKey(key);
    } catch (Exception e) {
      log.error("Error checking token blacklist: {}", e.getMessage(), e);
      // Redis error: allow request to continue
      return false;
    }
  }

  @Override
  public void removeFromBlacklist(String token) {
    if (token == null || token.trim().isEmpty()) {
      return;
    }

    try {
      String key = BLACKLIST_PREFIX + token;
      redisTemplate.delete(key);
    } catch (Exception e) {
      log.error("Error removing token from blacklist: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to remove token from blacklist", e);
    }
  }
}
