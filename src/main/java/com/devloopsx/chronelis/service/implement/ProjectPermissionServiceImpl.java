package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.ProjectAccessGrant;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.WorkspaceMember;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.ProjectAccessGrantRepository;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamMemberRepository;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import com.devloopsx.chronelis.service.cache.CacheKeys;
import com.devloopsx.chronelis.service.cache.RedisCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectPermissionServiceImpl implements ProjectPermissionService {
  ProjectRepository projectRepository;
  ProjectAccessGrantRepository projectAccessGrantRepository;
  WorkspaceMemberRepository workspaceMemberRepository;
  WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
  SecurityUtils securityUtils;
  RedisCacheService redisCacheService;

  static final Duration PROJECT_ACCESS_TTL = Duration.ofMinutes(5);
  static final Duration REALTIME_AUTHORIZED_USERS_TTL = Duration.ofMinutes(5);

  @Override
  public EffectiveProjectAccessResponse resolveCurrentUserAccess(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Project không tồn tại"));
    return resolveCurrentUserAccess(project);
  }

  @Override
  public EffectiveProjectAccessResponse resolveCurrentUserAccess(Project project) {
    User currentUser = securityUtils.getAuthenticatedUser();
    String currentUserId = currentUser.getUserId();
    return resolveAccess(project, currentUserId);
  }

  @Override
  public EffectiveProjectAccessRoleType resolveCurrentUserRole(Project project) {
    return resolveCurrentUserAccess(project).getEffectiveRole();
  }

  @Override
  public EffectiveProjectAccessRoleType resolveUserRole(Project project, String userId) {
    if (userId == null || userId.isBlank()) {
      return EffectiveProjectAccessRoleType.NO_ACCESS;
    }

    long version = redisCacheService.getVersion(CacheKeys.projectAccessVersion(project.getId()));
    String key = CacheKeys.projectAccess(project.getId(), userId, version);
    return redisCacheService
        .getJson(key, EffectiveProjectAccessRoleType.class)
        .orElseGet(
            () -> {
              EffectiveProjectAccessRoleType role = resolveUserRoleUncached(project, userId);
              redisCacheService.setJson(key, role, PROJECT_ACCESS_TTL);
              return role;
            });
  }

  @Override
  public Set<String> findAuthorizedUserIds(Project project) {
    long version = redisCacheService.getVersion(CacheKeys.projectAccessVersion(project.getId()));
    String key = CacheKeys.realtimeAuthorizedUsers(project.getId(), version);
    return redisCacheService
        .getJson(key, new TypeReference<Set<String>>() {})
        .orElseGet(
            () -> {
              Set<String> userIds = findAuthorizedUserIdsUncached(project);
              redisCacheService.setJson(key, userIds, REALTIME_AUTHORIZED_USERS_TTL);
              return userIds;
            });
  }

  private EffectiveProjectAccessRoleType resolveUserRoleUncached(Project project, String userId) {
    WorkspaceMember member =
        workspaceMemberRepository
            .findByWorkspaceIdAndUserUserId(project.getWorkspace().getId(), userId)
            .orElse(null);
    if (member == null) {
      return EffectiveProjectAccessRoleType.NO_ACCESS;
    }
    boolean workspaceOwner = member.getRole() == WorkspaceMemberRoleType.OWNER;
    return resolveRole(project, userId, workspaceOwner);
  }

  private Set<String> findAuthorizedUserIdsUncached(Project project) {
    Long workspaceId = project.getWorkspace().getId();
    Set<String> userIds = new HashSet<>();
    workspaceMemberRepository.findByWorkspaceIdOrderByJoinedAtAsc(workspaceId).stream()
        .filter(m -> m.getRole() == WorkspaceMemberRoleType.OWNER)
        .forEach(m -> userIds.add(m.getUser().getUserId()));

    if (project.getVisibility() == ProjectVisibilityType.PUBLIC) {
      workspaceMemberRepository
          .findByWorkspaceIdOrderByJoinedAtAsc(workspaceId)
          .forEach(member -> userIds.add(member.getUser().getUserId()));
      return userIds;
    }

    List<ProjectAccessGrant> grants =
        projectAccessGrantRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
    for (ProjectAccessGrant grant : grants) {
      if (grant.getSubjectType() == ProjectAccessSubjectType.USER && grant.getUser() != null) {
        userIds.add(grant.getUser().getUserId());
      }
      if (grant.getSubjectType() == ProjectAccessSubjectType.TEAM && grant.getTeam() != null) {
        workspaceTeamMemberRepository
            .findByTeamIdOrderByJoinedAtAsc(grant.getTeam().getId())
            .forEach(member -> userIds.add(member.getUser().getUserId()));
      }
    }

    return userIds;
  }

  private EffectiveProjectAccessRoleType resolveRole(
      Project project, String userId, boolean workspaceOwner) {
    Long workspaceId = project.getWorkspace().getId();
    WorkspaceMember member =
        workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId).orElse(null);
    if (member == null) {
      return EffectiveProjectAccessRoleType.NO_ACCESS;
    }

    if (workspaceOwner) {
      return EffectiveProjectAccessRoleType.MANAGER;
    }

    EffectiveProjectAccessRoleType grantRole = resolveHighestGrantRole(project, userId);
    EffectiveProjectAccessRoleType defaultRole =
        project.getVisibility() == ProjectVisibilityType.PUBLIC
            ? EffectiveProjectAccessRoleType.CONTRIBUTOR
            : EffectiveProjectAccessRoleType.NO_ACCESS;

    return EffectiveProjectAccessRoleType.max(grantRole, defaultRole);
  }

  private EffectiveProjectAccessRoleType resolveHighestGrantRole(Project project, String userId) {
    EffectiveProjectAccessRoleType highest = EffectiveProjectAccessRoleType.NO_ACCESS;

    ProjectAccessGrant userGrant =
        projectAccessGrantRepository
            .findByProjectIdAndUserUserId(project.getId(), userId)
            .orElse(null);
    if (userGrant != null) {
      highest = EffectiveProjectAccessRoleType.max(highest, toEffectiveRole(userGrant.getRole()));
    }

    List<Long> teamIds =
        workspaceTeamMemberRepository.findTeamIdsByWorkspaceIdAndUserId(
            project.getWorkspace().getId(), userId);
    if (!teamIds.isEmpty()) {
      List<ProjectAccessGrant> teamGrants =
          projectAccessGrantRepository.findByProjectIdAndSubjectTypeAndTeamIdIn(
              project.getId(), ProjectAccessSubjectType.TEAM, teamIds);
      for (ProjectAccessGrant teamGrant : teamGrants) {
        highest = EffectiveProjectAccessRoleType.max(highest, toEffectiveRole(teamGrant.getRole()));
      }
    }

    return highest;
  }

  private EffectiveProjectAccessRoleType toEffectiveRole(ProjectAccessRoleType role) {
    return switch (role) {
      case VIEWER -> EffectiveProjectAccessRoleType.VIEWER;
      case CONTRIBUTOR -> EffectiveProjectAccessRoleType.CONTRIBUTOR;
      case MANAGER -> EffectiveProjectAccessRoleType.MANAGER;
    };
  }

  private EffectiveProjectAccessResponse resolveAccess(Project project, String userId) {
    long version = redisCacheService.getVersion(CacheKeys.projectAccessVersion(project.getId()));
    String key = CacheKeys.projectEffectiveAccess(project.getId(), userId, version);
    return redisCacheService
        .getJson(key, EffectiveProjectAccessResponse.class)
        .orElseGet(
            () -> {
              EffectiveProjectAccessResponse response = resolveAccessUncached(project, userId);
              redisCacheService.setJson(key, response, PROJECT_ACCESS_TTL);
              return response;
            });
  }

  private EffectiveProjectAccessResponse resolveAccessUncached(Project project, String userId) {
    Long workspaceId = project.getWorkspace().getId();
    WorkspaceMember member =
        workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId).orElse(null);
    if (member == null) {
      throw new ApplicationException(
          ErrorCode.UNAUTHORIZED_ACCESS, "Người dùng không thuộc workspace của project này");
    }

    boolean workspaceOwner = member.getRole() == WorkspaceMemberRoleType.OWNER;
    EffectiveProjectAccessRoleType effectiveRole = resolveRole(project, userId, workspaceOwner);

    boolean canViewProject = effectiveRole.atLeast(EffectiveProjectAccessRoleType.VIEWER);
    boolean canContribute = effectiveRole.atLeast(EffectiveProjectAccessRoleType.CONTRIBUTOR);
    boolean canManageProjectWork = effectiveRole.atLeast(EffectiveProjectAccessRoleType.MANAGER);
    boolean canManageProjectAccess =
        workspaceOwner || effectiveRole == EffectiveProjectAccessRoleType.MANAGER;
    boolean canGrantManager = workspaceOwner;

    return EffectiveProjectAccessResponse.builder()
        .projectId(project.getId())
        .workspaceId(workspaceId)
        .visibility(project.getVisibility())
        .effectiveRole(effectiveRole)
        .workspaceOwner(workspaceOwner)
        .canViewProject(canViewProject)
        .canContribute(canContribute)
        .canComment(canContribute)
        .canManageProjectWork(canManageProjectWork)
        .canManageProjectAccess(canManageProjectAccess)
        .canGrantManager(canGrantManager)
        .canRevokeManager(canGrantManager)
        .canManageManagerAccess(canGrantManager)
        .canChangeVisibility(workspaceOwner)
        .canDeleteProject(workspaceOwner)
        .canAssignOthers(workspaceOwner || effectiveRole == EffectiveProjectAccessRoleType.MANAGER)
        .canManageWorkspaceMembers(workspaceOwner)
        .canManageWorkspaceTeams(workspaceOwner)
        .canManageWorkspaceInvites(workspaceOwner)
        .canManageWorkspaceSettings(workspaceOwner)
        .build();
  }
}
