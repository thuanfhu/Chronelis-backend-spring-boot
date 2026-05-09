package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.domain.TaskDependency;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.task.UpdateTaskDependenciesRequest;
import com.devloopsx.chronelis.dto.response.task.TaskDependencyDetailsResponse;
import com.devloopsx.chronelis.dto.response.task.TaskDependencyTaskResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.TaskDependencyRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.service.ActivityLogService;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.RealtimeEventPublisherService;
import com.devloopsx.chronelis.service.TaskDependencyService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskDependencyServiceImpl implements TaskDependencyService {
    TaskDependencyRepository taskDependencyRepository;
    TaskRepository taskRepository;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;
    ActivityLogService activityLogService;
    RealtimeEventPublisherService realtimeEventPublisherService;

    @Override
    @Transactional(readOnly = true)
    public TaskDependencyDetailsResponse getDependencies(Long taskId) {
        Task task = this.collaborationAccessService.requireTask(taskId);
        this.collaborationAccessService.ensureCurrentUserCanAccessTask(taskId);

        List<TaskDependency> incomingDependencies = this.taskDependencyRepository.findIncomingByTaskId(taskId);
        List<TaskDependency> outgoingDependencies = this.taskDependencyRepository.findOutgoingByTaskId(taskId);
        TaskDependencySummary summary = summarizeTasks(List.of(taskId), blockerNotesOf(task)).get(taskId);

        return TaskDependencyDetailsResponse.builder()
                .taskId(taskId)
                .blockerNote(trimToNull(task.getBlockerNote()))
                .blocked(summary != null && summary.blocked())
                .blockedReason(summary != null ? summary.blockedReason() : null)
                .blockedByOpenCount(summary != null ? summary.blockedByOpenCount() : 0)
                .blockingTaskCount(summary != null ? summary.blockingTaskCount() : 0)
                .blockedByTasks(incomingDependencies.stream()
                        .map(TaskDependency::getDependsOnTask)
                        .sorted(taskComparator())
                        .map(this::toTaskLinkResponse)
                        .toList())
                .blockingTasks(outgoingDependencies.stream()
                        .map(TaskDependency::getTask)
                        .sorted(taskComparator())
                        .map(this::toTaskLinkResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public TaskDependencyDetailsResponse updateDependencies(Long taskId, UpdateTaskDependenciesRequest request) {
        Task task = this.collaborationAccessService.requireTask(taskId);
        this.collaborationAccessService.ensureCurrentUserCanManageTask(taskId);

        Set<Long> nextDependencyIds = normalizeDependencyIds(taskId, request.getDependencyTaskIds());
        Map<Long, Task> dependencyTasksById = validateDependencyTasks(task, nextDependencyIds);
        ensureNoDependencyCycle(task, nextDependencyIds);

        List<TaskDependency> existingDependencies = this.taskDependencyRepository.findIncomingByTaskId(taskId);
        Set<Long> existingDependencyIds = existingDependencies.stream()
                .map(dependency -> dependency.getDependsOnTask().getId())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        Set<Long> dependencyIdsToRemove = new LinkedHashSet<>(existingDependencyIds);
        dependencyIdsToRemove.removeAll(nextDependencyIds);

        Set<Long> dependencyIdsToAdd = new LinkedHashSet<>(nextDependencyIds);
        dependencyIdsToAdd.removeAll(existingDependencyIds);

        if (!dependencyIdsToRemove.isEmpty()) {
            List<TaskDependency> dependenciesToRemove = existingDependencies.stream()
                    .filter(dependency -> dependencyIdsToRemove.contains(dependency.getDependsOnTask().getId()))
                    .toList();
            this.taskDependencyRepository.deleteAll(dependenciesToRemove);
        }

        if (!dependencyIdsToAdd.isEmpty()) {
            User currentUser = this.securityUtils.getAuthenticatedUser();
            LocalDateTime now = LocalDateTime.now();
            List<TaskDependency> dependenciesToAdd = dependencyIdsToAdd.stream()
                    .map(dependencyTaskId -> TaskDependency.builder()
                            .task(task)
                            .dependsOnTask(dependencyTasksById.get(dependencyTaskId))
                            .createdBy(currentUser)
                            .createdAt(now)
                            .updatedAt(now)
                            .build())
                    .toList();
            this.taskDependencyRepository.saveAll(dependenciesToAdd);
        }

        String nextBlockerNote = trimToNull(request.getBlockerNote());
        boolean blockerNoteChanged = !Objects.equals(trimToNull(task.getBlockerNote()), nextBlockerNote);
        if (blockerNoteChanged) {
            task.setBlockerNote(nextBlockerNote);
            task.setUpdatedAt(LocalDateTime.now());
            this.taskRepository.save(task);
        }

        if (!dependencyIdsToAdd.isEmpty() || !dependencyIdsToRemove.isEmpty() || blockerNoteChanged) {
            User currentUser = this.securityUtils.getAuthenticatedUser();
            this.activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                    ActivityActionType.TASK_DEPENDENCY_UPDATED, ActivityTargetType.TASK, taskId,
                    "Cập nhật phụ thuộc cho task " + task.getTitle());
        }

        Set<Long> impactedTaskIds = new LinkedHashSet<>();
        impactedTaskIds.add(taskId);
        impactedTaskIds.addAll(existingDependencyIds);
        impactedTaskIds.addAll(nextDependencyIds);

        Map<String, Object> eventPayload = new LinkedHashMap<>();
        eventPayload.put("taskId", taskId);
        eventPayload.put("impactedTaskIds", new ArrayList<>(impactedTaskIds));

        for (Long impactedTaskId : impactedTaskIds) {
            this.realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                    task.getProject().getId(), impactedTaskId, "task.dependencies-updated", eventPayload);
        }

        this.realtimeEventPublisherService.publishProjectEvent(task.getProject().getWorkspace().getId(),
                task.getProject().getId(), "task.dependencies-updated", eventPayload);

        return getDependencies(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, TaskDependencySummary> summarizeTasks(Collection<Long> taskIds,
            Map<Long, String> blockerNoteByTaskId) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }

        List<Long> normalizedTaskIds = taskIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (normalizedTaskIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> blockedByOpenCount = new HashMap<>();
        for (TaskDependency dependency : this.taskDependencyRepository.findIncomingByTaskIds(normalizedTaskIds)) {
            if (!Boolean.TRUE.equals(dependency.getDependsOnTask().getIsCompleted())) {
                blockedByOpenCount.merge(dependency.getTask().getId(), 1, Integer::sum);
            }
        }

        Map<Long, Integer> blockingTaskCount = new HashMap<>();
        for (TaskDependency dependency : this.taskDependencyRepository.findOutgoingByTaskIds(normalizedTaskIds)) {
            blockingTaskCount.merge(dependency.getDependsOnTask().getId(), 1, Integer::sum);
        }

        Map<Long, TaskDependencySummary> summaryByTaskId = new LinkedHashMap<>();
        for (Long taskId : normalizedTaskIds) {
            int openDependencyCount = blockedByOpenCount.getOrDefault(taskId, 0);
            int downstreamDependencyCount = blockingTaskCount.getOrDefault(taskId, 0);
            String blockerNote = trimToNull(blockerNoteByTaskId.get(taskId));
            boolean blocked = openDependencyCount > 0 || blockerNote != null;

            String blockedReason = null;
            if (openDependencyCount > 0) {
                blockedReason = openDependencyCount == 1
                        ? "Bị chặn bởi 1 task chưa hoàn thành"
                        : "Bị chặn bởi " + openDependencyCount + " task chưa hoàn thành";
            } else if (blockerNote != null) {
                blockedReason = blockerNote;
            }

            summaryByTaskId.put(taskId, new TaskDependencySummary(blocked, blockedReason, openDependencyCount,
                    downstreamDependencyCount));
        }

        return summaryByTaskId;
    }

    private Map<Long, String> blockerNotesOf(Task task) {
        Map<Long, String> blockerNotes = new HashMap<>();
        blockerNotes.put(task.getId(), task.getBlockerNote());
        return blockerNotes;
    }

    private Set<Long> normalizeDependencyIds(Long taskId, List<Long> rawDependencyIds) {
        Set<Long> dependencyIds = new LinkedHashSet<>();

        if (rawDependencyIds == null) {
            return dependencyIds;
        }

        for (Long dependencyTaskId : rawDependencyIds) {
            if (dependencyTaskId == null) {
                continue;
            }

            if (dependencyTaskId.equals(taskId)) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Task không thể tự phụ thuộc vào chính nó.");
            }

            dependencyIds.add(dependencyTaskId);
        }

        return dependencyIds;
    }

    private Map<Long, Task> validateDependencyTasks(Task task, Set<Long> dependencyIds) {
        Map<Long, Task> taskById = new LinkedHashMap<>();

        if (dependencyIds.isEmpty()) {
            return taskById;
        }

        for (Task dependencyTask : this.taskRepository.findAllById(dependencyIds)) {
            taskById.put(dependencyTask.getId(), dependencyTask);
        }

        if (taskById.size() != dependencyIds.size()) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Một hoặc nhiều task phụ thuộc không tồn tại.");
        }

        for (Task dependencyTask : taskById.values()) {
            if (!Objects.equals(dependencyTask.getProject().getId(), task.getProject().getId())) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Task phụ thuộc phải thuộc cùng project.");
            }
        }

        return taskById;
    }

    private void ensureNoDependencyCycle(Task task, Set<Long> nextDependencyIds) {
        if (nextDependencyIds.isEmpty()) {
            return;
        }

        Map<Long, Set<Long>> dependencyGraph = new LinkedHashMap<>();
        for (TaskDependency dependency : this.taskDependencyRepository.findByProjectId(task.getProject().getId())) {
            if (Objects.equals(dependency.getTask().getId(), task.getId())) {
                continue;
            }

            dependencyGraph.computeIfAbsent(dependency.getTask().getId(), ignored -> new LinkedHashSet<>())
                    .add(dependency.getDependsOnTask().getId());
        }

        dependencyGraph.put(task.getId(), new LinkedHashSet<>(nextDependencyIds));

        for (Long dependencyTaskId : nextDependencyIds) {
            if (canReachTask(dependencyTaskId, task.getId(), dependencyGraph)) {
                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                        "Phụ thuộc tạo thành vòng lặp giữa các task.");
            }
        }
    }

    private boolean canReachTask(Long startTaskId, Long targetTaskId, Map<Long, Set<Long>> dependencyGraph) {
        ArrayDeque<Long> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.add(startTaskId);

        while (!queue.isEmpty()) {
            Long currentTaskId = queue.removeFirst();
            if (!visited.add(currentTaskId)) {
                continue;
            }

            if (Objects.equals(currentTaskId, targetTaskId)) {
                return true;
            }

            for (Long nextTaskId : dependencyGraph.getOrDefault(currentTaskId, Set.of())) {
                queue.addLast(nextTaskId);
            }
        }

        return false;
    }

    private TaskDependencyTaskResponse toTaskLinkResponse(Task task) {
        return TaskDependencyTaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .goalId(task.getGoal() != null ? task.getGoal().getId() : null)
                .title(task.getTitle())
                .statusName(task.getStatus() != null ? task.getStatus().getName() : null)
                .statusCode(task.getStatus() != null ? task.getStatus().getCode() : null)
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .completed(task.getIsCompleted())
                .build();
    }

    private Comparator<Task> taskComparator() {
        return Comparator
                .comparing((Task task) -> Boolean.TRUE.equals(task.getIsCompleted()))
                .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}