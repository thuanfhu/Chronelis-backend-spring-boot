package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.domain.TaskSchedule;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.taskschedule.CreateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.UpdateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.taskschedule.TaskScheduleResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskScheduleMapper;
import com.devloopsx.chronelis.repository.TaskScheduleRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.service.cache.AfterCommitExecutor;
import com.devloopsx.chronelis.service.cache.DashboardCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskScheduleServiceImpl implements TaskScheduleService {
  TaskScheduleRepository taskScheduleRepository;
  TaskScheduleMapper taskScheduleMapper;
  CollaborationAccessService collaborationAccessService;
  SecurityUtils securityUtils;
  NotificationService notificationService;
  ActivityLogService activityLogService;
  RealtimeEventPublisherService realtimeEventPublisherService;
  AfterCommitExecutor afterCommitExecutor;
  DashboardCacheService dashboardCacheService;

  @Override
  @Transactional
  public TaskScheduleResponse createSchedule(CreateTaskScheduleRequest request) {
    validateScheduleTime(request.getScheduledStart(), request.getScheduledEnd());

    Task task = collaborationAccessService.requireTask(request.getTaskId());
    collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());

    User currentUser = securityUtils.getAuthenticatedUser();
    LocalDateTime now = LocalDateTime.now();

    TaskSchedule taskSchedule = taskScheduleMapper.toEntity(request);
    taskSchedule.setTask(task);
    taskSchedule.setScheduledDate(request.getScheduledStart().toLocalDate());
    taskSchedule.setCreatedBy(currentUser);
    taskSchedule.setCreatedAt(now);
    taskSchedule.setUpdatedAt(now);

    TaskSchedule savedSchedule = taskScheduleRepository.save(taskSchedule);

    activityLogService.createLog(
        task.getProject().getWorkspace().getId(),
        currentUser.getUserId(),
        ActivityActionType.TASK_RESCHEDULED,
        ActivityTargetType.SCHEDULE,
        savedSchedule.getId(),
        "Tạo lịch cho task " + task.getTitle());

    if (task.getAssignee() != null
        && !task.getAssignee().getUserId().equals(currentUser.getUserId())) {
      notificationService.createAndPublish(
          task.getAssignee().getUserId(),
          NotificationType.TASK_RESCHEDULED,
          "Task được lên lịch",
          "Task " + task.getTitle() + " vừa được lên lịch mới",
          ReferenceType.TASK,
          task.getId());
    }

    TaskScheduleResponse response = taskScheduleMapper.toResponse(savedSchedule);
    afterScheduleMutationCommit(task, "task-schedule.created", response);
    return response;
  }

  @Override
  @Transactional
  public TaskScheduleResponse updateSchedule(Long scheduleId, UpdateTaskScheduleRequest request) {
    validateScheduleTime(request.getScheduledStart(), request.getScheduledEnd());

    TaskSchedule schedule =
        taskScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Task schedule không tồn tại"));

    Task task = schedule.getTask();
    collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());

    taskScheduleMapper.updateEntity(schedule, request);
    schedule.setScheduledDate(request.getScheduledStart().toLocalDate());
    schedule.setUpdatedAt(LocalDateTime.now());

    TaskSchedule updatedSchedule = taskScheduleRepository.save(schedule);
    User currentUser = securityUtils.getAuthenticatedUser();

    activityLogService.createLog(
        task.getProject().getWorkspace().getId(),
        currentUser.getUserId(),
        ActivityActionType.TASK_RESCHEDULED,
        ActivityTargetType.SCHEDULE,
        updatedSchedule.getId(),
        "Cập nhật lịch cho task " + task.getTitle());

    if (task.getAssignee() != null
        && !task.getAssignee().getUserId().equals(currentUser.getUserId())) {
      notificationService.createAndPublish(
          task.getAssignee().getUserId(),
          NotificationType.TASK_RESCHEDULED,
          "Task đổi lịch",
          "Task " + task.getTitle() + " vừa được cập nhật lịch",
          ReferenceType.TASK,
          task.getId());
    }

    TaskScheduleResponse response = taskScheduleMapper.toResponse(updatedSchedule);
    afterScheduleMutationCommit(task, "task-schedule.updated", response);
    return response;
  }

  @Override
  @Transactional
  public void deleteSchedule(Long scheduleId) {
    TaskSchedule schedule =
        taskScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Task schedule không tồn tại"));

    Task task = schedule.getTask();
    collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());

    taskScheduleRepository.delete(schedule);
    User currentUser = securityUtils.getAuthenticatedUser();

    activityLogService.createLog(
        task.getProject().getWorkspace().getId(),
        currentUser.getUserId(),
        ActivityActionType.TASK_RESCHEDULED,
        ActivityTargetType.SCHEDULE,
        scheduleId,
        "Xóa lịch của task " + task.getTitle());

    afterScheduleMutationCommit(task, "task-schedule.deleted", scheduleId);
  }

  @Override
  public List<TaskScheduleResponse> listSchedulesByTask(Long taskId) {
    Task task = collaborationAccessService.requireTask(taskId);
    collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

    return taskScheduleRepository.findByTaskIdOrderByScheduledStartAsc(taskId).stream()
        .map(taskScheduleMapper::toResponse)
        .toList();
  }

  @Override
  public PaginationResponse getProjectCalendar(
      Long projectId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
    collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
    validateCalendarRange(fromDate, toDate);
    return sanitizeCalendarResponse(
        buildProjectCalendar(projectId, fromDate, toDate, pageable), "project-calendar");
  }

  @Override
  public PaginationResponse getWorkspaceCalendar(
      Long workspaceId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
    collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
    validateCalendarRange(fromDate, toDate);

    String currentUserId = securityUtils.getAuthenticatedUser().getUserId();
    return sanitizeCalendarResponse(
        buildWorkspaceCalendar(workspaceId, currentUserId, fromDate, toDate, pageable),
        "workspace-calendar");
  }

  private PaginationResponse buildProjectCalendar(
      Long projectId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
    Page<TaskSchedule> page =
        taskScheduleRepository.findByTaskProjectIdAndScheduledDateBetween(
            projectId, fromDate, toDate, pageable);

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
        .content(page.getContent().stream().map(taskScheduleMapper::toResponse).toList())
        .build();
  }

  private PaginationResponse buildWorkspaceCalendar(
      Long workspaceId,
      String currentUserId,
      LocalDate fromDate,
      LocalDate toDate,
      Pageable pageable) {
    Page<TaskSchedule> page =
        taskScheduleRepository.findVisibleByWorkspaceCalendar(
            workspaceId, currentUserId, fromDate, toDate, pageable);

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
        .content(page.getContent().stream().map(taskScheduleMapper::toResponse).toList())
        .build();
  }

  private void afterScheduleMutationCommit(Task task, String eventType, Object data) {
    Long workspaceId = task.getProject().getWorkspace().getId();
    Long projectId = task.getProject().getId();
    Long taskId = task.getId();

    afterCommitExecutor.runAfterCommit(
        () -> {
          if (task.getAssignee() != null) {
            dashboardCacheService.evictUserTaskCaches(task.getAssignee().getUserId());
          }
          realtimeEventPublisherService.publishProjectEvent(workspaceId, projectId, eventType, data);
          realtimeEventPublisherService.publishTaskEvent(
              workspaceId, projectId, taskId, eventType, data);
        });
  }

  private PaginationResponse sanitizeCalendarResponse(PaginationResponse response, String context) {
    if (response == null || !(response.getContent() instanceof List<?> content) || content.isEmpty()) {
      return response;
    }

    Map<Object, Object> dedupedById = new LinkedHashMap<>();
    int duplicates = 0;
    int anonymousIndex = 0;

    for (Object item : content) {
      Object scheduleId = extractScheduleId(item);
      Object dedupeKey = scheduleId != null ? scheduleId : "anonymous:" + anonymousIndex++;
      if (dedupedById.containsKey(dedupeKey)) {
        duplicates++;
      }
      dedupedById.put(dedupeKey, item);
    }

    if (duplicates == 0) {
      return response;
    }

    log.warn(
        "Detected {} duplicate schedule entries in calendar response for key {}",
        duplicates,
        context);
    return PaginationResponse.builder()
        .meta(response.getMeta())
        .content(List.copyOf(dedupedById.values()))
        .build();
  }

  private Object extractScheduleId(Object item) {
    if (item instanceof TaskScheduleResponse response) {
      return response.getId();
    }

    if (item instanceof Map<?, ?> map) {
      return map.get("id");
    }

    return null;
  }

  private void validateScheduleTime(LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {
    if (!scheduledEnd.isAfter(scheduledStart)) {
      throw new ApplicationException(
          ErrorCode.INVALID_REQUEST_DATA, "scheduled_end phải lớn hơn scheduled_start");
    }
  }

  private void validateCalendarRange(LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
      throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA, "Khoảng ngày không hợp lệ");
    }
  }
}
