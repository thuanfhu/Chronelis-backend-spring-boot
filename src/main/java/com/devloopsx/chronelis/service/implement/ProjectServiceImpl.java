package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.TaskStatus;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.domain.WorkspaceTeam;
import com.devloopsx.chronelis.dto.request.project.CreateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectStatusRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.ProjectMapper;
import com.devloopsx.chronelis.mapper.TaskStatusMapper;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {
        ProjectRepository projectRepository;
        TaskRepository taskRepository;
        TaskStatusRepository taskStatusRepository;
        UserRepository userRepository;
        WorkspaceTeamRepository workspaceTeamRepository;
        ProjectMapper projectMapper;
        CollaborationAccessService collaborationAccessService;
        SecurityUtils securityUtils;
        ActivityLogService activityLogService;
        RealtimeEventPublisherService realtimeEventPublisherService;

        @Override
        @Transactional
        public ProjectResponse createProject(CreateProjectRequest request) {
                collaborationAccessService.ensureCurrentUserIsWorkspaceManager(request.getWorkspaceId());
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
                project.setCreatedAt(now);
                project.setUpdatedAt(now);

                applyProjectManagerAssignments(project, workspace.getId(), request.getManagerUserId(),
                                request.getManagerTeamId());

                Project savedProject = projectRepository.save(project);
                createDefaultTaskStatuses(savedProject, now);

                activityLogService.createLog(workspace.getId(), currentUser.getUserId(),
                                ActivityActionType.PROJECT_CREATED,
                                ActivityTargetType.PROJECT, savedProject.getId(),
                                "Tạo project " + savedProject.getName());

                ProjectResponse response = projectMapper.toResponse(savedProject);
                realtimeEventPublisherService.publishProjectEvent(workspace.getId(), savedProject.getId(),
                                "project.created",
                                response);
                return response;
        }

        @Override
        @Transactional
        public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
                collaborationAccessService.ensureCurrentUserCanManageProject(projectId);
                Project project = collaborationAccessService.requireProject(projectId);

                boolean managerUpdateRequested = request.getManagerUserId() != null
                                || request.getManagerTeamId() != null;
                if (managerUpdateRequested) {
                        collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(project.getWorkspace().getId());
                }

                if ((request.getName() == null || request.getName().isBlank())
                                && request.getDescription() == null
                                && request.getStatus() == null
                                && !managerUpdateRequested) {
                        throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
                }

                projectMapper.updateEntity(project, request);

                if (request.getManagerUserId() != null || request.getManagerTeamId() != null) {
                        applyProjectManagerAssignments(project, project.getWorkspace().getId(),
                                        request.getManagerUserId(), request.getManagerTeamId());
                }

                project.setUpdatedAt(LocalDateTime.now());

                Project updatedProject = projectRepository.save(project);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(updatedProject.getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.PROJECT_UPDATED, ActivityTargetType.PROJECT, updatedProject.getId(),
                                "Cập nhật project " + updatedProject.getName());

                ProjectResponse response = projectMapper.toResponse(updatedProject);
                realtimeEventPublisherService.publishProjectEvent(updatedProject.getWorkspace().getId(),
                                updatedProject.getId(),
                                "project.updated", response);
                return response;
        }

        @Override
        @Transactional
        public ProjectResponse updateProjectStatus(Long projectId, UpdateProjectStatusRequest request) {
                collaborationAccessService.ensureCurrentUserCanManageProject(projectId);
                Project project = collaborationAccessService.requireProject(projectId);

                project.setStatus(request.getStatus());
                project.setUpdatedAt(LocalDateTime.now());

                Project updatedProject = projectRepository.save(project);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(updatedProject.getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.PROJECT_UPDATED, ActivityTargetType.PROJECT, updatedProject.getId(),
                                "Cập nhật trạng thái project " + updatedProject.getName() + " thành "
                                                + request.getStatus());

                ProjectResponse response = projectMapper.toResponse(updatedProject);
                realtimeEventPublisherService.publishProjectEvent(updatedProject.getWorkspace().getId(),
                                updatedProject.getId(),
                                "project.status-updated", response);
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
                Page<Project> page = projectRepository.findByWorkspaceId(workspaceId, pageable);

                return PaginationResponse.builder()
                                .meta(PaginationMeta.builder()
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

                collaborationAccessService.ensureCurrentUserIsWorkspaceManager(workspaceId);

                String projectName = project.getName();
                User currentUser = securityUtils.getAuthenticatedUser();

                // Delete tasks explicitly before removing project so task_status FK (RESTRICT)
                // does not conflict when DB cascades task_statuses via project delete.
                taskRepository.deleteByProjectIdIn(List.of(projectId));

                projectRepository.delete(project);

                activityLogService.createLog(workspaceId, currentUser.getUserId(),
                                ActivityActionType.PROJECT_DELETED,
                                ActivityTargetType.PROJECT, projectId,
                                "Xóa project " + projectName);

                realtimeEventPublisherService.publishWorkspaceEvent(workspaceId, "project.deleted", projectId);
        }

        private void applyProjectManagerAssignments(Project project, Long workspaceId, String managerUserId,
                        Long managerTeamId) {
                if (managerUserId != null) {
                        if (managerUserId.isBlank()) {
                                project.setManagerUser(null);
                        } else {
                                collaborationAccessService.ensureAssigneeBelongsWorkspace(managerUserId, workspaceId);

                                User managerUser = userRepository.findById(managerUserId)
                                                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
                                project.setManagerUser(managerUser);
                        }
                }

                if (managerTeamId != null) {
                        if (managerTeamId <= 0) {
                                project.setManagerTeam(null);
                        } else {
                                WorkspaceTeam managerTeam = workspaceTeamRepository.findById(managerTeamId)
                                                .orElseThrow(() -> new ApplicationException(
                                                                ErrorCode.RESOURCE_NOT_FOUND,
                                                                "Team manager không tồn tại"));

                                if (!managerTeam.getWorkspace().getId().equals(workspaceId)) {
                                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                                        "Manager team phải thuộc cùng workspace");
                                }

                                project.setManagerTeam(managerTeam);
                        }
                }
        }

        private void createDefaultTaskStatuses(Project project, LocalDateTime now) {
                List<TaskStatus> defaults = List.of(
                                TaskStatus.builder().project(project).name("To do").code("TODO").position(1)
                                                .isClosed(false)
                                                .createdAt(now).build(),
                                TaskStatus.builder().project(project).name("In Progress").code("IN_PROGRESS")
                                                .position(2)
                                                .isClosed(false)
                                                .createdAt(now).build(),
                                TaskStatus.builder().project(project).name("Done").code("DONE").position(3)
                                                .isClosed(true)
                                                .createdAt(now).build());

                taskStatusRepository.saveAll(defaults);
        }
}
