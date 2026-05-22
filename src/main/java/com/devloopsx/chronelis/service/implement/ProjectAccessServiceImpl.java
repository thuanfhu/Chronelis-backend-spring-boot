package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.dto.request.projectaccess.UpdateProjectAccessRequest;
import com.devloopsx.chronelis.dto.request.projectaccess.UpsertProjectAccessRequest;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.dto.response.projectaccess.ProjectAccessResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.ProjectAccessMapper;
import com.devloopsx.chronelis.repository.ProjectAccessGrantRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamMemberRepository;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.ProjectAccessService;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectAccessServiceImpl implements ProjectAccessService {
  ProjectAccessGrantRepository projectAccessGrantRepository;
  UserRepository userRepository;
  WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
  ProjectAccessMapper projectAccessMapper;
  CollaborationAccessService collaborationAccessService;
  ProjectPermissionService projectPermissionService;
  SecurityUtils securityUtils;
  CacheInvalidationService cacheInvalidationService;

  @Override
  public List<ProjectAccessResponse> listProjectAccess(Long projectId) {
    collaborationAccessService.ensureCurrentUserCanManageProjectAccess(projectId);
    return projectAccessGrantRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
        .map(projectAccessMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public ProjectAccessResponse upsertProjectAccess(
      Long projectId, UpsertProjectAccessRequest request) {
    Project project = collaborationAccessService.requireProject(projectId);
    EffectiveProjectAccessResponse actorAccess =
        projectPermissionService.resolveCurrentUserAccess(project);
    ensureCanManageRequestedRole(actorAccess, request.getRole());

    validateSubjectRequest(project, request);
    ProjectAccessGrant projectAccess = resolveExistingGrant(projectId, request);
    LocalDateTime now = LocalDateTime.now();

    if (projectAccess != null) {
      throw new ApplicationException(
          ErrorCode.INVALID_REQUEST_DATA, "Project access grant already exists");
    }

    projectAccess =
        ProjectAccessGrant.builder()
            .project(project)
            .subjectType(request.getSubjectType())
            .role(request.getRole())
            .grantedBy(securityUtils.getAuthenticatedUser())
            .createdAt(now)
            .updatedAt(now)
            .build();

    applySubject(projectAccess, project, request);
    ProjectAccessResponse response =
        projectAccessMapper.toResponse(projectAccessGrantRepository.save(projectAccess));
    invalidateProjectAccessMutation(project, affectedUserIds(projectAccess));
    return response;
  }

  @Override
  @Transactional
  public ProjectAccessResponse updateProjectAccess(
      Long projectId, Long accessId, UpdateProjectAccessRequest request) {
    Project project = collaborationAccessService.requireProject(projectId);
    EffectiveProjectAccessResponse actorAccess =
        projectPermissionService.resolveCurrentUserAccess(project);
    ensureCanManageRequestedRole(actorAccess, request.getRole());

    ProjectAccessGrant projectAccess =
        projectAccessGrantRepository
            .findByIdAndProjectId(accessId, projectId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Quyền truy cập project không tồn tại"));
    ensureCanManageExistingGrant(actorAccess, projectAccess);

    projectAccess.setRole(request.getRole());
    projectAccess.setUpdatedAt(LocalDateTime.now());
    ProjectAccessResponse response =
        projectAccessMapper.toResponse(projectAccessGrantRepository.save(projectAccess));
    invalidateProjectAccessMutation(project, affectedUserIds(projectAccess));
    return response;
  }

  @Override
  @Transactional
  public void revokeProjectAccess(Long projectId, Long accessId) {
    Project project = collaborationAccessService.requireProject(projectId);
    EffectiveProjectAccessResponse actorAccess =
        projectPermissionService.resolveCurrentUserAccess(project);
    if (!actorAccess.isCanManageProjectAccess()) {
      throw new ApplicationException(
          ErrorCode.UNAUTHORIZED_ACCESS, "Bạn không có quyền thu hồi quyền truy cập project này");
    }

    ProjectAccessGrant projectAccess =
        projectAccessGrantRepository
            .findByIdAndProjectId(accessId, projectId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Quyền truy cập project không tồn tại"));
    ensureCanManageExistingGrant(actorAccess, projectAccess);
    Set<String> affectedUserIds = affectedUserIds(projectAccess);
    projectAccessGrantRepository.delete(projectAccess);
    invalidateProjectAccessMutation(project, affectedUserIds);
  }

  @Override
  public EffectiveProjectAccessResponse getCurrentUserEffectiveAccess(Long projectId) {
    return projectPermissionService.resolveCurrentUserAccess(projectId);
  }

  private ProjectAccessGrant resolveExistingGrant(
      Long projectId, UpsertProjectAccessRequest request) {
    if (request.getSubjectType() == ProjectAccessSubjectType.USER) {
      return projectAccessGrantRepository
          .findByProjectIdAndUserUserId(projectId, request.getUserId())
          .orElse(null);
    }
    return projectAccessGrantRepository
        .findByProjectIdAndTeamId(projectId, request.getTeamId())
        .orElse(null);
  }

  private void applySubject(
      ProjectAccessGrant projectAccess, Project project, UpsertProjectAccessRequest request) {
    if (request.getSubjectType() == ProjectAccessSubjectType.USER) {
      User user =
          userRepository
              .findById(request.getUserId())
              .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
      projectAccess.setSubjectType(ProjectAccessSubjectType.USER);
      projectAccess.setUser(user);
      projectAccess.setTeam(null);
      return;
    }

    WorkspaceTeam team = collaborationAccessService.requireWorkspaceTeam(request.getTeamId());
    if (!team.getWorkspace().getId().equals(project.getWorkspace().getId())) {
      throw new ApplicationException(
          ErrorCode.INVALID_REQUEST_DATA,
          "Team được cấp quyền phải thuộc cùng workspace với project");
    }
    projectAccess.setSubjectType(ProjectAccessSubjectType.TEAM);
    projectAccess.setUser(null);
    projectAccess.setTeam(team);
  }

  private void validateSubjectRequest(Project project, UpsertProjectAccessRequest request) {
    if (request.getSubjectType() == ProjectAccessSubjectType.USER) {
      if (request.getUserId() == null
          || request.getUserId().isBlank()
          || request.getTeamId() != null) {
        throw new ApplicationException(
            ErrorCode.INVALID_REQUEST_DATA, "Quyền theo user cần userId và không được có teamId");
      }
      collaborationAccessService.ensureAssigneeBelongsWorkspace(
          request.getUserId(), project.getWorkspace().getId());
      return;
    }

    if (request.getTeamId() == null || request.getUserId() != null) {
      throw new ApplicationException(
          ErrorCode.INVALID_REQUEST_DATA, "Quyền theo team cần teamId và không được có userId");
    }
  }

  private void ensureCanManageRequestedRole(
      EffectiveProjectAccessResponse actorAccess, ProjectAccessRoleType role) {
    if (!actorAccess.isCanManageProjectAccess()) {
      throw new ApplicationException(
          ErrorCode.UNAUTHORIZED_ACCESS, "Bạn không có quyền quản lý quyền truy cập project này");
    }
    if (role == ProjectAccessRoleType.MANAGER && !actorAccess.isCanManageManagerAccess()) {
      throw new ApplicationException(
          ErrorCode.UNAUTHORIZED_ACCESS, "Chỉ owner workspace mới có quyền cấp quyền MANAGER");
    }
  }

  private void ensureCanManageExistingGrant(
      EffectiveProjectAccessResponse actorAccess, ProjectAccessGrant projectAccess) {
    if (projectAccess.getRole() == ProjectAccessRoleType.MANAGER
        && !actorAccess.isCanManageManagerAccess()) {
      throw new ApplicationException(
          ErrorCode.UNAUTHORIZED_ACCESS, "Chỉ owner workspace mới có quyền thay đổi quyền MANAGER");
    }
  }

  private Set<String> affectedUserIds(ProjectAccessGrant projectAccess) {
    if (projectAccess.getSubjectType() == ProjectAccessSubjectType.USER
        && projectAccess.getUser() != null) {
      return Set.of(projectAccess.getUser().getUserId());
    }

    if (projectAccess.getSubjectType() == ProjectAccessSubjectType.TEAM
        && projectAccess.getTeam() != null) {
      return workspaceTeamMemberRepository
          .findByTeamIdOrderByJoinedAtAsc(projectAccess.getTeam().getId())
          .stream()
          .map(member -> member.getUser().getUserId())
          .collect(Collectors.toSet());
    }

    return Set.of();
  }

  private void invalidateProjectAccessMutation(Project project, Set<String> affectedUserIds) {
    cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(project.getWorkspace().getId());
    cacheInvalidationService.invalidateUserWorkAfterCommit(affectedUserIds);
  }
}
