package com.devloopsx.chronelis.interceptor;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.service.cache.AuthzPermissionCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionInterceptor implements HandlerInterceptor {
  SecurityUtils securityUtils;
  AuthzPermissionCacheService authzPermissionCacheService;

  @Override
  @Transactional // fix lazy loading (FetchType = LAZY)
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler)
      throws Exception {
    String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String httpMethod = request.getMethod();

    // Authorization (check allowed permissions)
    User userAuthenticated = this.securityUtils.getAuthenticatedUser();
    Set<String> routePermissions =
        authzPermissionCacheService.getUserRoutePermissions(userAuthenticated);

    boolean isAuthorized =
        routePermissions.stream()
            .anyMatch(
                permissionRoute ->
                    authzPermissionCacheService.routeMatches(permissionRoute, httpMethod, apiPath));
    if (!isAuthorized) throw new ApplicationException(ErrorCode.UNAUTHORIZED);

    return true;
  }
}
