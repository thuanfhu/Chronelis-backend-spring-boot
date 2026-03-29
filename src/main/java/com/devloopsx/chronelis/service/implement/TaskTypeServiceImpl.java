package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.domain.Goal;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.TaskType;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.dto.request.tasktype.CreateTaskTypeRequest;
import com.devloopsx.chronelis.dto.request.tasktype.UpdateTaskTypeRequest;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskTypeMapper;
import com.devloopsx.chronelis.repository.TaskTypeRepository;
import com.devloopsx.chronelis.service.*;
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
public class TaskTypeServiceImpl implements TaskTypeService {
    TaskTypeRepository taskTypeRepository;
    TaskTypeMapper taskTypeMapper;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;

    @Override
    @Transactional
    public TaskTypeResponse createTaskType(CreateTaskTypeRequest request) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(request.getProjectId());

        Project project = collaborationAccessService.requireProject(request.getProjectId());
        Workspace workspace = project.getWorkspace();

        if (taskTypeRepository.existsByProjectIdAndNameIgnoreCase(request.getProjectId(), request.getName())) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Task type với tên này đã tồn tại trong project");
        }

        Goal goal = null;
        if (request.getGoalId() != null) {
            goal = collaborationAccessService.requireGoal(request.getGoalId());
            if (!goal.getProject().getId().equals(project.getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Goal không thuộc project được chọn");
            }
        }

        TaskType taskType = taskTypeMapper.toEntity(request);
        LocalDateTime now = LocalDateTime.now();
        taskType.setWorkspace(workspace);
        taskType.setProject(project);
        taskType.setGoal(goal);
        taskType.setCreatedAt(now);
        taskType.setUpdatedAt(now);

        TaskType saved = taskTypeRepository.save(taskType);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(workspace.getId(), userId,
                ActivityActionType.TASK_TYPE_CREATED, ActivityTargetType.TASK_TYPE,
                saved.getId(), "Tạo task type " + saved.getName());

        TaskTypeResponse response = taskTypeMapper.toResponse(saved);
        realtimeEventPublisherService.publishProjectEvent(workspace.getId(), project.getId(),
                "taskType.created", response);
        return response;
    }

    @Override
    @Transactional
    public TaskTypeResponse updateTaskType(Long taskTypeId, UpdateTaskTypeRequest request) {
        TaskType taskType = taskTypeRepository.findById(taskTypeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task type không tồn tại"));
        collaborationAccessService.ensureCurrentUserCanAccessProject(taskType.getProject().getId());

        if (request.getName() != null && taskTypeRepository.existsByProjectIdAndNameIgnoreCaseAndIdNot(
                taskType.getProject().getId(), request.getName(), taskTypeId)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Task type với tên này đã tồn tại trong project");
        }

        if (request.getGoalId() != null) {
            Goal goal = collaborationAccessService.requireGoal(request.getGoalId());
            if (!goal.getProject().getId().equals(taskType.getProject().getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Goal không thuộc project của task type");
            }
            taskType.setGoal(goal);
        }

        taskTypeMapper.updateEntity(taskType, request);
        taskType.setUpdatedAt(LocalDateTime.now());
        TaskType updated = taskTypeRepository.save(taskType);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(taskType.getWorkspace().getId(), userId,
                ActivityActionType.TASK_TYPE_UPDATED, ActivityTargetType.TASK_TYPE,
                updated.getId(), "Cập nhật task type " + updated.getName());

        TaskTypeResponse response = taskTypeMapper.toResponse(updated);
        realtimeEventPublisherService.publishProjectEvent(taskType.getWorkspace().getId(),
                taskType.getProject().getId(), "taskType.updated", response);
        return response;
    }

    @Override
    public TaskTypeResponse getTaskType(Long taskTypeId) {
        TaskType taskType = taskTypeRepository.findById(taskTypeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task type không tồn tại"));
        collaborationAccessService.ensureCurrentUserCanAccessProject(taskType.getProject().getId());
        return taskTypeMapper.toResponse(taskType);
    }

    @Override
    public List<TaskTypeResponse> listByProject(Long projectId) {
        collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        return taskTypeRepository.findByProjectIdOrderByNameAsc(projectId).stream()
                .map(taskTypeMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteTaskType(Long taskTypeId) {
        TaskType taskType = taskTypeRepository.findById(taskTypeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task type không tồn tại"));
        collaborationAccessService.ensureCurrentUserCanAccessProject(taskType.getProject().getId());

        Long workspaceId = taskType.getWorkspace().getId();
        Long projectId = taskType.getProject().getId();
        String name = taskType.getName();

        taskTypeRepository.delete(taskType);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(workspaceId, userId,
                ActivityActionType.TASK_TYPE_DELETED, ActivityTargetType.TASK_TYPE,
                taskTypeId, "Xóa task type " + name);

        realtimeEventPublisherService.publishProjectEvent(workspaceId, projectId,
                "taskType.deleted", taskTypeId);
    }
}
