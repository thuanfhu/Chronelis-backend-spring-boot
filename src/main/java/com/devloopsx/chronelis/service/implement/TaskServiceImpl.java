package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.dto.request.task.*;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskMapper;
import com.devloopsx.chronelis.repository.TaskCommentRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskScheduleRepository;
import com.devloopsx.chronelis.repository.TaskTypeRepository;
import com.devloopsx.chronelis.repository.UserRepository;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {
    TaskCommentRepository taskCommentRepository;
    TaskRepository taskRepository;
    TaskScheduleRepository taskScheduleRepository;
    TaskTypeRepository taskTypeRepository;
    UserRepository userRepository;
    TaskMapper taskMapper;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    NotificationService notificationService;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;
    GoalService goalService;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(request.getProjectId());

        Project project = collaborationAccessService.requireProject(request.getProjectId());
        TaskStatus status = collaborationAccessService.requireTaskStatus(request.getStatusId());
        if (!status.getProject().getId().equals(project.getId())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Task status không thuộc project được chọn");
        }

        Goal goal = null;
        if (request.getGoalId() != null) {
            goal = collaborationAccessService.requireGoal(request.getGoalId());
            if (!goal.getProject().getId().equals(project.getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Goal không thuộc project của task");
            }
        }

        User assignee = null;
        if (request.getAssigneeId() != null && !request.getAssigneeId().isBlank()) {
            collaborationAccessService.ensureAssigneeBelongsWorkspace(request.getAssigneeId(),
                    project.getWorkspace().getId());
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        }

        Task task = taskMapper.toEntity(request);
        User currentUser = securityUtils.getAuthenticatedUser();

        task.setProject(project);
        task.setGoal(goal);
        task.setStatus(status);
        task.setAssignee(assignee);
        task.setCreatedBy(currentUser);
        task.setEstimatedMinutes(request.getEstimatedMinutes() == null ? 0 : request.getEstimatedMinutes());
        task.setSourceView(request.getSourceView() != null ? request.getSourceView() : SourceViewType.KANBAN);

        if (request.getTaskTypeId() != null) {
            TaskType taskType = taskTypeRepository.findById(request.getTaskTypeId())
                    .orElseThrow(
                            () -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task type không tồn tại"));
            if (!taskType.getProject().getId().equals(project.getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Task type không thuộc project được chọn");
            }
            task.setTaskType(taskType);
        }

        if (task.getEstimatedMinutes() < 0) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA, "estimated_minutes không được âm");
        }

        int boardPosition = resolveInsertPosition(status.getId(), request.getBoardPosition());

        shiftRightFromPosition(status.getId(), boardPosition);
        task.setBoardPosition(boardPosition);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        if (Boolean.TRUE.equals(status.getIsClosed())) {
            task.setIsCompleted(true);
            task.setCompletedAt(now);
        } else {
            task.setIsCompleted(false);
            task.setCompletedAt(null);
        }

        Task savedTask = taskRepository.save(task);
        normalizeBoardPositions(status.getId());

        activityLogService.createLog(project.getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_CREATED,
                ActivityTargetType.TASK, savedTask.getId(), "Tạo task " + savedTask.getTitle());

        if (assignee != null && !assignee.getUserId().equals(currentUser.getUserId())) {
            notificationService.createAndPublish(assignee.getUserId(), NotificationType.TASK_ASSIGNED,
                    "Bạn được giao task", "Bạn vừa được giao task: " + savedTask.getTitle(),
                    ReferenceType.TASK, savedTask.getId());
        }

        TaskResponse response = taskMapper.toResponse(savedTask);
        realtimeEventPublisherService.publishTaskEvent(project.getWorkspace().getId(), project.getId(),
                savedTask.getId(),
                "task.created", response);

        if (savedTask.getGoal() != null) {
            goalService.recalculateGoalProgress(savedTask.getGoal().getId());
        }

        return response;
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());
        User currentUser = ensureCurrentUserIsTaskCreator(task);

        if (request.getTitle() == null && request.getGoalId() == null && request.getPriority() == null
                && request.getDueDate() == null && request.getEstimatedMinutes() == null
                && request.getTaskTypeId() == null) {
            throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
        }

        if (request.getEstimatedMinutes() != null && request.getEstimatedMinutes() < 0) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "estimated_minutes không được âm");
        }

        taskMapper.updateEntity(task, request);

        if (request.getGoalId() != null) {
            Goal goal = collaborationAccessService.requireGoal(request.getGoalId());
            if (!goal.getProject().getId().equals(task.getProject().getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Goal không thuộc project của task");
            }
            task.setGoal(goal);
        }

        if (request.getTaskTypeId() != null) {
            TaskType taskType = taskTypeRepository.findById(request.getTaskTypeId())
                    .orElseThrow(
                            () -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task type không tồn tại"));
            if (!taskType.getProject().getId().equals(task.getProject().getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Task type không thuộc project của task");
            }
            task.setTaskType(taskType);
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED, ActivityTargetType.TASK, updatedTask.getId(),
                "Cập nhật task " + updatedTask.getTitle());

        TaskResponse response = taskMapper.toResponse(updatedTask);
        realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                task.getProject().getId(),
                task.getId(), "task.updated", response);
        return response;
    }

    @Override
    public TaskResponse getTask(Long taskId) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());
        return taskMapper.toResponse(task);
    }

    @Override
    public PaginationResponse listTasksByProject(Long projectId, Pageable pageable) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        Page<Task> page = taskRepository.findByProjectId(projectId, pageable);

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .content(page.getContent().stream().map(taskMapper::toResponse).toList())
                .build();
    }

    @Override
    public PaginationResponse listTasksByGoal(Long goalId, Pageable pageable) {
        Goal goal = collaborationAccessService.requireGoal(goalId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(goal.getProject().getId());

        Page<Task> page = taskRepository.findByGoalId(goalId, pageable);
        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .content(page.getContent().stream().map(taskMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long taskId, MoveTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        TaskStatus targetStatus = collaborationAccessService.requireTaskStatus(request.getStatusId());
        if (!targetStatus.getProject().getId().equals(task.getProject().getId())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Task status không thuộc project của task");
        }

        Long sourceStatusId = task.getStatus().getId();
        int sourcePosition = task.getBoardPosition();

        if (!sourceStatusId.equals(targetStatus.getId())) {
            shiftLeftAfterRemoval(sourceStatusId, sourcePosition);
            task.setStatus(targetStatus);

            int targetPosition = resolveInsertPosition(targetStatus.getId(), request.getTargetPosition());
            shiftRightFromPosition(targetStatus.getId(), targetPosition);
            task.setBoardPosition(targetPosition);

            if (Boolean.TRUE.equals(targetStatus.getIsClosed())) {
                task.setIsCompleted(true);
                if (task.getCompletedAt() == null) {
                    task.setCompletedAt(LocalDateTime.now());
                }
            } else {
                task.setIsCompleted(false);
                task.setCompletedAt(null);
            }

            task.setUpdatedAt(LocalDateTime.now());
            Task updatedTask = taskRepository.save(task);
            normalizeBoardPositions(sourceStatusId);
            normalizeBoardPositions(targetStatus.getId());

            User currentUser = securityUtils.getAuthenticatedUser();
            activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                    ActivityActionType.TASK_MOVED_STATUS, ActivityTargetType.TASK, taskId,
                    "Di chuyển task " + updatedTask.getTitle() + " sang cột " + targetStatus.getName());

            if (updatedTask.getAssignee() != null
                    && !updatedTask.getAssignee().getUserId().equals(currentUser.getUserId())) {
                notificationService.createAndPublish(updatedTask.getAssignee().getUserId(),
                        NotificationType.TASK_STATUS_CHANGED,
                        "Task thay đổi trạng thái",
                        "Task " + updatedTask.getTitle() + " đã chuyển sang " + targetStatus.getName(),
                        ReferenceType.TASK, updatedTask.getId());
            }

            TaskResponse response = taskMapper.toResponse(updatedTask);
            realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                    task.getProject().getId(), task.getId(), "task.moved", response);

            if (task.getGoal() != null) {
                goalService.recalculateGoalProgress(task.getGoal().getId());
            }

            return response;
        }

        return reorderTask(taskId, ReorderTaskRequest.builder().targetPosition(request.getTargetPosition()).build());
    }

    @Override
    @Transactional
    public TaskResponse reorderTask(Long taskId, ReorderTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        Long statusId = task.getStatus().getId();
        List<Task> tasks = new ArrayList<>(taskRepository.findByStatusIdOrderByBoardPositionAscIdAsc(statusId));

        tasks.removeIf(item -> item.getId().equals(taskId));

        int targetPosition = request.getTargetPosition() == null ? tasks.size()
                : Math.max(0, Math.min(request.getTargetPosition(), tasks.size()));
        tasks.add(targetPosition, task);

        for (int index = 0; index < tasks.size(); index++) {
            Task item = tasks.get(index);
            item.setBoardPosition(index);
            item.setUpdatedAt(LocalDateTime.now());
        }

        taskRepository.saveAll(tasks);
        Task updatedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task không tồn tại"));

        User currentUser = securityUtils.getAuthenticatedUser();
        activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_REORDERED, ActivityTargetType.TASK, taskId,
                "Reorder task " + updatedTask.getTitle());

        TaskResponse response = taskMapper.toResponse(updatedTask);
        realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.reordered", response);
        return response;
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        String oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getUserId() : null;
        User currentUser = securityUtils.getAuthenticatedUser();

        if (request.getAssigneeId() == null || request.getAssigneeId().isBlank()) {
            task.setAssignee(null);
            task.setUpdatedAt(LocalDateTime.now());
            Task updatedTask = taskRepository.save(task);

            activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                    ActivityActionType.TASK_UNASSIGNED, ActivityTargetType.TASK, taskId,
                    "Bỏ gán task " + updatedTask.getTitle());

            TaskResponse response = taskMapper.toResponse(updatedTask);
            realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                    task.getProject().getId(), task.getId(), "task.unassigned", response);
            return response;
        }

        collaborationAccessService.ensureAssigneeBelongsWorkspace(request.getAssigneeId(),
                task.getProject().getWorkspace().getId());
        User newAssignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        task.setAssignee(newAssignee);
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_ASSIGNED, ActivityTargetType.TASK, taskId,
                "Giao task " + updatedTask.getTitle() + " cho " + newAssignee.getEmail());

        if (!newAssignee.getUserId().equals(currentUser.getUserId())
                && !Objects.equals(oldAssigneeId, newAssignee.getUserId())) {
            notificationService.createAndPublish(newAssignee.getUserId(), NotificationType.TASK_ASSIGNED,
                    "Bạn được giao task", "Bạn vừa được giao task: " + updatedTask.getTitle(),
                    ReferenceType.TASK, updatedTask.getId());
        }

        TaskResponse response = taskMapper.toResponse(updatedTask);
        realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.assigned", response);
        return response;
    }

    @Override
    @Transactional
    public TaskResponse updateTaskCompletion(Long taskId, UpdateTaskCompletionRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        task.setIsCompleted(request.getIsCompleted());
        task.setCompletedAt(Boolean.TRUE.equals(request.getIsCompleted()) ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        User currentUser = securityUtils.getAuthenticatedUser();

        activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED, ActivityTargetType.TASK, taskId,
                "Cập nhật trạng thái hoàn thành task " + updatedTask.getTitle());

        TaskResponse response = taskMapper.toResponse(updatedTask);
        realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.completion-updated", response);

        if (task.getGoal() != null) {
            goalService.recalculateGoalProgress(task.getGoal().getId());
        }

        return response;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());
        User currentUser = ensureCurrentUserIsTaskCreator(task);

        Long workspaceId = task.getProject().getWorkspace().getId();
        Long projectId = task.getProject().getId();
        Long statusId = task.getStatus().getId();
        int boardPosition = task.getBoardPosition();
        String title = task.getTitle();
        Long goalId = task.getGoal() != null ? task.getGoal().getId() : null;

        // Defensive cleanup in case DB foreign keys are not configured with CASCADE.
        taskCommentRepository.deleteByTaskId(taskId);
        taskScheduleRepository.deleteByTaskId(taskId);

        taskRepository.delete(task);
        shiftLeftAfterRemoval(statusId, boardPosition);
        normalizeBoardPositions(statusId);

        activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.TASK_DELETED,
                ActivityTargetType.TASK, taskId,
                "Xóa task " + title);

        realtimeEventPublisherService.publishTaskEvent(workspaceId, projectId, taskId, "task.deleted", taskId);

        if (goalId != null) {
            goalService.recalculateGoalProgress(goalId);
        }
    }

    private User ensureCurrentUserIsTaskCreator(Task task) {
        User currentUser = securityUtils.getAuthenticatedUser();
        if (!Objects.equals(task.getCreatedBy().getUserId(), currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Chỉ người tạo task mới có quyền chỉnh sửa hoặc xóa task này");
        }
        return currentUser;
    }

    private int getEndPosition(Long statusId) {
        Integer maxPosition = taskRepository.findMaxBoardPositionByStatusId(statusId);
        return (maxPosition == null ? -1 : maxPosition) + 1;
    }

    private int resolveInsertPosition(Long statusId, Integer requestedPosition) {
        int size = (int) taskRepository.countByStatusId(statusId);
        if (requestedPosition == null) {
            return size;
        }
        return Math.max(0, Math.min(requestedPosition, size));
    }

    private void shiftRightFromPosition(Long statusId, int fromPosition) {
        List<Task> toShift = taskRepository
                .findByStatusIdAndBoardPositionGreaterThanEqualOrderByBoardPositionAscIdAsc(statusId, fromPosition);
        for (Task item : toShift) {
            item.setBoardPosition(item.getBoardPosition() + 1);
        }
        taskRepository.saveAll(toShift);
    }

    private void shiftLeftAfterRemoval(Long statusId, int removedPosition) {
        List<Task> toShift = taskRepository
                .findByStatusIdAndBoardPositionGreaterThanOrderByBoardPositionAscIdAsc(statusId, removedPosition);
        for (Task item : toShift) {
            item.setBoardPosition(Math.max(0, item.getBoardPosition() - 1));
        }
        taskRepository.saveAll(toShift);
    }

    private void normalizeBoardPositions(Long statusId) {
        List<Task> tasks = taskRepository.findByStatusIdOrderByBoardPositionAscIdAsc(statusId);
        for (int index = 0; index < tasks.size(); index++) {
            tasks.get(index).setBoardPosition(index);
        }
        taskRepository.saveAll(tasks);
    }
}
