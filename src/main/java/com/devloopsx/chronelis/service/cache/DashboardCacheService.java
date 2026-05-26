package com.devloopsx.chronelis.service.cache;

import com.devloopsx.chronelis.dto.response.project.ProjectAnalyticsResponse;
import com.devloopsx.chronelis.dto.response.task.MyWorkResponse;
import com.devloopsx.chronelis.dto.response.task.TaskAnalyticsResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class DashboardCacheService {
  private static final String PREFIX = "chronelis:dashboard:";

  private final ObjectProvider<RedisTemplate<String, String>> redisTemplateProvider;
  private final ObjectMapper objectMapper;
  private final Duration ttl;

  public DashboardCacheService(
      ObjectProvider<RedisTemplate<String, String>> redisTemplateProvider,
      ObjectMapper objectMapper,
      @Value("${chronelis.dashboard-cache.ttl-seconds:300}") long ttlSeconds) {
    this.redisTemplateProvider = redisTemplateProvider;
    this.objectMapper = objectMapper;
    this.ttl = Duration.ofSeconds(Math.max(1, ttlSeconds));
  }

  public MyWorkResponse getMyWork(String userId, Supplier<MyWorkResponse> loader) {
    return getOrLoad(userKey("my-work", userId), MyWorkResponse.class, loader);
  }

  public TaskAnalyticsResponse getTaskAnalytics(
      String userId, Supplier<TaskAnalyticsResponse> loader) {
    return getOrLoad(userKey("task-analytics", userId), TaskAnalyticsResponse.class, loader);
  }

  public ProjectAnalyticsResponse getProjectAnalytics(
      Long projectId, Supplier<ProjectAnalyticsResponse> loader) {
    return getOrLoad(
        projectKey("project-analytics", projectId), ProjectAnalyticsResponse.class, loader);
  }

  public void evictUserTaskCaches(String userId) {
    if (!StringUtils.hasText(userId)) {
      return;
    }

    deleteKeys(List.of(userKey("my-work", userId), userKey("task-analytics", userId)));
  }

  public void evictUserTaskCaches(Collection<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return;
    }

    new LinkedHashSet<>(userIds).forEach(this::evictUserTaskCaches);
  }

  public void evictProjectAnalytics(Long projectId) {
    if (projectId == null) {
      return;
    }

    deleteKeys(List.of(projectKey("project-analytics", projectId)));
  }

  public void evictAll() {
    evictPattern(PREFIX + "*");
  }

  private <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
    RedisTemplate<String, String> redisTemplate = redisTemplate();
    try {
      if (redisTemplate != null) {
        String cached = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(cached)) {
          return objectMapper.readValue(cached, type);
        }
      }
    } catch (Exception ex) {
      log.warn("Dashboard cache read failed for key {}; falling back to database", key, ex);
    }

    T value = loader.get();
    if (value == null) {
      return null;
    }

    try {
      if (redisTemplate != null) {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
      }
    } catch (Exception ex) {
      log.warn("Dashboard cache write failed for key {}; response still served from database", key, ex);
    }

    return value;
  }

  private String userKey(String namespace, String userId) {
    return PREFIX + namespace + ":v1:user:" + userId + ":date:" + LocalDate.now();
  }

  private String projectKey(String namespace, Long projectId) {
    return PREFIX + namespace + ":v1:project:" + projectId + ":date:" + LocalDate.now();
  }

  private void evictPattern(String pattern) {
    try {
      RedisTemplate<String, String> redisTemplate = redisTemplate();
      if (redisTemplate == null) {
        return;
      }
      Set<String> keys = redisTemplate.keys(pattern);
      if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
      }
    } catch (Exception ex) {
      log.warn("Dashboard cache eviction failed for pattern {}; continuing without cache", pattern, ex);
    }
  }

  private void deleteKeys(Collection<String> keys) {
    try {
      RedisTemplate<String, String> redisTemplate = redisTemplate();
      if (redisTemplate == null) {
        return;
      }
      redisTemplate.delete(keys);
    } catch (Exception ex) {
      log.warn("Dashboard cache eviction failed for keys {}; continuing without cache", keys, ex);
    }
  }

  private RedisTemplate<String, String> redisTemplate() {
    try {
      return redisTemplateProvider.getIfAvailable();
    } catch (Exception ex) {
      log.warn("Dashboard cache RedisTemplate is unavailable; continuing without cache", ex);
      return null;
    }
  }
}
