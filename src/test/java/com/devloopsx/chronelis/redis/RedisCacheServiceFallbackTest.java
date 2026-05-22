package com.devloopsx.chronelis.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devloopsx.chronelis.service.cache.RedisCacheService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceFallbackTest {

  @Mock RedisTemplate<String, String> redisTemplate;

  RedisCacheService redisCacheService;

  @BeforeEach
  void setUp() {
    redisCacheService = new RedisCacheService(redisTemplate, new ObjectMapper());
    ReflectionTestUtils.setField(redisCacheService, "maxPayloadBytes", 128);
  }

  @Test
  void cacheReadWriteDeleteAndVersionFailuresDoNotThrow() {
    when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis unavailable"));
    doThrow(new RuntimeException("redis unavailable")).when(redisTemplate).delete("cache:key");

    assertThat(redisCacheService.getJson("cache:key", SmokeDto.class)).isEmpty();
    assertThatCode(
            () ->
                redisCacheService.setJson(
                    "cache:key", new SmokeDto("ok", 1), Duration.ofSeconds(30)))
        .doesNotThrowAnyException();
    assertThatCode(() -> redisCacheService.delete("cache:key")).doesNotThrowAnyException();
    assertThat(redisCacheService.incrementVersion("version:key")).isZero();
    assertThat(redisCacheService.getVersion("version:key")).isZero();
  }

  @Test
  void oversizedPayloadIsSkippedWithoutCallingRedis() {
    String largeValue = "x".repeat(512);

    assertThatCode(
            () ->
                redisCacheService.setJson(
                    "cache:large", new SmokeDto(largeValue, 1), Duration.ofSeconds(30)))
        .doesNotThrowAnyException();
    verify(redisTemplate, never()).opsForValue();
  }

  static class SmokeDto {
    public String name;
    public int value;

    SmokeDto() {}

    SmokeDto(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
