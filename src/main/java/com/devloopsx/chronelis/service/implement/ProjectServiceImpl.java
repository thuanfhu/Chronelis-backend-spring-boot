package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.TaskStatus;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
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
import com.devloopsx.chronelis.repository.TaskStatusRepository;
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
        TaskStatusRepository taskStatusRepository;
        ProjectMapper projectMapper;
        CollaborationAccessService collaborationAccessService;
        SecurityUtils securityUtils;
        ActivityLogService activityLogService;
        RealtimeEventPublisherService realtimeEventPublisherService;

        @Override
        @Transactional
        public ProjectResponse createProject(CreateProjectRequest request) {
                collaborationAccessService.requireCurrentWorkspaceMember(request.getWorkspaceId());
                Workspace workspace = collaborationAccessService.requireWorkspace(request.getWorkspaceId());

                User currentUser = securityUtils.getAuthenticatedUser();
                LocalDateTime now = LocalDateTime.now();

                Project project = projectMapper.toEntity(request);
                project.setWorkspace(workspace);
                project.setCreatedBy(currentUser);
                project.setStatus(ProjectStatusType.ACTIVE);
                project.setCreatedAt(now);
                project.setUpdatedAt(now);

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
                collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
                Project project = collaborationAccessService.requireProject(projectId);

                if ((request.getName() == null || request.getName().isBlank()) && request.getStatus() == null) {
                        throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
                }

                projectMapper.updateEntity(project, request);
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
                collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
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

        private void createDefaultTaskStatuses(Project project, LocalDateTime now) {
                List<TaskStatus> defaults = List.of(
                                TaskStatus.builder().project(project).name("Inbox").code("INBOX").position(1)
                                                .isClosed(false)
                                                .createdAt(now).build(),
                                TaskStatus.builder().project(project).name("Planned").code("PLANNED").position(2)
                                                .isClosed(false)
                                                .createdAt(now).build(),
                                TaskStatus.builder().project(project).name("Doing").code("DOING").position(3)
                                                .isClosed(false)
                                                .createdAt(now).build(),
                                TaskStatus.builder().project(project).name("Done").code("DONE").position(4)
                                                .isClosed(true)
                                                .createdAt(now).build());

                taskStatusRepository.saveAll(defaults);
        }
}
