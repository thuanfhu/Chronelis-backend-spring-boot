package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.dto.response.realtime.RealtimeEventResponse;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import com.devloopsx.chronelis.service.RealtimeEventPublisherService;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RealtimeEventPublisherServiceImpl implements RealtimeEventPublisherService {
  SimpMessagingTemplate messagingTemplate;
  ProjectRepository projectRepository;
  ProjectPermissionService projectPermissionService;

  @Override
  public void publishWorkspaceEvent(Long workspaceId, String eventType, Object data) {
    messagingTemplate.convertAndSend(
        "/public/workspaces/" + workspaceId + "/events", buildEvent(eventType, data));
  }

  @Override
  public void publishProjectEvent(Long workspaceId, Long projectId, String eventType, Object data) {
    Project project = projectRepository.findById(projectId).orElse(null);
    if (project == null) {
      return;
    }

    RealtimeEventResponse event = buildEvent(eventType, data);
    if (project.getVisibility() == ProjectVisibilityType.PUBLIC) {
      messagingTemplate.convertAndSend(
          "/public/workspaces/" + workspaceId + "/projects/" + projectId + "/events", event);
      return;
    }

    for (String userId : projectPermissionService.findAuthorizedUserIds(project)) {
      messagingTemplate.convertAndSendToUser(
          userId,
          "/private/workspaces/" + workspaceId + "/projects/" + projectId + "/events",
          event);
    }
  }

  @Override
  public void publishTaskEvent(Long workspaceId, Long projectId, Long taskId, String eventType, Object data) {
    Project project = projectRepository.findById(projectId).orElse(null);
    if (project == null) {
      return;
    }

    RealtimeEventResponse event = buildEvent(eventType, data);
    if (project.getVisibility() == ProjectVisibilityType.PUBLIC) {
      messagingTemplate.convertAndSend(
          "/public/workspaces/"
              + workspaceId
              + "/projects/"
              + projectId
              + "/tasks/"
              + taskId
              + "/events",
          event);
      return;
    }

    for (String userId : projectPermissionService.findAuthorizedUserIds(project)) {
      messagingTemplate.convertAndSendToUser(
          userId,
          "/private/workspaces/"
              + workspaceId
              + "/projects/"
              + projectId
              + "/tasks/"
              + taskId
              + "/events",
          event);
    }
  }

  @Override
  public void publishNotificationEvent(String userId, String eventType, Object data) {
    messagingTemplate.convertAndSendToUser(
        userId, "/private/notifications", buildEvent(eventType, data));
  }

  @Override
  public void publishUnreadCount(String userId, long unreadCount) {
    messagingTemplate.convertAndSendToUser(
        userId, "/private/notifications/unread-count", unreadCount);
  }

  private RealtimeEventResponse buildEvent(String eventType, Object data) {
    return RealtimeEventResponse.builder()
        .eventType(eventType)
        .data(data)
        .occurredAt(LocalDateTime.now())
        .build();
  }
}
