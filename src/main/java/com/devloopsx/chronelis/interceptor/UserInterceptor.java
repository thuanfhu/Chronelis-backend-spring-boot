package com.devloopsx.chronelis.interceptor;

import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * WebSocket Interceptor for JWT authentication Validates JWT token from STOMP headers and sets
 * authenticated user as Principal
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserInterceptor implements ChannelInterceptor {

  private final SecurityUtils securityUtils;

  private static class AuthenticatedUser implements java.security.Principal {
    private final String userId;
    private final String email;

    public AuthenticatedUser(String userId, String email) {
      this.userId = userId;
      this.email = email;
    }

    @Override
    public String getName() {
      return userId; // Return userId as principal name
    }

    public String getEmail() {
      return email;
    }
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      Object raw = message.getHeaders().get("nativeHeaders");
      if (raw instanceof Map) {
        // Get Authorization header from STOMP connect headers
        Object authHeader = ((Map<?, ?>) raw).get("Authorization");
        if (authHeader instanceof List && !((List<?>) authHeader).isEmpty()) {
          String token = ((List<?>) authHeader).getFirst().toString();

          // Remove "Bearer " prefix if exists
          if (token.startsWith("Bearer ")) {
            token = token.substring(7);
          }

          try {
            // Verify JWT token
            SignedJWT signedJWT = securityUtils.verifyAccessToken(token);
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");

            // Set authenticated user as Principal
            accessor.setUser(new AuthenticatedUser(userId, email));

            log.info("WebSocket authenticated user: {} ({})", userId, email);
          } catch (JOSEException | ParseException e) {
            log.error("Invalid JWT token for WebSocket connection: {}", e.getMessage());
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
          }
        } else {
          log.error("No Authorization header found in WebSocket CONNECT");
          throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
      }
    }
    return message;
  }
}
