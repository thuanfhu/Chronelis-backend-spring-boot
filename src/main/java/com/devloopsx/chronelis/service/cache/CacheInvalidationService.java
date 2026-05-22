package com.devloopsx.chronelis.service.cache;

import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheInvalidationService {
  RedisCacheService redisCacheService;
  AfterCommitExecutor afterCommitExecutor;
  ProjectRepository projectRepository;
  WorkspaceMemberRepository workspaceMemberRepository;

  public void invalidateGlobalAuthzAfterCommit() {
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.VERSION_AUTHZ_GLOBAL));
  }

  public void invalidateProjectAccessAfterCommit(Long projectId) {
    if (projectId == null) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.projectAccessVersion(projectId)));
  }

  public void invalidateWorkspaceAccessAfterCommit(Long workspaceId) {
    if (workspaceId == null) {
      return;
    }

    afterCommitExecutor.runAfterCommit(
        () -> {
          redisCacheService.incrementVersion(CacheKeys.workspaceMembersVersion(workspaceId));
          redisCacheService.incrementVersion(CacheKeys.workspaceAccessVersion(workspaceId));

          List<Long> projectIds = projectRepository.findIdsByWorkspaceId(workspaceId);
          for (Long projectId : projectIds) {
            redisCacheService.incrementVersion(CacheKeys.projectAccessVersion(projectId));
          }

          workspaceMemberRepository.findByWorkspaceIdOrderByJoinedAtAsc(workspaceId).stream()
              .map(member -> member.getUser().getUserId())
              .forEach(
                  userId -> redisCacheService.incrementVersion(CacheKeys.userWorkVersion(userId)));
        });
  }

  public void invalidateUserWorkAfterCommit(String userId) {
    if (userId == null || userId.isBlank()) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.userWorkVersion(userId)));
  }

  public void invalidateUserWorkAfterCommit(Collection<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () ->
            userIds.stream()
                .filter(Objects::nonNull)
                .filter(userId -> !userId.isBlank())
                .distinct()
                .forEach(
                    userId ->
                        redisCacheService.incrementVersion(CacheKeys.userWorkVersion(userId))));
  }

  public void invalidateProjectTasksAfterCommit(Long projectId) {
    if (projectId == null) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.projectTasksVersion(projectId)));
  }

  public void invalidateProjectSchedulesAfterCommit(Long projectId) {
    if (projectId == null) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.projectSchedulesVersion(projectId)));
  }

  public void invalidateWorkspaceSchedulesAfterCommit(Long workspaceId) {
    if (workspaceId == null) {
      return;
    }
    afterCommitExecutor.runAfterCommit(
        () -> redisCacheService.incrementVersion(CacheKeys.workspaceSchedulesVersion(workspaceId)));
  }
}
