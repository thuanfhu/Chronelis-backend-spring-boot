package com.devloopsx.chronelis.interceptor;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionInterceptor implements HandlerInterceptor {
  SecurityUtils securityUtils;

  @Override
  @Transactional // fix lazy loading (FetchType = LAZY)
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler)
      throws Exception {
    String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String httpMethod = request.getMethod();

    // Authorization (check allowed permissions)
    User userAuthenticated = this.securityUtils.getAuthenticatedUser();
    boolean isAuthorized =
        userAuthenticated.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(
                permission ->
                    normalizeMethod(permission.getHttpMethod()).equals(normalizeMethod(httpMethod))
                        && isRouteMatch(permission.getApiPath(), apiPath));
    if (!isAuthorized) throw new ApplicationException(ErrorCode.UNAUTHORIZED);

    return true;
  }

  private String normalizeMethod(String httpMethod) {
    return httpMethod == null ? "" : httpMethod.trim().toUpperCase();
  }

  private boolean isRouteMatch(String permissionPath, String requestPathPattern) {
    String normalizedPermissionPath = normalizePath(permissionPath);
    String normalizedRequestPath = normalizePath(requestPathPattern);

    String[] permissionSegments = normalizedPermissionPath.split("/");
    String[] requestSegments = normalizedRequestPath.split("/");

    if (permissionSegments.length != requestSegments.length) {
      return false;
    }

    for (int index = 0; index < permissionSegments.length; index++) {
      String permissionSegment = permissionSegments[index];
      String requestSegment = requestSegments[index];

      if (permissionSegment.equals(requestSegment)) {
        continue;
      }

      if (isPathVariable(permissionSegment) && isPathVariable(requestSegment)) {
        continue;
      }

      return false;
    }

    return true;
  }

  private String normalizePath(String path) {
    if (!StringUtils.hasText(path)) {
      return "";
    }

    String normalized = path.trim();
    if (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }

    return normalized;
  }

  private boolean isPathVariable(String segment) {
    return segment.startsWith("{") && segment.endsWith("}");
  }
}
