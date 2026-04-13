package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.task.UpdateTaskDependenciesRequest;
import com.devloopsx.chronelis.dto.response.task.TaskDependencyDetailsResponse;
import java.util.Collection;
import java.util.Map;

public interface TaskDependencyService {
    TaskDependencyDetailsResponse getDependencies(Long taskId);

    TaskDependencyDetailsResponse updateDependencies(Long taskId, UpdateTaskDependenciesRequest request);

    Map<Long, TaskDependencySummary> summarizeTasks(Collection<Long> taskIds, Map<Long, String> blockerNoteByTaskId);

    record TaskDependencySummary(
            boolean blocked,
            String blockedReason,
            int blockedByOpenCount,
            int blockingTaskCount) {
    }
}