package com.devloopsx.chronelis.interceptor;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionInterceptor implements HandlerInterceptor {
	UserRepository userRepository;
	SecurityUtils securityUtils;

	@Override
	@Transactional // fix lazy loading (FetchType = LAZY)
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws Exception {
		String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		String httpMethod = request.getMethod();

		// Authorization (check allowed permissions)
		User userAuthenticated = this.securityUtils.getAuthenticatedUser();
		Set<Permission> allPermissions = userAuthenticated.getRoles().stream()
				.flatMap(role -> role.getPermissions().stream()).collect(Collectors.toSet());

		boolean isAuthorized = allPermissions.stream()
				.anyMatch(permission -> isRouteMatch(permission.getApiPath(), apiPath)
						&& permission.getHttpMethod().equalsIgnoreCase(httpMethod));
		if (!isAuthorized)
			throw new ApplicationException(ErrorCode.UNAUTHORIZED);

		return true;
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
