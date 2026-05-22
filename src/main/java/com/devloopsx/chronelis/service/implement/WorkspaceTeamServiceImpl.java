package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.domain.WorkspaceTeam;
import com.devloopsx.chronelis.domain.WorkspaceTeamMember;
import com.devloopsx.chronelis.dto.request.team.AddTeamMemberRequest;
import com.devloopsx.chronelis.dto.request.team.CreateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.request.team.UpdateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamMemberResponse;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.WorkspaceTeamMapper;
import com.devloopsx.chronelis.mapper.WorkspaceTeamMemberMapper;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamMemberRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamRepository;
import com.devloopsx.chronelis.service.ActivityLogService;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.RealtimeEventPublisherService;
import com.devloopsx.chronelis.service.WorkspaceTeamService;
import com.devloopsx.chronelis.service.cache.AfterCommitExecutor;
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkspaceTeamServiceImpl implements WorkspaceTeamService {
        WorkspaceTeamRepository workspaceTeamRepository;
        WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
        UserRepository userRepository;
        WorkspaceTeamMapper workspaceTeamMapper;
        WorkspaceTeamMemberMapper workspaceTeamMemberMapper;
        CollaborationAccessService collaborationAccessService;
        SecurityUtils securityUtils;
        ActivityLogService activityLogService;
        RealtimeEventPublisherService realtimeEventPublisherService;
        CacheInvalidationService cacheInvalidationService;
        AfterCommitExecutor afterCommitExecutor;

        @Override
        @Transactional
        public WorkspaceTeamResponse createTeam(CreateWorkspaceTeamRequest request) {
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(request.getWorkspaceId());
                Workspace workspace = collaborationAccessService.requireWorkspace(request.getWorkspaceId());

                if (workspaceTeamRepository.existsByWorkspaceIdAndNameIgnoreCase(request.getWorkspaceId(),
                                request.getName())) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Team với tên này đã tồn tại trong workspace");
                }

                User currentUser = securityUtils.getAuthenticatedUser();
                LocalDateTime now = LocalDateTime.now();

                WorkspaceTeam team = WorkspaceTeam.builder()
                                .workspace(workspace)
                                .name(request.getName())
                                .description(request.getDescription())
                                .createdBy(currentUser)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                WorkspaceTeam saved = workspaceTeamRepository.save(team);

                activityLogService.createLog(workspace.getId(), currentUser.getUserId(),
                                ActivityActionType.TEAM_CREATED, ActivityTargetType.TEAM,
                                saved.getId(), "Tạo team " + saved.getName());

                WorkspaceTeamResponse response = workspaceTeamMapper.toResponse(saved);
                publishWorkspaceEventAfterCommit(workspace.getId(), "team.created", response);
                return response;
        }

        @Override
        @Transactional
        public WorkspaceTeamResponse updateTeam(Long teamId, UpdateWorkspaceTeamRequest request) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(team.getWorkspace().getId());

                if (request.getName() != null && workspaceTeamRepository.existsByWorkspaceIdAndNameIgnoreCaseAndIdNot(
                                team.getWorkspace().getId(), request.getName(), teamId)) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Team với tên này đã tồn tại trong workspace");
                }

                if (request.getName() != null)
                        team.setName(request.getName());
                if (request.getDescription() != null)
                        team.setDescription(request.getDescription());
                team.setUpdatedAt(LocalDateTime.now());

                WorkspaceTeam updated = workspaceTeamRepository.save(team);

                String userId = securityUtils.getAuthenticatedUser().getUserId();
                activityLogService.createLog(team.getWorkspace().getId(), userId,
                                ActivityActionType.TEAM_UPDATED, ActivityTargetType.TEAM,
                                updated.getId(), "Cập nhật team " + updated.getName());

                WorkspaceTeamResponse response = workspaceTeamMapper.toResponse(updated);
                publishWorkspaceEventAfterCommit(team.getWorkspace().getId(), "team.updated",
                                response);
                return response;
        }

        @Override
        public List<WorkspaceTeamResponse> listByWorkspace(Long workspaceId) {
                collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
                return workspaceTeamRepository.findByWorkspaceIdOrderByNameAsc(workspaceId).stream()
                                .map(workspaceTeamMapper::toResponse).toList();
        }

        @Override
        public WorkspaceTeamResponse getTeam(Long teamId) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                collaborationAccessService.requireCurrentWorkspaceMember(team.getWorkspace().getId());
                return workspaceTeamMapper.toResponse(team);
        }

        @Override
        @Transactional
        public void deleteTeam(Long teamId) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(team.getWorkspace().getId());

                Long workspaceId = team.getWorkspace().getId();
                String name = team.getName();

                // Defensive cleanup in case DB foreign keys are not configured with CASCADE.
                workspaceTeamMemberRepository.deleteByTeamId(teamId);

                workspaceTeamRepository.delete(team);

                String userId = securityUtils.getAuthenticatedUser().getUserId();
                activityLogService.createLog(workspaceId, userId,
                                ActivityActionType.TEAM_DELETED, ActivityTargetType.TEAM,
                                teamId, "Xóa team " + name);

                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                publishWorkspaceEventAfterCommit(workspaceId, "team.deleted", teamId);
        }

        @Override
        @Transactional
        public WorkspaceTeamMemberResponse addMember(Long teamId, AddTeamMemberRequest request) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                Long workspaceId = team.getWorkspace().getId();
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);

                collaborationAccessService.ensureAssigneeBelongsWorkspace(request.getUserId(), workspaceId);

                if (workspaceTeamMemberRepository.existsByTeamIdAndUserUserId(teamId, request.getUserId())) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Người dùng đã là thành viên của team này");
                }

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

                WorkspaceTeamMember member = WorkspaceTeamMember.builder()
                                .team(team)
                                .user(user)
                                .joinedAt(LocalDateTime.now())
                                .build();

                WorkspaceTeamMember saved = workspaceTeamMemberRepository.save(member);

                String currentUserId = securityUtils.getAuthenticatedUser().getUserId();
                activityLogService.createLog(workspaceId, currentUserId,
                                ActivityActionType.TEAM_MEMBER_ADDED, ActivityTargetType.TEAM,
                                teamId, "Thêm thành viên vào team " + team.getName());

                WorkspaceTeamMemberResponse response = workspaceTeamMemberMapper.toResponse(saved);
                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                cacheInvalidationService.invalidateUserWorkAfterCommit(request.getUserId());
                publishWorkspaceEventAfterCommit(workspaceId, "team.memberAdded", response);
                return response;
        }

        @Override
        @Transactional
        public void removeMember(Long teamId, String userId) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                Long workspaceId = team.getWorkspace().getId();
                collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(workspaceId);

                if (!workspaceTeamMemberRepository.existsByTeamIdAndUserUserId(teamId, userId)) {
                        throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                        "Người dùng không phải là thành viên team");
                }

                workspaceTeamMemberRepository.deleteByTeamIdAndUserUserId(teamId, userId);

                String currentUserId = securityUtils.getAuthenticatedUser().getUserId();
                activityLogService.createLog(workspaceId, currentUserId,
                                ActivityActionType.TEAM_MEMBER_REMOVED, ActivityTargetType.TEAM,
                                teamId, "Xóa thành viên khỏi team " + team.getName());

                cacheInvalidationService.invalidateWorkspaceAccessAfterCommit(workspaceId);
                cacheInvalidationService.invalidateUserWorkAfterCommit(userId);
                publishWorkspaceEventAfterCommit(workspaceId, "team.memberRemoved", teamId);
        }

        @Override
        public List<WorkspaceTeamMemberResponse> listMembers(Long teamId) {
                WorkspaceTeam team = workspaceTeamRepository.findById(teamId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Team không tồn tại"));
                collaborationAccessService.requireCurrentWorkspaceMember(team.getWorkspace().getId());

                return workspaceTeamMemberRepository.findByTeamIdOrderByJoinedAtAsc(teamId).stream()
                                .map(workspaceTeamMemberMapper::toResponse).toList();
        }

        private void publishWorkspaceEventAfterCommit(Long workspaceId, String eventType, Object data) {
                afterCommitExecutor.runAfterCommit(() -> realtimeEventPublisherService.publishWorkspaceEvent(workspaceId,
                                eventType, data));
        }
}
