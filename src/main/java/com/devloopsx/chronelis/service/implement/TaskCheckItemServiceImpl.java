package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.domain.TaskCheckItem;
import com.devloopsx.chronelis.dto.request.checkitem.CreateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.request.checkitem.ReorderTaskCheckItemsRequest;
import com.devloopsx.chronelis.dto.request.checkitem.UpdateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.response.checkitem.TaskCheckItemResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskCheckItemMapper;
import com.devloopsx.chronelis.repository.TaskCheckItemRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskCheckItemServiceImpl implements TaskCheckItemService {
    TaskCheckItemRepository taskCheckItemRepository;
    TaskCheckItemMapper taskCheckItemMapper;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;

    @Override
    @Transactional
    public TaskCheckItemResponse createCheckItem(CreateTaskCheckItemRequest request) {
        Task task = collaborationAccessService.requireTask(request.getTaskId());
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        Integer maxPos = taskCheckItemRepository.findMaxPositionByTaskId(request.getTaskId());
        int nextPos = (maxPos == null ? -1 : maxPos) + 1;

        TaskCheckItem item = taskCheckItemMapper.toEntity(request);
        LocalDateTime now = LocalDateTime.now();
        item.setTask(task);
        item.setIsChecked(false);
        item.setPosition(nextPos);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        TaskCheckItem saved = taskCheckItemRepository.save(item);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(task.getProject().getWorkspace().getId(), userId,
                ActivityActionType.CHECK_ITEM_CREATED, ActivityTargetType.CHECK_ITEM,
                saved.getId(), "Tạo check item " + saved.getTitle());

        TaskCheckItemResponse response = taskCheckItemMapper.toResponse(saved);
        realtimeEventPublisherService.publishTaskEvent(
                task.getProject().getWorkspace().getId(), task.getProject().getId(),
                task.getId(), "checkItem.created", response);
        return response;
    }

    @Override
    @Transactional
    public TaskCheckItemResponse updateCheckItem(Long checkItemId, UpdateTaskCheckItemRequest request) {
        TaskCheckItem item = taskCheckItemRepository.findById(checkItemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Check item không tồn tại"));
        Task task = item.getTask();
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        taskCheckItemMapper.updateEntity(item, request);
        item.setUpdatedAt(LocalDateTime.now());
        TaskCheckItem updated = taskCheckItemRepository.save(item);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(task.getProject().getWorkspace().getId(), userId,
                ActivityActionType.CHECK_ITEM_UPDATED, ActivityTargetType.CHECK_ITEM,
                updated.getId(), "Cập nhật check item " + updated.getTitle());

        TaskCheckItemResponse response = taskCheckItemMapper.toResponse(updated);
        realtimeEventPublisherService.publishTaskEvent(
                task.getProject().getWorkspace().getId(), task.getProject().getId(),
                task.getId(), "checkItem.updated", response);
        return response;
    }

    @Override
    @Transactional
    public TaskCheckItemResponse toggleCheckItem(Long checkItemId) {
        TaskCheckItem item = taskCheckItemRepository.findById(checkItemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Check item không tồn tại"));
        Task task = item.getTask();
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        item.setIsChecked(!Boolean.TRUE.equals(item.getIsChecked()));
        item.setUpdatedAt(LocalDateTime.now());
        TaskCheckItem updated = taskCheckItemRepository.save(item);

        TaskCheckItemResponse response = taskCheckItemMapper.toResponse(updated);
        realtimeEventPublisherService.publishTaskEvent(
                task.getProject().getWorkspace().getId(), task.getProject().getId(),
                task.getId(), "checkItem.toggled", response);
        return response;
    }

    @Override
    @Transactional
    public void deleteCheckItem(Long checkItemId) {
        TaskCheckItem item = taskCheckItemRepository.findById(checkItemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Check item không tồn tại"));
        Task task = item.getTask();
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        String title = item.getTitle();
        taskCheckItemRepository.delete(item);

        String userId = securityUtils.getAuthenticatedUser().getUserId();
        activityLogService.createLog(task.getProject().getWorkspace().getId(), userId,
                ActivityActionType.CHECK_ITEM_DELETED, ActivityTargetType.CHECK_ITEM,
                checkItemId, "Xóa check item " + title);

        realtimeEventPublisherService.publishTaskEvent(
                task.getProject().getWorkspace().getId(), task.getProject().getId(),
                task.getId(), "checkItem.deleted", checkItemId);
    }

    @Override
    public List<TaskCheckItemResponse> listByTask(Long taskId) {
        Task task = collaborationAccessService.requireTask(taskId);
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        return taskCheckItemRepository.findByTaskIdOrderByPositionAsc(taskId).stream()
                .map(taskCheckItemMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void reorderCheckItems(ReorderTaskCheckItemsRequest request) {
        Task task = collaborationAccessService.requireTask(request.getTaskId());
        collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

        List<TaskCheckItem> items = taskCheckItemRepository.findByTaskIdOrderByPositionAsc(request.getTaskId());
        Map<Long, TaskCheckItem> itemMap = items.stream()
                .collect(Collectors.toMap(TaskCheckItem::getId, Function.identity()));

        List<Long> orderedIds = request.getItemIdsInOrder();
        for (int i = 0; i < orderedIds.size(); i++) {
            TaskCheckItem item = itemMap.get(orderedIds.get(i));
            if (item != null) {
                item.setPosition(i);
                item.setUpdatedAt(LocalDateTime.now());
            }
        }

        taskCheckItemRepository.saveAll(items);

        realtimeEventPublisherService.publishTaskEvent(
                task.getProject().getWorkspace().getId(), task.getProject().getId(),
                task.getId(), "checkItem.reordered", request.getTaskId());
    }
}
