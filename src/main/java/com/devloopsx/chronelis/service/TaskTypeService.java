package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.tasktype.CreateTaskTypeRequest;
import com.devloopsx.chronelis.dto.request.tasktype.UpdateTaskTypeRequest;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;

import java.util.List;

public interface TaskTypeService {
    TaskTypeResponse createTaskType(CreateTaskTypeRequest request);

    TaskTypeResponse updateTaskType(Long taskTypeId, UpdateTaskTypeRequest request);

    TaskTypeResponse getTaskType(Long taskTypeId);

    List<TaskTypeResponse> listByProject(Long projectId);

    void deleteTaskType(Long taskTypeId);
}
