package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.domain.WorkspaceMember;
import com.devloopsx.chronelis.dto.request.workspace.AddWorkspaceMemberRequest;
import com.devloopsx.chronelis.dto.request.workspace.CreateWorkspaceRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceMemberRoleRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceMemberResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.WorkspaceMapper;
import com.devloopsx.chronelis.mapper.WorkspaceMemberMapper;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import com.devloopsx.chronelis.repository.WorkspaceRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.service.cache.AfterCommitExecutor;
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
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
public class WorkspaceServiceImpl implements WorkspaceService {
        WorkspaceRepository workspaceRepository;
        ProjectRepository projectRepository;
        TaskRepository taskRepository;
        WorkspaceMemberRepository workspaceMemberRepository;
        UserRepository userRepository;
        WorkspaceMapper workspaceMapper;
        WorkspaceMemberMapper workspaceMemberMapper;
        SecurityUtils securityUtils;
        CollaborationAccessService collaborationAccessService;
        ActivityLogService activityLogService;
        RealtimeEventPublisherService realtimeEventPublisherService;
        NotificationService notificationService;
        CacheInvalidationService cacheInvalidationService;
        AfterCommitExecutor afterCommitExecutor;

        @Override
        @Transactional
        public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
                User currentUser = securityUtils.getAuthenticatedUser();
                LocalDateTime now = LocalDateTime.now();

                Workspace workspace = workspaceMapper.toEntity(request);
                workspace.setOwner(currentUser);
                workspace.setCreatedAt(now);
                workspace.setUpdatedAt(now);

                Workspace savedWorkspace = workspaceRepository.save(workspace);

                WorkspaceMember ownerMember = WorkspaceMember.builder()
                                .workspace(savedWorkspace)
                                .user(currentUser)
                                .role(WorkspaceMemberRoleType.OWNER)
                                .joinedAt(now)
                                .build();
                workspaceMemberRepository.save(ownerMember);

                activityLogService.createLog(savedWorkspace.getId(), currentUser.getUserId(),
                                ActivityActionType.WORKSPACE_CREATED,
                                ActivityTargetType.WORKSPACE, savedWorkspace.getId(),
                                "Tạo workspace " + savedWorkspace.getName());

                WorkspaceResponse response = workspaceMapper.toResponse(savedWorkspace);
                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(savedWorkspace.getId());
                publishWorkspaceEventAfterCommit(savedWorkspace.getId(), "workspace.created",
                                response);
                return response;
        }

        @Override
        @Transactional
        public WorkspaceResponse updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request) {
                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);
                collaborationAccessService.ensureCurrentUserIsWorkspaceManager(workspaceId);

                if (request.getName() == null || request.getName().isBlank()) {
                        throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
                }

                workspaceMapper.updateEntity(workspace, request);
                workspace.setUpdatedAt(LocalDateTime.now());

                Workspace updatedWorkspace = workspaceRepository.save(workspace);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.WORKSPACE_UPDATED,
                                ActivityTargetType.WORKSPACE, workspaceId,
                                "Cập nhật workspace " + updatedWorkspace.getName());

                WorkspaceResponse response = workspaceMapper.toResponse(updatedWorkspace);
                publishWorkspaceEventAfterCommit(workspaceId, "workspace.updated", response);
                return response;
        }

        @Override
        public WorkspaceResponse getWorkspace(Long workspaceId) {
                collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);
                return workspaceMapper.toResponse(workspace);
        }

        @Override
        public PaginationResponse listVisibleWorkspaces(Pageable pageable) {
                User currentUser = securityUtils.getAuthenticatedUser();
                Page<Workspace> page = workspaceRepository.findVisibleByUserId(currentUser.getUserId(), pageable);

                return PaginationResponse.builder()
                                .meta(PaginationMeta.builder()
                                                .currentPage(pageable.getPageNumber() + 1)
                                                .pageSize(pageable.getPageSize())
                                                .totalPages(page.getTotalPages())
                                                .totalElements(page.getTotalElements())
                                                .hasNext(page.hasNext())
                                                .hasPrevious(page.hasPrevious())
                                                .build())
                                .content(page.getContent().stream().map(workspaceMapper::toResponse).toList())
                                .build();
        }

        @Override
        @Transactional
        public WorkspaceMemberResponse addMember(Long workspaceId, AddWorkspaceMemberRequest request) {
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);

                User targetUser = resolveWorkspaceMemberTarget(request.getUserId());

                if (workspaceMemberRepository.existsByWorkspaceIdAndUserUserId(workspaceId, targetUser.getUserId())) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Người dùng đã là thành viên workspace");
                }

                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);
                WorkspaceMember member = WorkspaceMember.builder()
                                .workspace(workspace)
                                .user(targetUser)
                                .role(request.getRole())
                                .joinedAt(LocalDateTime.now())
                                .build();

                WorkspaceMember savedMember = workspaceMemberRepository.save(member);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.MEMBER_ADDED,
                                ActivityTargetType.MEMBER, savedMember.getId(),
                                "Thêm thành viên " + targetUser.getEmail() + " vào workspace");

                WorkspaceMemberResponse response = workspaceMemberMapper.toResponse(savedMember);
                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                cacheInvalidationService.invalidateUserWorkAfterCommit(targetUser.getUserId());
                publishWorkspaceEventAfterCommit(workspaceId, "workspace.member.added", response);

                notificationService.createAndPublish(targetUser.getUserId(), NotificationType.WORKSPACE_MEMBER_ADDED,
                                "Bạn được thêm vào workspace", "Bạn vừa được thêm vào workspace " + workspace.getName(),
                                ReferenceType.WORKSPACE, workspaceId);

                return response;
        }

        @Override
        public List<WorkspaceMemberResponse> listMembers(Long workspaceId) {
                collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
                return workspaceMemberRepository.findByWorkspaceIdOrderByJoinedAtAsc(workspaceId).stream()
                                .map(workspaceMemberMapper::toResponse)
                                .toList();
        }

        @Override
        @Transactional
        public WorkspaceMemberResponse updateMemberRole(Long workspaceId, String userId,
                        UpdateWorkspaceMemberRoleRequest request) {
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);
                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);

                WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Thành viên workspace không tồn tại"));

                if (member.getRole() == request.getRole()) {
                        throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
                }

                if (member.getRole() == WorkspaceMemberRoleType.OWNER && request.getRole() != WorkspaceMemberRoleType.OWNER) {
                        long ownerCount = workspaceMemberRepository.countByWorkspaceIdAndRole(workspaceId, WorkspaceMemberRoleType.OWNER);
                        if (ownerCount <= 1) {
                                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                                "Không thể hạ cấp OWNER duy nhất của workspace");
                        }
                }

                member.setRole(request.getRole());
                WorkspaceMember updatedMember = workspaceMemberRepository.save(member);

                User currentUser = securityUtils.getAuthenticatedUser();
                activityLogService.createLog(workspaceId, currentUser.getUserId(),
                                ActivityActionType.MEMBER_ROLE_UPDATED,
                                ActivityTargetType.MEMBER, updatedMember.getId(),
                                "Cập nhật vai trò thành viên " + updatedMember.getUser().getEmail());

                WorkspaceMemberResponse response = workspaceMemberMapper.toResponse(updatedMember);
                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                cacheInvalidationService.invalidateUserWorkAfterCommit(userId);
                publishWorkspaceEventAfterCommit(workspaceId, "workspace.member.role-updated",
                                response);
                return response;
        }

        @Override
        @Transactional
        public void removeMember(Long workspaceId, String userId) {
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);
                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);

                WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Thành viên workspace không tồn tại"));

                if (member.getRole() == WorkspaceMemberRoleType.OWNER) {
                        long ownerCount = workspaceMemberRepository.countByWorkspaceIdAndRole(workspaceId, WorkspaceMemberRoleType.OWNER);
                        if (ownerCount <= 1) {
                                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                                "Không thể xóa OWNER duy nhất của workspace");
                        }
                }

                workspaceMemberRepository.delete(member);

                User currentUser = securityUtils.getAuthenticatedUser();
                activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.MEMBER_REMOVED,
                                ActivityTargetType.MEMBER, member.getId(),
                                "Xóa thành viên " + member.getUser().getEmail() + " khỏi workspace");

                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                cacheInvalidationService.invalidateUserWorkAfterCommit(userId);
                publishWorkspaceEventAfterCommit(workspaceId, "workspace.member.removed",
                                workspaceMemberMapper.toResponse(member));

                notificationService.createAndPublish(userId, NotificationType.WORKSPACE_MEMBER_REMOVED,
                                "Bạn đã bị xóa khỏi workspace", "Bạn đã bị xóa khỏi workspace " + workspace.getName(),
                                ReferenceType.WORKSPACE, workspaceId);
        }

        private User resolveWorkspaceMemberTarget(String userIdentifier) {
                String normalizedIdentifier = userIdentifier == null ? "" : userIdentifier.trim();
                if (normalizedIdentifier.isEmpty()) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Thiếu thông tin định danh người dùng");
                }

                return userRepository.findById(normalizedIdentifier)
                                .or(() -> userRepository.findByEmailIgnoreCase(normalizedIdentifier))
                                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        }

        @Override
        @Transactional
        public void deleteWorkspace(Long workspaceId) {
                Workspace workspace = collaborationAccessService.requireWorkspace(workspaceId);
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);

                User currentUser = securityUtils.getAuthenticatedUser();

                String workspaceName = workspace.getName();

                List<Long> workspaceProjectIds = projectRepository.findIdsByWorkspaceId(workspaceId);
                List<String> workspaceUserIds = workspaceMemberRepository.findByWorkspaceIdOrderByJoinedAtAsc(workspaceId)
                                .stream()
                                .map(member -> member.getUser().getUserId())
                                .toList();
                if (!workspaceProjectIds.isEmpty()) {
                        // Remove tasks before deleting workspace/project cascades to prevent
                        // fk_tasks_status (RESTRICT) violations during status deletion.
                        taskRepository.deleteByProjectIdIn(workspaceProjectIds);
                }

                workspaceRepository.delete(workspace);

                workspaceProjectIds.forEach(projectId -> {
                        cacheInvalidationService.invalidateProjectAccessAfterCommit(projectId);
                        cacheInvalidationService.invalidateProjectTasksAfterCommit(projectId);
                        cacheInvalidationService.invalidateProjectSchedulesAfterCommit(projectId);
                });
                cacheInvalidationService.invalidateUserWorkAfterCommit(workspaceUserIds);
                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                publishWorkspaceEventAfterCommit(workspaceId, "workspace.deleted", workspaceName);
        }

        private void publishWorkspaceEventAfterCommit(Long workspaceId, String eventType, Object data) {
                afterCommitExecutor.runAfterCommit(() -> realtimeEventPublisherService.publishWorkspaceEvent(workspaceId,
                                eventType, data));
        }
}
