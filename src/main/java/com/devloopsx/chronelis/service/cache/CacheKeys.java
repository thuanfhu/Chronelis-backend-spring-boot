package com.devloopsx.chronelis.service.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheKeys {
  public static final String VERSION_AUTHZ_GLOBAL = "version:authz:global";

  public static String authzUserPermissions(String userId, long version) {
    return "authz:api-permissions:user:%s:v%d".formatted(userId, version);
  }

  public static String projectAccessVersion(Long projectId) {
    return "version:project-access:%d".formatted(projectId);
  }

  public static String projectAccess(Long projectId, String userId, long version) {
    return "access:project:%d:user:%s:v%d".formatted(projectId, userId, version);
  }

  public static String projectEffectiveAccess(Long projectId, String userId, long version) {
    return "access:project:%d:user:%s:effective:v%d".formatted(projectId, userId, version);
  }

  public static String realtimeAuthorizedUsers(Long projectId, long version) {
    return "rt:authorized-users:project:%d:v%d".formatted(projectId, version);
  }

  public static String workspaceMembersVersion(Long workspaceId) {
    return "version:workspace-members:%d".formatted(workspaceId);
  }

  public static String workspaceAccessVersion(Long workspaceId) {
    return "version:workspace-access:%d".formatted(workspaceId);
  }

  public static String userWorkVersion(String userId) {
    return "version:user-work:%s".formatted(userId);
  }

  public static String myWork(String userId, long version) {
    return "mywork:user:%s:v%d".formatted(userId, version);
  }

  public static String taskAnalytics(String userId, long version) {
    return "analytics:task:user:%s:v%d".formatted(userId, version);
  }

  public static String projectTasksVersion(Long projectId) {
    return "version:project-tasks:%d".formatted(projectId);
  }

  public static String projectAnalytics(Long projectId, long version) {
    return "analytics:project:%d:v%d".formatted(projectId, version);
  }

  public static String projectSchedulesVersion(Long projectId) {
    return "version:project-schedules:%d".formatted(projectId);
  }

  public static String workspaceSchedulesVersion(Long workspaceId) {
    return "version:workspace-schedules:%d".formatted(workspaceId);
  }

  public static String projectCalendar(
      Long projectId, String fromDate, String toDate, int page, int size, long scheduleVersion) {
    return "calendar:project:%d:from:%s:to:%s:page:%d:size:%d:v%d"
        .formatted(projectId, fromDate, toDate, page, size, scheduleVersion);
  }

  public static String workspaceCalendar(
      Long workspaceId,
      String userId,
      String fromDate,
      String toDate,
      int page,
      int size,
      long scheduleVersion,
      long accessVersion) {
    return "calendar:workspace:%d:user:%s:from:%s:to:%s:page:%d:size:%d:scheduleV%d:accessV%d"
        .formatted(
            workspaceId, userId, fromDate, toDate, page, size, scheduleVersion, accessVersion);
  }

  public static String notificationUnreadCount(String userId) {
    return "notif:unread-count:user:%s".formatted(userId);
  }
}
