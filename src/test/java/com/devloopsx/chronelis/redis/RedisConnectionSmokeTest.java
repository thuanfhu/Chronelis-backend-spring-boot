package com.devloopsx.chronelis.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.devloopsx.chronelis.configuration.RedisConfiguration;
import com.devloopsx.chronelis.service.cache.RedisCacheService;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tools.jackson.databind.ObjectMapper;

@SpringJUnitConfig(classes = RedisConnectionSmokeTest.RedisSmokeConfiguration.class)
@EnabledIfSystemProperty(named = "redis.smoke.enabled", matches = "true")
class RedisConnectionSmokeTest {

  @Autowired RedisTemplate<String, String> redisTemplate;

  @Autowired RedisCacheService redisCacheService;

  @Value("${redis.cache.max-payload-bytes:65536}")
  int maxPayloadBytes;

  @Test
  void redisTemplateCanSetGetTtlAndDelete() {
    String key = testKey("template");

    assertThatCode(() -> redisTemplate.opsForValue().set(key, "ok", Duration.ofSeconds(30)))
        .doesNotThrowAnyException();
    assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("ok");
    assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isPositive();
    assertThat(redisTemplate.delete(key)).isTrue();
  }

  @Test
  void redisCacheServiceCanRoundTripJsonAndSkipLargePayload() {
    String key = testKey("json");
    String missingKey = testKey("missing");
    String largeKey = testKey("large");

    assertThat(redisCacheService.getJson(missingKey, SmokeDto.class)).isEmpty();

    redisCacheService.setJson(key, new SmokeDto("chronelis", 1), Duration.ofSeconds(30));

    assertThat(redisCacheService.getJson(key, SmokeDto.class))
        .hasValueSatisfying(
            value -> {
              assertThat(value.name).isEqualTo("chronelis");
              assertThat(value.value).isEqualTo(1);
            });
    assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isPositive();

    redisCacheService.delete(key);
    assertThat(redisTemplate.hasKey(key)).isFalse();

    redisCacheService.setJson(
        largeKey, new SmokeDto("x".repeat(maxPayloadBytes + 512), 2), Duration.ofSeconds(30));
    assertThat(redisTemplate.hasKey(largeKey)).isFalse();
  }

  private String testKey(String suffix) {
    return "chronelis:test:redis-smoke:%s:%s".formatted(suffix, UUID.randomUUID());
  }

  @Configuration
  @Import({RedisConfiguration.class, RedisCacheService.class})
  static class RedisSmokeConfiguration {
    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
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
