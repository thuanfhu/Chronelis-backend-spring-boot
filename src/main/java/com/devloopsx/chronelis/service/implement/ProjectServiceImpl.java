package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.ProjectAccessGrant;
import com.devloopsx.chronelis.domain.TaskStatus;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.domain.WorkspaceTeam;
import com.devloopsx.chronelis.dto.request.project.CreateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectStatusRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectAnalyticsResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.ProjectMapper;
import com.devloopsx.chronelis.repository.ProjectAccessGrantRepository;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.service.cache.AfterCommitExecutor;
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
import com.devloopsx.chronelis.service.cache.CacheKeys;
import com.devloopsx.chronelis.service.cache.RedisCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {
  ProjectRepository projectRepository;
  ProjectAccessGrantRepository projectAccessGrantRepository;
  TaskRepository taskRepository;
  TaskStatusRepository taskStatusRepository;
  UserRepository userRepository;
  WorkspaceTeamRepository workspaceTeamRepository;
  ProjectMapper projectMapper;
  CollaborationAccessService collaborationAccessService;
  SecurityUtils securityUtils;
  ActivityLogService activityLogService;
  RealtimeEventPublisherService realtimeEventPublisherService;
  RedisCacheService redisCacheService;
  CacheInvalidationService cacheInvalidationService;
  AfterCommitExecutor afterCommitExecutor;

  static final Duration PROJECT_ANALYTICS_TTL = Duration.ofMinutes(3);

  @Override
  @Transactional
  public ProjectResponse createProject(CreateProjectRequest request) {
    collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(request.getWorkspaceId());
    Workspace workspace = collaborationAccessService.requireWorkspace(request.getWorkspaceId());

    if (request.getManagerUserId() != null || request.getManagerTeamId() != null) {
      collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(request.getWorkspaceId());
    }

    User currentUser = securityUtils.getAuthenticatedUser();
    LocalDateTime now = LocalDateTime.now();

    Project project = projectMapper.toEntity(request);
    project.setWorkspace(workspace);
    project.setCreatedBy(currentUser);
    project.setStatus(ProjectStatusType.ACTIVE);
    project.setVisibility(
        request.getVisibility() != null ? request.getVisibility() : ProjectVisibilityType.PUBLIC);
    project.setCreatedAt(now);
    project.setUpdatedAt(now);

    applyProjectManagerAssignments(
        project, workspace.getId(), request.getManagerUserId(), request.getManagerTeamId());

    Project savedProject = projectRepository.save(project);
    syncProjectManagerGrants(savedProject, null, null, true, currentUser, now);
    createDefaultTaskStatuses(savedProject, now);

    activityLogService.createLog(
        workspace.getId(),
        currentUser.getUserId(),
        ActivityActionType.PROJECT_CREATED,
        ActivityTargetType.PROJECT,
        savedProject.getId(),
        "Tạo project " + savedProject.getName());

    ProjectResponse response = projectMapper.toResponse(savedProject);
    cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspace.getId());
    publishProjectEventAfterCommit(
        workspace.getId(), savedProject.getId(), "project.created", response);
    return response;
  }

  @Override
  @Transactional
  public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
    Project project = collaborationAccessService.requireProject(projectId);
    String previousManagerUserId =
        project.getManagerUser() != null ? project.getManagerUser().getUserId() : null;
    Long previousManagerTeamId =
        project.getManagerTeam() != null ? project.getManagerTeam().getId() : null;

    if (request.getVisibility() != null) {
      collaborationAccessService.ensureCurrentUserCanChangeProjectVisibility(projectId);
    } else {
      collaborationAccessService.ensureCurrentUserCanManageProjectWork(projectId);
    }

    boolean managerUpdateRequested =
        request.getManagerUserId() != null || request.getManagerTeamId() != null;
    if (managerUpdateRequested) {
      collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(project.getWorkspace().getId());
    }

    if ((request.getName() == null || request.getName().isBlank())
        && request.getDescription() == null
        && request.getStatus() == null
        && request.getVisibility() == null
        && !managerUpdateRequested) {
      throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
    }

    projectMapper.updateEntity(project, request);

    if (request.getManagerUserId() != null || request.getManagerTeamId() != null) {
      applyProjectManagerAssignments(
          project,
          project.getWorkspace().getId(),
          request.getManagerUserId(),
          request.getManagerTeamId());
    }

    project.setUpdatedAt(LocalDateTime.now());

    Project updatedProject = projectRepository.save(project);
    User currentUser = securityUtils.getAuthenticatedUser();
    syncProjectManagerGrants(
        updatedProject,
        previousManagerUserId,
        previousManagerTeamId,
        managerUpdateRequested,
        currentUser,
        LocalDateTime.now());

    activityLogService.createLog(
        updatedProject.getWorkspace().getId(),
        currentUser.getUserId(),
        ActivityActionType.PROJECT_UPDATED,
        ActivityTargetType.PROJECT,
        updatedProject.getId(),
        "Cập nhật project " + updatedProject.getName());

    ProjectResponse response = projectMapper.toResponse(updatedProject);
    if (request.getVisibility() != null || managerUpdateRequested) {
      cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(
          updatedProject.getWorkspace().getId());
    }
    publishProjectEventAfterCommit(
        updatedProject.getWorkspace().getId(), updatedProject.getId(), "project.updated", response);
    return response;
  }

  @Override
  @Transactional
  public ProjectResponse updateProjectStatus(Long projectId, UpdateProjectStatusRequest request) {
    collaborationAccessService.ensureCurrentUserCanManageProjectWork(projectId);
    Project project = collaborationAccessService.requireProject(projectId);

    project.setStatus(request.getStatus());
    project.setUpdatedAt(LocalDateTime.now());

    Project updatedProject = projectRepository.save(project);
    User currentUser = securityUtils.getAuthenticatedUser();

    activityLogService.createLog(
        updatedProject.getWorkspace().getId(),
        currentUser.getUserId(),
        ActivityActionType.PROJECT_UPDATED,
        ActivityTargetType.PROJECT,
        updatedProject.getId(),
        "Cập nhật trạng thái project "
            + updatedProject.getName()
            + " thành "
            + request.getStatus());

    ProjectResponse response = projectMapper.toResponse(updatedProject);
    publishProjectEventAfterCommit(
        updatedProject.getWorkspace().getId(),
        updatedProject.getId(),
        "project.status-updated",
        response);
    return response;
  }

  @Override
  public ProjectResponse getProject(Long projectId) {
    collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
    return projectMapper.toResponse(collaborationAccessService.requireProject(projectId));
  }

  @Override
  public PaginationResponse listProjectsByWorkspace(Long workspaceId, Pageable pageable) {
    collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
    String currentUserId = securityUtils.getAuthenticatedUser().getUserId();
    Page<Project> page =
        projectRepository.findVisibleByWorkspaceIdAndUserId(workspaceId, currentUserId, pageable);

    return PaginationResponse.builder()
        .meta(
            PaginationMeta.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build())
        .content(page.getContent().stream().map(projectMapper::toResponse).toList())
        .build();
  }

  @Override
  @Transactional
  public void deleteProject(Long projectId) {
    Project project = collaborationAccessService.requireProject(projectId);
    Long workspaceId = project.getWorkspace().getId();

    collaborationAccessService.ensureCurrentUserCanDeleteProject(projectId);

    String projectName = project.getName();
    User currentUser = securityUtils.getAuthenticatedUser();

    // Delete tasks explicitly before removing project so task_status FK (RESTRICT)
    // does not conflict when DB cascades task_statuses via project delete.
    taskRepository.deleteByProjectIdIn(List.of(projectId));

    projectRepository.delete(project);

    activityLogService.createLog(
        workspaceId,
        currentUser.getUserId(),
        ActivityActionType.PROJECT_DELETED,
        ActivityTargetType.PROJECT,
        projectId,
        "Xóa project " + projectName);
    cacheInvalidationService.invalidateProjectAccessAfterCommit(projectId);
    cacheInvalidationService.invalidateProjectTasksAfterCommit(projectId);
    cacheInvalidationService.invalidateProjectSchedulesAfterCommit(projectId);
    cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
    publishProjectEventAfterCommit(workspaceId, projectId, "project.deleted", projectId);
  }

  private void applyProjectManagerAssignments(
      Project project, Long workspaceId, String managerUserId, Long managerTeamId) {
    if (managerUserId != null) {
      if (managerUserId.isBlank()) {
        project.setManagerUser(null);
      } else {
        collaborationAccessService.ensureAssigneeBelongsWorkspace(managerUserId, workspaceId);

        User managerUser =
            userRepository
                .findById(managerUserId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        project.setManagerUser(managerUser);
      }
    }

    if (managerTeamId != null) {
      if (managerTeamId <= 0) {
        project.setManagerTeam(null);
      } else {
        WorkspaceTeam managerTeam =
            workspaceTeamRepository
                .findById(managerTeamId)
                .orElseThrow(
                    () ->
                        new ApplicationException(
                            ErrorCode.RESOURCE_NOT_FOUND, "Team manager không tồn tại"));

        if (!managerTeam.getWorkspace().getId().equals(workspaceId)) {
          throw new ApplicationException(
              ErrorCode.INVALID_REQUEST_DATA, "Manager team phải thuộc cùng workspace");
        }

        project.setManagerTeam(managerTeam);
      }
    }
  }

  private void createDefaultTaskStatuses(Project project, LocalDateTime now) {
    List<TaskStatus> defaults =
        List.of(
            TaskStatus.builder()
                .project(project)
                .name("To do")
                .code("TODO")
                .position(1)
                .isClosed(false)
                .createdAt(now)
                .build(),
            TaskStatus.builder()
                .project(project)
                .name("In Progress")
                .code("IN_PROGRESS")
                .position(2)
                .isClosed(false)
                .createdAt(now)
                .build(),
            TaskStatus.builder()
                .project(project)
                .name("Done")
                .code("DONE")
                .position(3)
                .isClosed(true)
                .createdAt(now)
                .build());

    taskStatusRepository.saveAll(defaults);
  }

  private void syncProjectManagerGrants(
      Project project,
      String previousManagerUserId,
      Long previousManagerTeamId,
      boolean managerUpdateRequested,
      User actor,
      LocalDateTime now) {
    if (!managerUpdateRequested) {
      return;
    }

    String nextManagerUserId =
        project.getManagerUser() != null ? project.getManagerUser().getUserId() : null;
    Long nextManagerTeamId =
        project.getManagerTeam() != null ? project.getManagerTeam().getId() : null;

    if (previousManagerUserId != null && !previousManagerUserId.equals(nextManagerUserId)) {
      projectAccessGrantRepository
          .findByProjectIdAndUserUserId(project.getId(), previousManagerUserId)
          .filter(grant -> grant.getRole() == ProjectAccessRoleType.MANAGER)
          .ifPresent(projectAccessGrantRepository::delete);
    }

    if (previousManagerTeamId != null && !previousManagerTeamId.equals(nextManagerTeamId)) {
      projectAccessGrantRepository
          .findByProjectIdAndTeamId(project.getId(), previousManagerTeamId)
          .filter(grant -> grant.getRole() == ProjectAccessRoleType.MANAGER)
          .ifPresent(projectAccessGrantRepository::delete);
    }

    if (project.getManagerUser() != null) {
      ProjectAccessGrant grant =
          projectAccessGrantRepository
              .findByProjectIdAndUserUserId(project.getId(), project.getManagerUser().getUserId())
              .orElseGet(
                  () ->
                      ProjectAccessGrant.builder()
                          .project(project)
                          .subjectType(ProjectAccessSubjectType.USER)
                          .user(project.getManagerUser())
                          .grantedBy(actor)
                          .createdAt(now)
                          .build());
      grant.setSubjectType(ProjectAccessSubjectType.USER);
      grant.setUser(project.getManagerUser());
      grant.setTeam(null);
      grant.setRole(ProjectAccessRoleType.MANAGER);
      grant.setGrantedBy(grant.getGrantedBy() != null ? grant.getGrantedBy() : actor);
      grant.setUpdatedAt(now);
      projectAccessGrantRepository.save(grant);
    }

    if (project.getManagerTeam() != null) {
      ProjectAccessGrant grant =
          projectAccessGrantRepository
              .findByProjectIdAndTeamId(project.getId(), project.getManagerTeam().getId())
              .orElseGet(
                  () ->
                      ProjectAccessGrant.builder()
                          .project(project)
                          .subjectType(ProjectAccessSubjectType.TEAM)
                          .team(project.getManagerTeam())
                          .grantedBy(actor)
                          .createdAt(now)
                          .build());
      grant.setSubjectType(ProjectAccessSubjectType.TEAM);
      grant.setUser(null);
      grant.setTeam(project.getManagerTeam());
      grant.setRole(ProjectAccessRoleType.MANAGER);
      grant.setGrantedBy(grant.getGrantedBy() != null ? grant.getGrantedBy() : actor);
      grant.setUpdatedAt(now);
      projectAccessGrantRepository.save(grant);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ProjectAnalyticsResponse getProjectAnalytics(Long projectId) {
    collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
    long version = redisCacheService.getVersion(CacheKeys.projectTasksVersion(projectId));
    String key = CacheKeys.projectAnalytics(projectId, version);
    return redisCacheService
        .getJson(key, ProjectAnalyticsResponse.class)
        .orElseGet(
            () -> {
              ProjectAnalyticsResponse response = buildProjectAnalytics(projectId);
              redisCacheService.setJson(key, response, PROJECT_ANALYTICS_TTL);
              return response;
            });
  }

  private ProjectAnalyticsResponse buildProjectAnalytics(Long projectId) {
    LocalDateTime since = LocalDateTime.now().minusDays(30);

    Map<String, Integer> createdMap = new HashMap<>();
    Map<String, Integer> completedMap = new HashMap<>();
    for (Object[] row : taskRepository.countCreatedByDayForProject(projectId, since)) {
      createdMap.put(row[0].toString(), ((Number) row[1]).intValue());
    }
    for (Object[] row : taskRepository.countCompletedByDayForProject(projectId, since)) {
      completedMap.put(row[0].toString(), ((Number) row[1]).intValue());
    }

    List<ProjectAnalyticsResponse.DailyTrendPoint> trend = new ArrayList<>();
    int cumulative = 0;
    for (int i = 29; i >= 0; i--) {
      String date = LocalDate.now().minusDays(i).toString();
      int created = createdMap.getOrDefault(date, 0);
      cumulative += created;
      trend.add(
          ProjectAnalyticsResponse.DailyTrendPoint.builder()
              .date(date)
              .created(created)
              .completed(completedMap.getOrDefault(date, 0))
              .cumulative(cumulative)
              .build());
    }

    long total = taskRepository.countByProjectId(projectId);
    long completed = taskRepository.countByProjectIdAndIsCompletedTrue(projectId);
    double rate = total > 0 ? Math.round((double) completed / total * 1000.0) / 10.0 : 0.0;

    return ProjectAnalyticsResponse.builder()
        .trend(trend)
        .totalTasks((int) total)
        .completedTasks((int) completed)
        .completionRate(rate)
        .build();
  }

  private void publishProjectEventAfterCommit(
      Long workspaceId, Long projectId, String eventType, Object data) {
    afterCommitExecutor.runAfterCommit(
        () ->
            realtimeEventPublisherService.publishProjectEvent(
                workspaceId, projectId, eventType, data));
  }
}
