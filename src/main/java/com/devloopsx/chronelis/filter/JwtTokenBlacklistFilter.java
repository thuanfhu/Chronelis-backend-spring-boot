package com.devloopsx.chronelis.filter;

import com.devloopsx.chronelis.configuration.SecurityConfiguration;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenBlacklistFilter extends OncePerRequestFilter {

  private final TokenBlacklistService tokenBlacklistService;
  private final JwtDecoder jwtDecoder;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = authHeader.substring(7);

      if (tokenBlacklistService.isTokenBlacklisted(token)) {
        throw new ApplicationException(ErrorCode.TOKEN_REVOKED);
      }

      jwtDecoder.decode(token);
      filterChain.doFilter(request, response);

    } catch (ApplicationException e) {
      response.setStatus(e.getErrorCode().getStatusCode().value());
      response.setContentType("application/json;charset=UTF-8");

      Map<String, Object> errorDetail = new HashMap<>();
      errorDetail.put("code", e.getErrorCode().getCode());
      errorDetail.put("message", e.getErrorCode().getMessage());

      Map<String, Object> apiResponse = new HashMap<>();
      apiResponse.put("success", false);
      apiResponse.put("errors", List.of(errorDetail));

      Map<String, Object> meta = new HashMap<>();
      meta.put("instance", request.getRequestURI());
      meta.put("timestamp", java.time.Instant.now().toString());
      apiResponse.put("meta", meta);

      response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    } catch (Exception e) {
      log.debug("Token validation error in blacklist filter: {}", e.getMessage());
      filterChain.doFilter(request, response);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return Arrays.stream(SecurityConfiguration.PUBLIC_ENDPOINTS)
        .anyMatch(
            endpoint -> {
              String pattern =
                  endpoint
                      .replace("{userId}", "[^/]+")
                      .replace("{hubId}", "[^/]+")
                      .replace("{categoryId}", "[^/]+")
                      .replace("{productId}", "[^/]+")
                      .replace("{inventoryItemId}", "[^/]+")
                      .replace("{slug}", "[^/]+")
                      .replace("{code}", "[^/]+")
                      .replace("{reviewId}", "[^/]+")
                      .replace("{policyId}", "[^/]+")
                      .replace("**", ".*");
              return path.matches(pattern) || path.startsWith(endpoint.replace("**", ""));
            });
  }
}
