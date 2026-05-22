package com.devloopsx.chronelis.configuration;

import java.security.Principal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WebSocketEventListener {
  // SimpMessagingTemplate messagingTemplate;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
    Principal principal = headerAccessor.getUser();

    if (principal == null) {
      log.warn(
          "WebSocket connection without authenticated user (sessionId: {})",
          headerAccessor.getSessionId());
      return;
    }

    String userId = principal.getName(); // userId from JWT token
    log.info("WebSocket connected: userId={}", userId);
  }

  @EventListener
  @Async("asyncTaskExecutor")
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
    Principal principal = headerAccessor.getUser();

    if (principal != null) {
      String userId = principal.getName();
      log.info("WebSocket disconnected: userId={}", userId);
    } else {
      log.info("WebSocket disconnected: sessionId={}", headerAccessor.getSessionId());
    }
  }
}
