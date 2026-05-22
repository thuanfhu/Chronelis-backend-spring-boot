package com.devloopsx.chronelis.service.cache;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.domain.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthzPermissionCacheService {
    static final Duration USER_PERMISSION_TTL = Duration.ofMinutes(10);

    RedisCacheService redisCacheService;

    public Set<String> getUserRoutePermissions(User user) {
        long version = redisCacheService.getVersion(CacheKeys.VERSION_AUTHZ_GLOBAL);
        String key = CacheKeys.authzUserPermissions(user.getUserId(), version);

        return redisCacheService.getJson(key, new TypeReference<Set<String>>() {
                })
                .orElseGet(() -> {
                    Set<String> routes = loadUserRoutePermissions(user);
                    redisCacheService.setJson(key, routes, USER_PERMISSION_TTL);
                    return routes;
                });
    }

    public String routeString(String httpMethod, String apiPath) {
        return normalizeMethod(httpMethod) + " " + normalizePath(apiPath);
    }

    public boolean routeMatches(String cachedRoute, String httpMethod, String requestPathPattern) {
        if (!StringUtils.hasText(cachedRoute)) {
            return false;
        }

        int separatorIndex = cachedRoute.indexOf(' ');
        if (separatorIndex <= 0 || separatorIndex >= cachedRoute.length() - 1) {
            return false;
        }

        String permissionMethod = cachedRoute.substring(0, separatorIndex);
        String permissionPath = cachedRoute.substring(separatorIndex + 1);

        return permissionMethod.equals(normalizeMethod(httpMethod))
                && isRouteMatch(permissionPath, requestPathPattern);
    }

    private Set<String> loadUserRoutePermissions(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(this::routeString)
                .collect(Collectors.toSet());
    }

    private String routeString(Permission permission) {
        return routeString(permission.getHttpMethod(), permission.getApiPath());
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

        for (int i = 0; i < permissionSegments.length; i++) {
            String permissionSegment = permissionSegments[i];
            String requestSegment = requestSegments[i];

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
        if (path == null || path.isBlank()) {
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
