package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.dto.request.task.*;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.task.MyWorkResponse;
import com.devloopsx.chronelis.dto.response.task.MyWorkScheduleItemResponse;
import com.devloopsx.chronelis.dto.response.task.TaskAnalyticsResponse;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskMapper;
import com.devloopsx.chronelis.repository.TaskCommentRepository;
import com.devloopsx.chronelis.repository.TaskDependencyRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskScheduleRepository;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.repository.TaskTypeRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.service.cache.AfterCommitExecutor;
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
import com.devloopsx.chronelis.service.cache.CacheKeys;
import com.devloopsx.chronelis.service.cache.RedisCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskServiceImpl implements TaskService {
    TaskCommentRepository taskCommentRepository;
    TaskDependencyRepository taskDependencyRepository;
    TaskRepository taskRepository;
    TaskScheduleRepository taskScheduleRepository;
    TaskStatusRepository taskStatusRepository;
    TaskTypeRepository taskTypeRepository;
    UserRepository userRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    TaskMapper taskMapper;
    TaskDependencyService taskDependencyService;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    NotificationService notificationService;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;
    GoalService goalService;
    ProjectPermissionService projectPermissionService;
    RedisCacheService redisCacheService;
    CacheInvalidationService cacheInvalidationService;
    AfterCommitExecutor afterCommitExecutor;

    static final Duration MY_WORK_TTL = Duration.ofSeconds(60);
    static final Duration TASK_ANALYTICS_TTL = Duration.ofMinutes(3);

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        collaborationAccessService.ensureCurrentUserCanContributeToProject(request.getProjectId());

        Project project = collaborationAccessService.requireProject(request.getProjectId());
        User currentUser = securityUtils.getAuthenticatedUser();
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
            if (!request.getAssigneeId().equals(currentUser.getUserId())) {
                collaborationAccessService.ensureCurrentUserCanAssignOthers(project.getId());
            }
            collaborationAccessService.ensureAssigneeBelongsWorkspace(request.getAssigneeId(),
                    project.getWorkspace().getId());
            ensureAssigneeCanAccessProject(project, request.getAssigneeId());
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        }

        Task task = taskMapper.toEntity(request);

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
            task.setLastOpenStatus(null);
        } else {
            task.setIsCompleted(false);
            task.setCompletedAt(null);
            task.setLastOpenStatus(status);
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

        TaskResponse response = toTaskResponse(savedTask);
        invalidateTaskMutationAfterCommit(project.getId(), assigneeIds(savedTask));
        publishTaskEventAfterCommit(project.getWorkspace().getId(), project.getId(),
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
        collaborationAccessService.ensureCurrentUserCanManageTask(taskId);
        User currentUser = securityUtils.getAuthenticatedUser();
        Long previousGoalId = task.getGoal() != null ? task.getGoal().getId() : null;

        if (request.getTitle() == null && request.getDescription() == null && request.getGoalId() == null
                && !Boolean.TRUE.equals(request.getClearGoal()) && request.getPriority() == null
                && request.getDueDate() == null && request.getEstimatedMinutes() == null
                && request.getTaskTypeId() == null && request.getNotesHtml() == null) {
            throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
        }

        if (Boolean.TRUE.equals(request.getClearGoal()) && request.getGoalId() != null) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Không thể vừa chọn goal mới vừa yêu cầu bỏ liên kết goal");
        }

        if (request.getEstimatedMinutes() != null && request.getEstimatedMinutes() < 0) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "estimated_minutes không được âm");
        }

        taskMapper.updateEntity(task, request);

        if (Boolean.TRUE.equals(request.getClearGoal())) {
            task.setGoal(null);
        }

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

        Long nextGoalId = updatedTask.getGoal() != null ? updatedTask.getGoal().getId() : null;
        if (!Objects.equals(previousGoalId, nextGoalId)) {
            if (previousGoalId != null) {
                goalService.recalculateGoalProgress(previousGoalId);
            }
            if (nextGoalId != null) {
                goalService.recalculateGoalProgress(nextGoalId);
            }
        }

        TaskResponse response = toTaskResponse(updatedTask);
        invalidateTaskMutationAfterCommit(task.getProject().getId(), assigneeIds(updatedTask));
        publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                task.getProject().getId(),
                task.getId(), "task.updated", response);
        return response;
    }

    @Override
    public TaskResponse getTask(Long taskId) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());
        return toTaskResponse(task);
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
                .content(toTaskResponses(page.getContent()))
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
                .content(toTaskResponses(page.getContent()))
                .build();
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long taskId, MoveTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanManageTask(taskId);

        TaskStatus targetStatus = collaborationAccessService.requireTaskStatus(request.getStatusId());
        if (!targetStatus.getProject().getId().equals(task.getProject().getId())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Task status không thuộc project của task");
        }

        TaskStatus sourceStatus = task.getStatus();
        Long sourceStatusId = task.getStatus().getId();
        int sourcePosition = task.getBoardPosition();

        if (!sourceStatusId.equals(targetStatus.getId())) {
            shiftLeftAfterRemoval(sourceStatusId, sourcePosition);
            task.setStatus(targetStatus);

            int targetPosition = resolveInsertPosition(targetStatus.getId(), request.getTargetPosition());
            shiftRightFromPosition(targetStatus.getId(), targetPosition);
            task.setBoardPosition(targetPosition);

            if (Boolean.TRUE.equals(targetStatus.getIsClosed())) {
                if (!Boolean.TRUE.equals(sourceStatus.getIsClosed())) {
                    task.setLastOpenStatus(sourceStatus);
                }
                task.setIsCompleted(true);
                if (task.getCompletedAt() == null) {
                    task.setCompletedAt(LocalDateTime.now());
                }
            } else {
                task.setLastOpenStatus(targetStatus);
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

            TaskResponse response = toTaskResponse(updatedTask);
            invalidateTaskMutationAfterCommit(task.getProject().getId(), assigneeIds(updatedTask));
            publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
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
        collaborationAccessService.ensureCurrentUserCanManageTask(taskId);

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

        TaskResponse response = toTaskResponse(updatedTask);
        invalidateTaskMutationAfterCommit(task.getProject().getId(), assigneeIds(updatedTask));
        publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.reordered", response);
        return response;
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanManageTask(taskId);

        String oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getUserId() : null;
        User currentUser = securityUtils.getAuthenticatedUser();

        if (request.getAssigneeId() == null || request.getAssigneeId().isBlank()) {
            if (!Objects.equals(oldAssigneeId, currentUser.getUserId())) {
                collaborationAccessService.ensureCurrentUserCanAssignOthers(task.getProject().getId());
            }
            task.setAssignee(null);
            task.setUpdatedAt(LocalDateTime.now());
            Task updatedTask = taskRepository.save(task);

            activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                    ActivityActionType.TASK_UNASSIGNED, ActivityTargetType.TASK, taskId,
                    "Bỏ gán task " + updatedTask.getTitle());

            TaskResponse response = toTaskResponse(updatedTask);
            invalidateTaskMutationAfterCommit(task.getProject().getId(), oldAssigneeId == null ? Set.of() : Set.of(oldAssigneeId));
            publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                    task.getProject().getId(), task.getId(), "task.unassigned", response);
            return response;
        }

        collaborationAccessService.ensureAssigneeBelongsWorkspace(request.getAssigneeId(),
                task.getProject().getWorkspace().getId());
        ensureAssigneeCanAccessProject(task.getProject(), request.getAssigneeId());
        if (!request.getAssigneeId().equals(currentUser.getUserId())) {
            collaborationAccessService.ensureCurrentUserCanAssignOthers(task.getProject().getId());
        }
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

        TaskResponse response = toTaskResponse(updatedTask);
        Set<String> affectedAssignees = new HashSet<>();
        if (oldAssigneeId != null) {
            affectedAssignees.add(oldAssigneeId);
        }
        affectedAssignees.add(newAssignee.getUserId());
        invalidateTaskMutationAfterCommit(task.getProject().getId(), affectedAssignees);
        publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.assigned", response);
        return response;
    }

    @Override
    @Transactional
    public TaskResponse updateTaskCompletion(Long taskId, UpdateTaskCompletionRequest request) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanManageTask(taskId);

        boolean nextCompleted = Boolean.TRUE.equals(request.getIsCompleted());
        TaskStatus sourceStatus = task.getStatus();
        TaskStatus targetStatus = sourceStatus;

        if (nextCompleted && !Boolean.TRUE.equals(sourceStatus.getIsClosed())) {
            targetStatus = resolveFirstClosedStatus(task.getProject().getId());
        }

        if (!nextCompleted && Boolean.TRUE.equals(sourceStatus.getIsClosed())) {
            targetStatus = resolveRestoreOpenStatus(task);
        }

        boolean statusChanged = !sourceStatus.getId().equals(targetStatus.getId());
        if (statusChanged) {
            shiftLeftAfterRemoval(sourceStatus.getId(), task.getBoardPosition());
            task.setStatus(targetStatus);
            task.setBoardPosition(getEndPosition(targetStatus.getId()));
        }

        LocalDateTime now = LocalDateTime.now();
        if (nextCompleted) {
            if (!Boolean.TRUE.equals(sourceStatus.getIsClosed())) {
                task.setLastOpenStatus(sourceStatus);
            }
            task.setIsCompleted(true);
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(now);
            }
        } else {
            if (!Boolean.TRUE.equals(targetStatus.getIsClosed())) {
                task.setLastOpenStatus(targetStatus);
            }
            task.setIsCompleted(false);
            task.setCompletedAt(null);
        }

        task.setUpdatedAt(now);
        Task updatedTask = taskRepository.save(task);

        if (statusChanged) {
            normalizeBoardPositions(sourceStatus.getId());
            normalizeBoardPositions(targetStatus.getId());
        }

        User currentUser = securityUtils.getAuthenticatedUser();

        activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED, ActivityTargetType.TASK, taskId,
                "Cập nhật trạng thái hoàn thành task " + updatedTask.getTitle());

        TaskResponse response = toTaskResponse(updatedTask);
        invalidateTaskMutationAfterCommit(task.getProject().getId(), assigneeIds(updatedTask));
        publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), task.getId(), "task.completion-updated", response);

        if (task.getGoal() != null) {
            goalService.recalculateGoalProgress(task.getGoal().getId());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MyWorkResponse getMyWork() {
        User currentUser = securityUtils.getAuthenticatedUser();
        long version = redisCacheService.getVersion(CacheKeys.userWorkVersion(currentUser.getUserId()));
        String key = CacheKeys.myWork(currentUser.getUserId(), version);
        return redisCacheService.getJson(key, MyWorkResponse.class)
                .orElseGet(() -> {
                    MyWorkResponse response = buildMyWork(currentUser);
                    redisCacheService.setJson(key, response, MY_WORK_TTL);
                    return response;
                });
    }

    private MyWorkResponse buildMyWork(User currentUser) {
        List<Long> workspaceIds = workspaceMemberRepository.findByUserUserId(currentUser.getUserId()).stream()
                .map(member -> member.getWorkspace().getId())
                .distinct()
                .toList();

        if (workspaceIds.isEmpty()) {
            return MyWorkResponse.builder()
                    .assignedCount(0)
                    .blockedCount(0)
                    .overdueCount(0)
                    .dueTodayCount(0)
                    .highPriorityCount(0)
                    .upcomingScheduledCount(0)
                    .generatedAt(LocalDateTime.now())
                    .build();
        }

        List<Task> assignedTasks = taskRepository.findVisibleAssignedOpenTasks(
                currentUser.getUserId(),
                workspaceIds,
                PageRequest.of(0, 500));
        List<TaskResponse> assignedTaskResponses = toTaskResponses(assignedTasks);

        Map<Long, TaskResponse> taskResponseById = new LinkedHashMap<>();
        for (TaskResponse taskResponse : assignedTaskResponses) {
            taskResponseById.put(taskResponse.getId(), taskResponse);
        }

        LocalDate today = LocalDate.now();
        List<MyWorkScheduleItemResponse> upcomingSchedules = taskScheduleRepository
                .findVisibleAssignedOpenSchedules(
                        currentUser.getUserId(),
                        workspaceIds,
                        today,
                        today.plusDays(14),
                        PageRequest.of(0, 80))
                .stream()
                .map(schedule -> {
                    TaskResponse taskResponse = taskResponseById.computeIfAbsent(schedule.getTask().getId(),
                            ignored -> toTaskResponse(schedule.getTask()));
                    return MyWorkScheduleItemResponse.builder()
                            .scheduleId(schedule.getId())
                            .taskId(schedule.getTask().getId())
                            .scheduledStart(schedule.getScheduledStart())
                            .scheduledEnd(schedule.getScheduledEnd())
                            .task(taskResponse)
                            .build();
                })
                .toList();

        int blockedCount = 0;
        int overdueCount = 0;
        int dueTodayCount = 0;
        int highPriorityCount = 0;

        for (TaskResponse taskResponse : assignedTaskResponses) {
            if (Boolean.TRUE.equals(taskResponse.getBlocked())) {
                blockedCount++;
            }

            if (taskResponse.getDueDate() != null) {
                LocalDate dueDate = taskResponse.getDueDate().toLocalDate();
                if (dueDate.isBefore(today)) {
                    overdueCount++;
                } else if (dueDate.isEqual(today)) {
                    dueTodayCount++;
                }
            }

            if (taskResponse.getPriority() == TaskPriorityType.HIGH
                    || taskResponse.getPriority() == TaskPriorityType.URGENT) {
                highPriorityCount++;
            }
        }

        return MyWorkResponse.builder()
                .assignedCount(assignedTaskResponses.size())
                .blockedCount(blockedCount)
                .overdueCount(overdueCount)
                .dueTodayCount(dueTodayCount)
                .highPriorityCount(highPriorityCount)
                .upcomingScheduledCount(upcomingSchedules.size())
                .assignedTasks(assignedTaskResponses)
                .upcomingSchedules(upcomingSchedules)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAnalyticsResponse getTaskAnalytics() {
        User currentUser = securityUtils.getAuthenticatedUser();
        long version = redisCacheService.getVersion(CacheKeys.userWorkVersion(currentUser.getUserId()));
        String key = CacheKeys.taskAnalytics(currentUser.getUserId(), version);
        return redisCacheService.getJson(key, TaskAnalyticsResponse.class)
                .orElseGet(() -> {
                    TaskAnalyticsResponse response = buildTaskAnalytics(currentUser);
                    redisCacheService.setJson(key, response, TASK_ANALYTICS_TTL);
                    return response;
                });
    }

    private TaskAnalyticsResponse buildTaskAnalytics(User currentUser) {
        List<Long> workspaceIds = workspaceMemberRepository.findByUserUserId(currentUser.getUserId())
                .stream().map(m -> m.getWorkspace().getId()).distinct().toList();

        if (workspaceIds.isEmpty()) {
            return TaskAnalyticsResponse.builder()
                    .trend(buildEmptyTrend(14))
                    .estimatedByPriority(List.of())
                    .totalAssigned(0).totalCompleted(0).build();
        }

        LocalDateTime since = LocalDateTime.now().minusDays(14);
        String userId = currentUser.getUserId();

        Map<String, Integer> createdMap = new TreeMap<>();
        Map<String, Integer> completedMap = new TreeMap<>();
        for (Object[] row : taskRepository.countCreatedByDayForUser(userId, workspaceIds, since)) {
            createdMap.put(row[0].toString(), ((Number) row[1]).intValue());
        }
        for (Object[] row : taskRepository.countCompletedByDayForUser(userId, workspaceIds, since)) {
            completedMap.put(row[0].toString(), ((Number) row[1]).intValue());
        }

        List<TaskAnalyticsResponse.DailyTrendPoint> trend = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            trend.add(TaskAnalyticsResponse.DailyTrendPoint.builder()
                    .date(date)
                    .created(createdMap.getOrDefault(date, 0))
                    .completed(completedMap.getOrDefault(date, 0))
                    .build());
        }

        List<TaskAnalyticsResponse.PriorityEstimatePoint> estimates = new ArrayList<>();
        for (Object[] row : taskRepository.sumEstimatedByPriorityForUser(userId, workspaceIds)) {
            estimates.add(TaskAnalyticsResponse.PriorityEstimatePoint.builder()
                    .priority(row[0].toString())
                    .totalMinutes(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                    .taskCount(((Number) row[2]).longValue())
                    .build());
        }

        int totalAssigned = createdMap.values().stream().mapToInt(Integer::intValue).sum();
        int totalCompleted = completedMap.values().stream().mapToInt(Integer::intValue).sum();

        return TaskAnalyticsResponse.builder()
                .trend(trend)
                .estimatedByPriority(estimates)
                .totalAssigned(totalAssigned)
                .totalCompleted(totalCompleted)
                .build();
    }

    private List<TaskAnalyticsResponse.DailyTrendPoint> buildEmptyTrend(int days) {
        List<TaskAnalyticsResponse.DailyTrendPoint> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            trend.add(TaskAnalyticsResponse.DailyTrendPoint.builder()
                    .date(LocalDate.now().minusDays(i).toString()).created(0).completed(0).build());
        }
        return trend;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());
        User currentUser = securityUtils.getAuthenticatedUser();

        Long workspaceId = task.getProject().getWorkspace().getId();
        Long projectId = task.getProject().getId();
        Long statusId = task.getStatus().getId();
        int boardPosition = task.getBoardPosition();
        String title = task.getTitle();
        Long goalId = task.getGoal() != null ? task.getGoal().getId() : null;
        String assigneeId = task.getAssignee() != null ? task.getAssignee().getUserId() : null;

        // Defensive cleanup in case DB foreign keys are not configured with CASCADE.
        taskCommentRepository.deleteByTaskId(taskId);
        taskScheduleRepository.deleteByTaskId(taskId);
        taskDependencyRepository.deleteByTaskIdOrDependsOnTaskId(taskId, taskId);

        taskRepository.delete(task);
        shiftLeftAfterRemoval(statusId, boardPosition);
        normalizeBoardPositions(statusId);

        activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.TASK_DELETED,
                ActivityTargetType.TASK, taskId,
                "Xóa task " + title);

        invalidateTaskMutationAfterCommit(projectId, assigneeId == null ? Set.of() : Set.of(assigneeId));
        cacheInvalidationService.invalidateProjectSchedulesAfterCommit(projectId);
        cacheInvalidationService.invalidateWorkspaceSchedulesAfterCommit(workspaceId);
        publishTaskEventAfterCommit(workspaceId, projectId, taskId, "task.deleted", taskId);

        if (goalId != null) {
            goalService.recalculateGoalProgress(goalId);
        }
    }

    private TaskStatus resolveFirstClosedStatus(Long projectId) {
        return taskStatusRepository.findFirstByProjectIdAndIsClosedTrueOrderByPositionAsc(projectId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Project chưa có cột hoàn tất để đánh dấu task đã xong"));
    }

    private TaskStatus resolveRestoreOpenStatus(Task task) {
        Long projectId = task.getProject().getId();

        if (task.getLastOpenStatus() != null) {
            Optional<TaskStatus> lastOpenStatus = taskStatusRepository.findByProjectIdAndIdAndIsClosedFalse(
                    projectId,
                    task.getLastOpenStatus().getId());
            if (lastOpenStatus.isPresent()) {
                return lastOpenStatus.get();
            }
        }

        return taskStatusRepository.findFirstByProjectIdAndIsClosedFalseOrderByPositionAsc(projectId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Project chưa có cột mở để khôi phục task"));
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

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse response = taskMapper.toResponse(task);
        applyDependencySummary(response, taskDependencyService.summarizeTasks(List.of(task.getId()),
                blockerNotesByTaskId(List.of(task))).get(task.getId()));
        return response;
    }

    private List<TaskResponse> toTaskResponses(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        List<TaskResponse> responses = new ArrayList<>(tasks.stream().map(taskMapper::toResponse).toList());
        Map<Long, TaskDependencyService.TaskDependencySummary> summaryByTaskId = taskDependencyService.summarizeTasks(
                tasks.stream().map(Task::getId).toList(),
                blockerNotesByTaskId(tasks));

        for (TaskResponse response : responses) {
            applyDependencySummary(response, summaryByTaskId.get(response.getId()));
        }

        return responses;
    }

    private Map<Long, String> blockerNotesByTaskId(List<Task> tasks) {
        Map<Long, String> blockerNotesByTaskId = new LinkedHashMap<>();
        for (Task task : tasks) {
            blockerNotesByTaskId.put(task.getId(), task.getBlockerNote());
        }
        return blockerNotesByTaskId;
    }

    private void applyDependencySummary(TaskResponse response,
            TaskDependencyService.TaskDependencySummary dependencySummary) {
        if (response == null || dependencySummary == null) {
            return;
        }

        response.setBlocked(dependencySummary.blocked());
        response.setBlockedReason(dependencySummary.blockedReason());
        response.setBlockedByOpenCount(dependencySummary.blockedByOpenCount());
        response.setBlockingTaskCount(dependencySummary.blockingTaskCount());
    }

    private void publishTaskEventAfterCommit(Long workspaceId, Long projectId, Long taskId, String eventType,
            Object data) {
        afterCommitExecutor.runAfterCommit(() -> realtimeEventPublisherService.publishTaskEvent(workspaceId,
                projectId, taskId, eventType, data));
    }

    private void invalidateTaskMutationAfterCommit(Long projectId, Collection<String> affectedUserIds) {
        cacheInvalidationService.invalidateProjectTasksAfterCommit(projectId);
        cacheInvalidationService.invalidateUserWorkAfterCommit(affectedUserIds);
    }

    private Set<String> assigneeIds(Task task) {
        if (task == null || task.getAssignee() == null) {
            return Set.of();
        }
        return Set.of(task.getAssignee().getUserId());
    }

    private void ensureAssigneeCanAccessProject(Project project, String assigneeId) {
        if (!projectPermissionService.resolveUserRole(project, assigneeId).atLeast(EffectiveProjectAccessRoleType.VIEWER)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Assignee must have project access");
        }
    }
}
