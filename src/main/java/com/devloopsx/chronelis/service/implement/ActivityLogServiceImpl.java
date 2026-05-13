package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.domain.ActivityLog;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.ActivityLogMapper;
import com.devloopsx.chronelis.repository.ActivityLogRepository;
import com.devloopsx.chronelis.repository.GoalRepository;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.TaskCommentRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskScheduleRepository;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.repository.TaskTypeRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceRepository;
import com.devloopsx.chronelis.service.ActivityLogService;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogServiceImpl implements ActivityLogService {
    private static final Set<ActivityTargetType> PROJECT_SCOPED_TARGETS = EnumSet.of(
            ActivityTargetType.PROJECT,
            ActivityTargetType.GOAL,
            ActivityTargetType.TASK,
            ActivityTargetType.COMMENT,
            ActivityTargetType.SCHEDULE,
            ActivityTargetType.STATUS,
            ActivityTargetType.TASK_TYPE,
            ActivityTargetType.CHECK_ITEM);

    ActivityLogRepository activityLogRepository;
    WorkspaceRepository workspaceRepository;
    UserRepository userRepository;
    ProjectRepository projectRepository;
    GoalRepository goalRepository;
    TaskRepository taskRepository;
    TaskCommentRepository taskCommentRepository;
    TaskScheduleRepository taskScheduleRepository;
    TaskStatusRepository taskStatusRepository;
    TaskTypeRepository taskTypeRepository;
    ActivityLogMapper activityLogMapper;
    CollaborationAccessService collaborationAccessService;
    ProjectPermissionService projectPermissionService;

    @Override
    public PaginationResponse listByWorkspace(Long workspaceId, String actorId, ActivityActionType actionType,
            ActivityTargetType targetType, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable) {
        collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);

        Specification<ActivityLog> spec = (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);

        if (actorId != null && !actorId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actor").get("userId"), actorId));
        }
        if (actionType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actionType"), actionType));
        }
        if (targetType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType));
        }
        if (fromDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }
        if (toDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
        }

        Page<ActivityLog> page = activityLogRepository.findAll(spec, pageable);

        List<ActivityLog> visibleLogs = page.getContent().stream()
                .filter(this::isVisibleToCurrentUser)
                .toList();

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(visibleLogs.isEmpty() ? 0 : 1)
                        .totalElements((long) visibleLogs.size())
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .content(visibleLogs.stream().map(activityLogMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public void createLog(Long workspaceId, String actorId, ActivityActionType actionType,
            ActivityTargetType targetType,
            Long targetId, String description) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Workspace không tồn tại"));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        ActivityLog activityLog = ActivityLog.builder()
                .workspace(workspace)
                .actor(actor)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        activityLogRepository.save(activityLog);
    }

    private boolean isVisibleToCurrentUser(ActivityLog activityLog) {
        if (!PROJECT_SCOPED_TARGETS.contains(activityLog.getTargetType())) {
            return true;
        }

        return resolveProject(activityLog)
                .map(project -> projectPermissionService.resolveCurrentUserRole(project)
                        .atLeast(EffectiveProjectAccessRoleType.VIEWER))
                .orElse(false);
    }

    private Optional<Project> resolveProject(ActivityLog activityLog) {
        Long targetId = activityLog.getTargetId();
        return switch (activityLog.getTargetType()) {
            case PROJECT -> projectRepository.findById(targetId);
            case GOAL -> goalRepository.findById(targetId).map(goal -> goal.getProject());
            case TASK, CHECK_ITEM -> taskRepository.findById(targetId).map(task -> task.getProject());
            case COMMENT -> taskCommentRepository.findById(targetId).map(comment -> comment.getTask().getProject());
            case SCHEDULE -> taskScheduleRepository.findById(targetId).map(schedule -> schedule.getTask().getProject());
            case STATUS -> taskStatusRepository.findById(targetId).map(status -> status.getProject());
            case TASK_TYPE -> taskTypeRepository.findById(targetId).map(taskType -> taskType.getProject());
            default -> Optional.empty();
        };
    }
}
