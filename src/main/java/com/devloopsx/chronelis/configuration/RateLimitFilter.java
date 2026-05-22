package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.utils.ResponseUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

  // Stores buckets for each client IP
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public RateLimitFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Get client IP (supports proxy)
    String clientIp = getClientIp(request);
    // Get or create a bucket for the client and check if a token is available
    Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());
    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      // Send 429 error response using reusable utility method
      ResponseUtils.sendErrorResponse(request, response, objectMapper, ErrorCode.TOO_MANY_REQUESTS);
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private Bucket createNewBucket() {
    Bandwidth limit =
        Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build();
    return Bucket.builder().addLimit(limit).build();
  }
}
