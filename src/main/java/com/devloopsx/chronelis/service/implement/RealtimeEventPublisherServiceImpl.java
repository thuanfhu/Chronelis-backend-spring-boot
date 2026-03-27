package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.dto.response.realtime.RealtimeEventResponse;
import com.devloopsx.chronelis.service.RealtimeEventPublisherService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RealtimeEventPublisherServiceImpl implements RealtimeEventPublisherService {
    SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishWorkspaceEvent(Long workspaceId, String eventType, Object data) {
        messagingTemplate.convertAndSend("/public/workspaces/" + workspaceId + "/events",
                buildEvent(eventType, data));
    }

    @Override
    public void publishProjectEvent(Long workspaceId, Long projectId, String eventType, Object data) {
        messagingTemplate.convertAndSend("/public/workspaces/" + workspaceId + "/projects/" + projectId + "/events",
                buildEvent(eventType, data));
    }

    @Override
    public void publishTaskEvent(Long workspaceId, Long projectId, Long taskId, String eventType, Object data) {
        messagingTemplate.convertAndSend(
                "/public/workspaces/" + workspaceId + "/projects/" + projectId + "/tasks/" + taskId + "/events",
                buildEvent(eventType, data));
    }

    @Override
    public void publishNotificationEvent(String userId, String eventType, Object data) {
        messagingTemplate.convertAndSendToUser(userId, "/private/notifications", buildEvent(eventType, data));
    }

    @Override
    public void publishUnreadCount(String userId, long unreadCount) {
        messagingTemplate.convertAndSendToUser(userId, "/private/notifications/unread-count", unreadCount);
    }

    private RealtimeEventResponse buildEvent(String eventType, Object data) {
        return RealtimeEventResponse.builder()
                .eventType(eventType)
                .data(data)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
