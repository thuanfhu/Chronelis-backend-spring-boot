package com.devloopsx.chronelis.service;

public interface RealtimeEventPublisherService {
    void publishWorkspaceEvent(Long workspaceId, String eventType, Object data);

    void publishProjectEvent(Long workspaceId, Long projectId, String eventType, Object data);

    void publishTaskEvent(Long workspaceId, Long projectId, Long taskId, String eventType, Object data);

    void publishNotificationEvent(String userId, String eventType, Object data);

    void publishUnreadCount(String userId, long unreadCount);
}
