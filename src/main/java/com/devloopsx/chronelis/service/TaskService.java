package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.task.*;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request);

    TaskResponse getTask(Long taskId);

    PaginationResponse listTasksByProject(Long projectId, Pageable pageable);

    PaginationResponse listTasksByGoal(Long goalId, Pageable pageable);

    TaskResponse moveTask(Long taskId, MoveTaskRequest request);

    TaskResponse reorderTask(Long taskId, ReorderTaskRequest request);

    TaskResponse assignTask(Long taskId, AssignTaskRequest request);

    TaskResponse updateTaskCompletion(Long taskId, UpdateTaskCompletionRequest request);

    void deleteTask(Long taskId);
}
