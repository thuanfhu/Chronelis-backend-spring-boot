package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.TaskStatus;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.taskstatus.CreateTaskStatusRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.ReorderTaskStatusesRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.UpdateTaskStatusRequest;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskStatusMapper;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskStatusServiceImpl implements TaskStatusService {
    TaskStatusRepository taskStatusRepository;
    TaskRepository taskRepository;
    TaskStatusMapper taskStatusMapper;
    CollaborationAccessService collaborationAccessService;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;
    SecurityUtils securityUtils;

    @Override
    @Transactional
    public TaskStatusResponse createStatus(CreateTaskStatusRequest request) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(request.getProjectId());
        Project project = collaborationAccessService.requireProject(request.getProjectId());

        String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (taskStatusRepository.existsByProjectIdAndCodeIgnoreCase(project.getId(), normalizedCode)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Code status đã tồn tại trong project");
        }

        List<TaskStatus> statuses = taskStatusRepository.findByProjectIdOrderByPositionAsc(project.getId());
        int insertPosition = resolveInsertPosition(request.getPosition(), statuses.size());
        shiftRightFromPosition(statuses, insertPosition);

        TaskStatus taskStatus = taskStatusMapper.toEntity(request);
        taskStatus.setProject(project);
        taskStatus.setCode(normalizedCode);
        taskStatus.setPosition(insertPosition);
        taskStatus.setIsClosed(Boolean.TRUE.equals(request.getIsClosed()));
        taskStatus.setCreatedAt(LocalDateTime.now());

        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);
        User currentUser = securityUtils.getAuthenticatedUser();

        activityLogService.createLog(project.getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED, ActivityTargetType.STATUS, savedStatus.getId(),
                "Tạo cột Kanban " + savedStatus.getName());

        TaskStatusResponse response = taskStatusMapper.toResponse(savedStatus);
        realtimeEventPublisherService.publishProjectEvent(project.getWorkspace().getId(), project.getId(),
                "task-status.created", response);
        return response;
    }

    @Override
    public List<TaskStatusResponse> listStatusesByProject(Long projectId) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        return taskStatusRepository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(taskStatusMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskStatusResponse updateStatus(Long statusId, UpdateTaskStatusRequest request) {
        TaskStatus status = collaborationAccessService.requireTaskStatus(statusId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(status.getProject().getId());

        boolean hasUpdate = request.getName() != null || request.getCode() != null || request.getPosition() != null
                || request.getIsClosed() != null;
        if (!hasUpdate) {
            throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
        }

        if (request.getCode() != null) {
            String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);
            if (!normalizedCode.equalsIgnoreCase(status.getCode())
                    && taskStatusRepository.existsByProjectIdAndCodeIgnoreCase(status.getProject().getId(),
                            normalizedCode)) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Code status đã tồn tại trong project");
            }
            status.setCode(normalizedCode);
        }

        if (request.getName() != null) {
            status.setName(request.getName());
        }

        if (request.getIsClosed() != null) {
            status.setIsClosed(request.getIsClosed());
        }

        if (request.getPosition() != null) {
            moveStatusPosition(status, request.getPosition());
        }

        TaskStatus updatedStatus = taskStatusRepository.save(status);
        User currentUser = securityUtils.getAuthenticatedUser();

        activityLogService.createLog(status.getProject().getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED, ActivityTargetType.STATUS, updatedStatus.getId(),
                "Cập nhật cột Kanban " + updatedStatus.getName());

        TaskStatusResponse response = taskStatusMapper.toResponse(updatedStatus);
        realtimeEventPublisherService.publishProjectEvent(status.getProject().getWorkspace().getId(),
                status.getProject().getId(), "task-status.updated", response);
        return response;
    }

    @Override
    @Transactional
    public List<TaskStatusResponse> reorderStatuses(Long projectId, ReorderTaskStatusesRequest request) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);

        List<TaskStatus> statuses = taskStatusRepository.findByProjectIdOrderByPositionAsc(projectId);
        List<Long> providedIds = request.getStatusIdsInOrder();

        if (statuses.size() != providedIds.size()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Danh sách status không đầy đủ để reorder");
        }

        Set<Long> existingIds = statuses.stream().map(TaskStatus::getId).collect(HashSet::new, Set::add, Set::addAll);
        if (!existingIds.containsAll(providedIds)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Danh sách status chứa ID không hợp lệ");
        }

        Map<Long, TaskStatus> byId = new HashMap<>();
        statuses.forEach(status -> byId.put(status.getId(), status));

        int position = 1;
        for (Long statusId : providedIds) {
            TaskStatus status = byId.get(statusId);
            status.setPosition(position++);
        }

        taskStatusRepository.saveAll(statuses);

        Project project = statuses.getFirst().getProject();
        User currentUser = securityUtils.getAuthenticatedUser();
        activityLogService.createLog(project.getWorkspace().getId(), currentUser.getUserId(),
                ActivityActionType.TASK_UPDATED,
                ActivityTargetType.STATUS, projectId,
                "Reorder các cột Kanban");

        List<TaskStatusResponse> response = taskStatusRepository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(taskStatusMapper::toResponse)
                .toList();

        realtimeEventPublisherService.publishProjectEvent(project.getWorkspace().getId(), project.getId(),
                "task-status.reordered", response);

        return response;
    }

    @Override
    @Transactional
    public void deleteStatus(Long statusId) {
        TaskStatus status = collaborationAccessService.requireTaskStatus(statusId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(status.getProject().getId());

        long taskCount = taskRepository.countByStatusId(statusId);
        if (taskCount > 0) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Không thể xóa cột Kanban vì vẫn còn task");
        }

        Project project = status.getProject();
        Long workspaceId = project.getWorkspace().getId();
        Integer deletedPosition = status.getPosition();

        taskStatusRepository.delete(status);

        List<TaskStatus> remainingStatuses = taskStatusRepository.findByProjectIdOrderByPositionAsc(project.getId());
        for (TaskStatus remainingStatus : remainingStatuses) {
            if (remainingStatus.getPosition() > deletedPosition) {
                remainingStatus.setPosition(remainingStatus.getPosition() - 1);
            }
        }
        taskStatusRepository.saveAll(remainingStatuses);

        User currentUser = securityUtils.getAuthenticatedUser();
        activityLogService.createLog(workspaceId, currentUser.getUserId(), ActivityActionType.TASK_UPDATED,
                ActivityTargetType.STATUS, statusId,
                "Xóa cột Kanban " + status.getName());

        realtimeEventPublisherService.publishProjectEvent(workspaceId, project.getId(), "task-status.deleted",
                statusId);
    }

    private int resolveInsertPosition(Integer requestedPosition, int currentSize) {
        if (requestedPosition == null) {
            return currentSize + 1;
        }
        if (requestedPosition <= 1) {
            return 1;
        }
        return Math.min(requestedPosition, currentSize + 1);
    }

    private void shiftRightFromPosition(List<TaskStatus> statuses, int fromPosition) {
        for (TaskStatus status : statuses) {
            if (status.getPosition() >= fromPosition) {
                status.setPosition(status.getPosition() + 1);
            }
        }
        taskStatusRepository.saveAll(statuses);
    }

    private void moveStatusPosition(TaskStatus status, Integer targetPositionInput) {
        List<TaskStatus> statuses = taskStatusRepository.findByProjectIdOrderByPositionAsc(status.getProject().getId());
        int currentPosition = status.getPosition();
        int targetPosition = Math.max(1, Math.min(targetPositionInput, statuses.size()));

        if (targetPosition == currentPosition) {
            return;
        }

        for (TaskStatus item : statuses) {
            if (item.getId().equals(status.getId())) {
                continue;
            }

            if (targetPosition < currentPosition
                    && item.getPosition() >= targetPosition
                    && item.getPosition() < currentPosition) {
                item.setPosition(item.getPosition() + 1);
            }

            if (targetPosition > currentPosition
                    && item.getPosition() > currentPosition
                    && item.getPosition() <= targetPosition) {
                item.setPosition(item.getPosition() - 1);
            }
        }

        status.setPosition(targetPosition);
        taskStatusRepository.saveAll(statuses);
    }
}
