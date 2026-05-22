package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.interceptor.PermissionInterceptor;
import com.devloopsx.chronelis.service.cache.AuthzPermissionCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
  SecurityUtils securityUtils;
  AuthzPermissionCacheService authzPermissionCacheService;

  @Bean
  PermissionInterceptor getPermissionInterceptor() {
    return new PermissionInterceptor(securityUtils, authzPermissionCacheService);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    String[] whiteList = {
      // Auth
      "/api/v1/auth/register",
      "/api/v1/auth/verify-active-account",
      "/api/v1/auth/login",
      "/api/v1/auth/refresh",
      "/api/v1/auth/resend-verify",
      "/api/v1/auth/forgot-password",
      "/api/v1/auth/reset-password",
      // Project assistant (service-layer authorization)
      "/api/v1/project-assistant/**",
      // Public browsing
      "/api/v1/categories",
      "/api/v1/categories/{categoryId}",
      "/api/v1/categories/tree",
      "/api/v1/products",
      "/api/v1/products/{productId}",
      "/api/v1/products/slug/{slug}",
      "/api/v1/products/{productId}/reviews",
      "/api/v1/hubs/public",
      "/api/v1/policies",
      "/api/v1/policies/{policyId}",
      "/api/v1/policies/code/{code}/latest",
      // VNPay callbacks
      "/api/v1/payments/vnpay/return",
      "/api/v1/payments/vnpay/ipn",
      // Contact tickets (guest can create)
      "/api/v1/contact-tickets"
    };

    registry.addInterceptor(getPermissionInterceptor()).excludePathPatterns(whiteList);
  }
}
