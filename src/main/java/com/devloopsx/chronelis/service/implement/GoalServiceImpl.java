package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Goal;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.WorkspaceTeam;
import com.devloopsx.chronelis.dto.request.goal.CreateGoalRequest;
import com.devloopsx.chronelis.dto.request.goal.UpdateGoalRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.goal.GoalResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.GoalMapper;
import com.devloopsx.chronelis.repository.GoalRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskTypeRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalServiceImpl implements GoalService {
    GoalRepository goalRepository;
    TaskRepository taskRepository;
    TaskTypeRepository taskTypeRepository;
    UserRepository userRepository;
    WorkspaceTeamRepository workspaceTeamRepository;
    GoalMapper goalMapper;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {
        collaborationAccessService.ensureCurrentUserCanManageProjectWork(request.getProjectId());
        Project project = collaborationAccessService.requireProject(request.getProjectId());

        if (request.getManagerUserId() != null || request.getManagerTeamId() != null) {
            collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(project.getWorkspace().getId());
        }

        Goal goal = goalMapper.toEntity(request);
        User currentUser = securityUtils.getAuthenticatedUser();
        LocalDateTime now = LocalDateTime.now();

        goal.setProject(project);
        goal.setCreatedBy(currentUser);
        goal.setStatus(request.getStatus() == null ? GoalStatusType.NOT_STARTED : request.getStatus());
        goal.setProgressPercent(request.getProgressPercent() == null ? BigDecimal.ZERO : request.getProgressPercent());
        goal.setCreatedAt(now);
        goal.setUpdatedAt(now);

        applyGoalManagerAssignments(goal, project.getWorkspace().getId(), request.getManagerUserId(),
                request.getManagerTeamId());

        validateProgress(goal.getProgressPercent());

        Goal savedGoal = goalRepository.save(goal);

        activityLogService.createLog(project.getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.GOAL_CREATED,
                ActivityTargetType.GOAL, savedGoal.getId(),
                "Tạo goal " + savedGoal.getTitle());

        GoalResponse response = goalMapper.toResponse(savedGoal);
        realtimeEventPublisherService.publishProjectEvent(project.getWorkspace().getId(), project.getId(),
                "goal.created", response);
        return response;
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request) {
        Goal goal = collaborationAccessService.requireGoal(goalId);
        collaborationAccessService.ensureCurrentUserCanManageGoal(goalId);

        boolean managerUpdateRequested = request.getManagerUserId() != null || request.getManagerTeamId() != null;
        if (managerUpdateRequested) {
            collaborationAccessService.ensureCurrentUserIsWorkspaceOwner(goal.getProject().getWorkspace().getId());
        }

        if (request.getTitle() == null && request.getGoalType() == null && request.getStatus() == null
                && request.getProgressPercent() == null && !managerUpdateRequested) {
            throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
        }

        goalMapper.updateEntity(goal, request);

        if (managerUpdateRequested) {
            applyGoalManagerAssignments(goal, goal.getProject().getWorkspace().getId(), request.getManagerUserId(),
                    request.getManagerTeamId());
        }

        if (request.getProgressPercent() != null) {
            validateProgress(request.getProgressPercent());
        }

        goal.setUpdatedAt(LocalDateTime.now());
        Goal updatedGoal = goalRepository.save(goal);
        User currentUser = securityUtils.getAuthenticatedUser();

        activityLogService.createLog(goal.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.GOAL_UPDATED, ActivityTargetType.GOAL, updatedGoal.getId(),
                "Cập nhật goal " + updatedGoal.getTitle());

        GoalResponse response = goalMapper.toResponse(updatedGoal);
        realtimeEventPublisherService.publishProjectEvent(goal.getProject().getWorkspace().getId(),
                goal.getProject().getId(),
                "goal.updated", response);
        return response;
    }

    @Override
    public GoalResponse getGoal(Long goalId) {
        Goal goal = collaborationAccessService.requireGoal(goalId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(goal.getProject().getId());
        return goalMapper.toResponse(goal);
    }

    @Override
    public PaginationResponse listGoalsByProject(Long projectId, Pageable pageable) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        Page<Goal> page = goalRepository.findByProjectId(projectId, pageable);

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .content(page.getContent().stream().map(goalMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        Goal goal = collaborationAccessService.requireGoal(goalId);
        collaborationAccessService.ensureCurrentUserCanManageGoal(goalId);

        Long workspaceId = goal.getProject().getWorkspace().getId();
        Long projectId = goal.getProject().getId();
        User currentUser = securityUtils.getAuthenticatedUser();

        // Defensive cleanup in case DB foreign keys are not configured with SET NULL.
        taskRepository.clearGoalReferences(goalId);
        taskTypeRepository.clearGoalReferences(goalId);

        goalRepository.delete(goal);

        activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.GOAL_DELETED,
                ActivityTargetType.GOAL, goalId,
                "Xóa goal " + goal.getTitle());

        realtimeEventPublisherService.publishProjectEvent(workspaceId, projectId, "goal.deleted", goalId);
    }

    @Override
    @Transactional
    public void recalculateGoalProgress(Long goalId) {
        Goal goal = collaborationAccessService.requireGoal(goalId);
        long total = taskRepository.countByGoalId(goalId);
        if (total == 0) {
            goal.setProgressPercent(BigDecimal.ZERO);
        } else {
            long completed = taskRepository.countByGoalIdAndIsCompletedTrue(goalId);
            BigDecimal percent = BigDecimal.valueOf(completed)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
            goal.setProgressPercent(percent);
        }
        goal.setUpdatedAt(LocalDateTime.now());
        Goal saved = goalRepository.save(goal);

        GoalResponse response = goalMapper.toResponse(saved);
        realtimeEventPublisherService.publishProjectEvent(
                goal.getProject().getWorkspace().getId(),
                goal.getProject().getId(),
                "goal.updated", response);
    }

    private void validateProgress(BigDecimal progress) {
        if (progress == null) {
            return;
        }

        if (progress.compareTo(BigDecimal.ZERO) < 0 || progress.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Tiến độ goal phải nằm trong khoảng 0..100");
        }
    }

    private void applyGoalManagerAssignments(Goal goal, Long workspaceId, String managerUserId, Long managerTeamId) {
        if (managerUserId != null) {
            if (managerUserId.isBlank()) {
                goal.setManagerUser(null);
            } else {
                collaborationAccessService.ensureAssigneeBelongsWorkspace(managerUserId, workspaceId);
                User managerUser = userRepository.findById(managerUserId)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
                goal.setManagerUser(managerUser);
            }
        }

        if (managerTeamId != null) {
            if (managerTeamId <= 0) {
                goal.setManagerTeam(null);
            } else {
                WorkspaceTeam managerTeam = workspaceTeamRepository.findById(managerTeamId)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                "Team manager không tồn tại"));

                if (!managerTeam.getWorkspace().getId().equals(workspaceId)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                            "Manager team phải thuộc cùng workspace");
                }

                goal.setManagerTeam(managerTeam);
            }
        }
    }
}
