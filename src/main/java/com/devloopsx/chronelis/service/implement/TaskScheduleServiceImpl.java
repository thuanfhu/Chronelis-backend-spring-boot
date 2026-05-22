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
import com.devloopsx.chronelis.service.cache.CacheInvalidationService;
import com.devloopsx.chronelis.service.cache.CacheKeys;
import com.devloopsx.chronelis.service.cache.RedisCacheService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
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
        RedisCacheService redisCacheService;
        CacheInvalidationService cacheInvalidationService;
        AfterCommitExecutor afterCommitExecutor;

        static final Duration CALENDAR_TTL = Duration.ofSeconds(60);

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

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.TASK_RESCHEDULED, ActivityTargetType.SCHEDULE, savedSchedule.getId(),
                                "Tạo lịch cho task " + task.getTitle());

                if (task.getAssignee() != null && !task.getAssignee().getUserId().equals(currentUser.getUserId())) {
                        notificationService.createAndPublish(task.getAssignee().getUserId(),
                                        NotificationType.TASK_RESCHEDULED,
                                        "Task được lên lịch", "Task " + task.getTitle() + " vừa được lên lịch mới",
                                        ReferenceType.TASK,
                                        task.getId());
                }

                TaskScheduleResponse response = taskScheduleMapper.toResponse(savedSchedule);
                invalidateScheduleMutationAfterCommit(task);
                publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-schedule.created", response);
                return response;
        }

        @Override
        @Transactional
        public TaskScheduleResponse updateSchedule(Long scheduleId, UpdateTaskScheduleRequest request) {
                validateScheduleTime(request.getScheduledStart(), request.getScheduledEnd());

                TaskSchedule schedule = taskScheduleRepository.findById(scheduleId)
                                .orElseThrow(
                                                () -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                                "Task schedule không tồn tại"));

                Task task = schedule.getTask();
                collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());

                taskScheduleMapper.updateEntity(schedule, request);
                schedule.setScheduledDate(request.getScheduledStart().toLocalDate());
                schedule.setUpdatedAt(LocalDateTime.now());

                TaskSchedule updatedSchedule = taskScheduleRepository.save(schedule);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.TASK_RESCHEDULED, ActivityTargetType.SCHEDULE,
                                updatedSchedule.getId(),
                                "Cập nhật lịch cho task " + task.getTitle());

                if (task.getAssignee() != null && !task.getAssignee().getUserId().equals(currentUser.getUserId())) {
                        notificationService.createAndPublish(task.getAssignee().getUserId(),
                                        NotificationType.TASK_RESCHEDULED,
                                        "Task đổi lịch", "Task " + task.getTitle() + " vừa được cập nhật lịch",
                                        ReferenceType.TASK,
                                        task.getId());
                }

                TaskScheduleResponse response = taskScheduleMapper.toResponse(updatedSchedule);
                invalidateScheduleMutationAfterCommit(task);
                publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-schedule.updated", response);
                return response;
        }

        @Override
        @Transactional
        public void deleteSchedule(Long scheduleId) {
                TaskSchedule schedule = taskScheduleRepository.findById(scheduleId)
                                .orElseThrow(
                                                () -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                                "Task schedule không tồn tại"));

                Task task = schedule.getTask();
                collaborationAccessService.ensureCurrentUserCanManageProjectWork(task.getProject().getId());

                taskScheduleRepository.delete(schedule);
                User currentUser = securityUtils.getAuthenticatedUser();

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.TASK_RESCHEDULED, ActivityTargetType.SCHEDULE, scheduleId,
                                "Xóa lịch của task " + task.getTitle());

                invalidateScheduleMutationAfterCommit(task);
                publishTaskEventAfterCommit(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-schedule.deleted", scheduleId);
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
        public PaginationResponse getProjectCalendar(Long projectId, LocalDate fromDate, LocalDate toDate,
                        Pageable pageable) {
                collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
                validateCalendarRange(fromDate, toDate);
                long scheduleVersion = redisCacheService.getVersion(CacheKeys.projectSchedulesVersion(projectId));
                String key = CacheKeys.projectCalendar(projectId, fromDate.toString(), toDate.toString(),
                                pageable.getPageNumber(), pageable.getPageSize(), scheduleVersion);
                return redisCacheService.getJson(key, PaginationResponse.class)
                                .orElseGet(() -> {
                                        PaginationResponse response = buildProjectCalendar(projectId, fromDate, toDate,
                                                        pageable);
                                        redisCacheService.setJson(key, response, CALENDAR_TTL);
                                        return response;
                                });
        }

        @Override
        public PaginationResponse getWorkspaceCalendar(Long workspaceId, LocalDate fromDate, LocalDate toDate,
                        Pageable pageable) {
                collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);
                validateCalendarRange(fromDate, toDate);

                String currentUserId = securityUtils.getAuthenticatedUser().getUserId();
                long scheduleVersion = redisCacheService.getVersion(CacheKeys.workspaceSchedulesVersion(workspaceId));
                long accessVersion = redisCacheService.getVersion(CacheKeys.workspaceAccessVersion(workspaceId));
                String key = CacheKeys.workspaceCalendar(workspaceId, currentUserId, fromDate.toString(),
                                toDate.toString(), pageable.getPageNumber(), pageable.getPageSize(), scheduleVersion,
                                accessVersion);
                return redisCacheService.getJson(key, PaginationResponse.class)
                                .orElseGet(() -> {
                                        PaginationResponse response = buildWorkspaceCalendar(workspaceId, currentUserId,
                                                        fromDate, toDate, pageable);
                                        redisCacheService.setJson(key, response, CALENDAR_TTL);
                                        return response;
                                });
        }

        private PaginationResponse buildProjectCalendar(Long projectId, LocalDate fromDate, LocalDate toDate,
                        Pageable pageable) {
                Page<TaskSchedule> page = taskScheduleRepository.findByTaskProjectIdAndScheduledDateBetween(projectId,
                                fromDate, toDate, pageable);

                return PaginationResponse.builder()
                                .meta(PaginationMeta.builder()
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

        private PaginationResponse buildWorkspaceCalendar(Long workspaceId, String currentUserId, LocalDate fromDate,
                        LocalDate toDate, Pageable pageable) {
                Page<TaskSchedule> page = taskScheduleRepository.findVisibleByWorkspaceCalendar(
                                workspaceId,
                                currentUserId,
                                fromDate, toDate, pageable);

                return PaginationResponse.builder()
                                .meta(PaginationMeta.builder()
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

        private void invalidateScheduleMutationAfterCommit(Task task) {
                cacheInvalidationService.invalidateProjectSchedulesAfterCommit(task.getProject().getId());
                cacheInvalidationService.invalidateWorkspaceSchedulesAfterCommit(task.getProject().getWorkspace().getId());
                if (task.getAssignee() != null) {
                        cacheInvalidationService.invalidateUserWorkAfterCommit(task.getAssignee().getUserId());
                }
        }

        private void publishTaskEventAfterCommit(Long workspaceId, Long projectId, Long taskId, String eventType,
                        Object data) {
                afterCommitExecutor.runAfterCommit(() -> realtimeEventPublisherService.publishTaskEvent(workspaceId,
                                projectId, taskId, eventType, data));
        }

        private void validateScheduleTime(LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {
                if (!scheduledEnd.isAfter(scheduledStart)) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "scheduled_end phải lớn hơn scheduled_start");
                }
        }

        private void validateCalendarRange(LocalDate fromDate, LocalDate toDate) {
                if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
                        throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                        "Khoảng ngày không hợp lệ");
                }
        }
}
