package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.domain.WorkspaceInvite;
import com.devloopsx.chronelis.domain.WorkspaceMember;
import com.devloopsx.chronelis.dto.request.invite.CreateWorkspaceInviteRequest;
import com.devloopsx.chronelis.dto.request.invite.JoinByInviteRequest;
import com.devloopsx.chronelis.dto.response.invite.WorkspaceInviteResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.WorkspaceInviteMapper;
import com.devloopsx.chronelis.repository.WorkspaceInviteRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceInviteServiceImpl implements WorkspaceInviteService {
    WorkspaceInviteRepository workspaceInviteRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    WorkspaceInviteMapper workspaceInviteMapper;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    ActivityLogService activityLogService;
    NotificationService notificationService;
    RealtimeEventPublisherService realtimeEventPublisherService;

    @Override
    @Transactional
    public WorkspaceInviteResponse createInvite(CreateWorkspaceInviteRequest request) {
        collaborationAccessService.ensureCurrentUserIsWorkspaceManager(request.getWorkspaceId());
        Workspace workspace = collaborationAccessService.requireWorkspace(request.getWorkspaceId());

        User currentUser = securityUtils.getAuthenticatedUser();

        WorkspaceMemberRoleType roleToAssign = request.getRoleToAssign() != null
                ? request.getRoleToAssign()
                : WorkspaceMemberRoleType.MEMBER;

        WorkspaceInvite invite = WorkspaceInvite.builder()
                .workspace(workspace)
                .inviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .roleToAssign(roleToAssign)
                .createdBy(currentUser)
                .maxUses(request.getMaxUses())
                .usedCount(0)
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        WorkspaceInvite saved = workspaceInviteRepository.save(invite);

        activityLogService.createLog(workspace.getId(), currentUser.getUserId(),
                ActivityActionType.INVITE_CREATED, ActivityTargetType.INVITE,
                saved.getId(), "Tạo lời mời tham gia workspace " + workspace.getName());

        WorkspaceInviteResponse response = workspaceInviteMapper.toResponse(saved);
        realtimeEventPublisherService.publishWorkspaceEvent(workspace.getId(), "invite.created", response);
        return response;
    }

    @Override
    public List<WorkspaceInviteResponse> listActiveInvites(Long workspaceId) {
        collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
        return workspaceInviteRepository.findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(workspaceId).stream()
                .map(workspaceInviteMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void revokeInvite(Long inviteId) {
        WorkspaceInvite invite = workspaceInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Lời mời không tồn tại"));
        collaborationAccessService.ensureCurrentUserIsWorkspaceManager(invite.getWorkspace().getId());

        invite.setIsActive(false);
        workspaceInviteRepository.save(invite);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(invite.getWorkspace().getId(), userId,
                ActivityActionType.INVITE_REVOKED, ActivityTargetType.INVITE,
                inviteId, "Thu hồi lời mời workspace " + invite.getWorkspace().getName());

        realtimeEventPublisherService.publishWorkspaceEvent(invite.getWorkspace().getId(),
                "invite.revoked", inviteId);
    }

    @Override
    public WorkspaceInviteResponse validateInviteCode(String inviteCode) {
        WorkspaceInvite invite = workspaceInviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Mã mời không hợp lệ hoặc đã hết hạn"));

        validateInvite(invite);
        return workspaceInviteMapper.toResponse(invite);
    }

    @Override
    @Transactional
    public void joinByInvite(JoinByInviteRequest request) {
        WorkspaceInvite invite = workspaceInviteRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Mã mời không hợp lệ hoặc đã hết hạn"));

        validateInvite(invite);

        User currentUser = securityUtils.getAuthenticatedUser();
        Long workspaceId = invite.getWorkspace().getId();

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserUserId(workspaceId, currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Bạn đã là thành viên của workspace này");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(invite.getWorkspace())
                .user(currentUser)
                .role(invite.getRoleToAssign())
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepository.save(member);

        invite.setUsedCount(invite.getUsedCount() + 1);
        if (invite.getMaxUses() != null && invite.getUsedCount() >= invite.getMaxUses()) {
            invite.setIsActive(false);
        }
        workspaceInviteRepository.save(invite);

        activityLogService.createLog(workspaceId, currentUser.getUserId(),
                ActivityActionType.INVITE_USED, ActivityTargetType.INVITE,
                invite.getId(), currentUser.getEmail() + " tham gia workspace qua lời mời");

        if (!invite.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            notificationService.createAndPublish(invite.getCreatedBy().getUserId(),
                    NotificationType.WORKSPACE_INVITE_USED,
                    "Lời mời được sử dụng",
                    currentUser.getEmail() + " đã tham gia workspace " + invite.getWorkspace().getName(),
                    ReferenceType.INVITE, invite.getId());
        }

        realtimeEventPublisherService.publishWorkspaceEvent(workspaceId, "invite.used", workspaceId);
    }

    private void validateInvite(WorkspaceInvite invite) {
        if (!Boolean.TRUE.equals(invite.getIsActive())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA, "Lời mời đã bị thu hồi");
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA, "Lời mời đã hết hạn");
        }
        if (invite.getMaxUses() != null && invite.getUsedCount() >= invite.getMaxUses()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA, "Lời mời đã hết lượt sử dụng");
        }
    }
}
