package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // Handle 401 error (Authentication fails, throw error in Spring Filter)
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    ErrorCode errorCode;

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || authHeader.trim().isEmpty()) {
      errorCode = ErrorCode.MISSING_TOKEN;
    } else if (authException.getMessage() != null
        && authException.getMessage().toLowerCase().contains("expired")) {
      errorCode = ErrorCode.TOKEN_EXPIRED;
    } else {
      errorCode = ErrorCode.INVALID_TOKEN;
    }

    ResponseUtils.sendErrorResponse(request, response, objectMapper, errorCode);
  }
}
