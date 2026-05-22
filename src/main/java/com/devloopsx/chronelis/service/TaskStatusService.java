package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.taskstatus.CreateTaskStatusRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.ReorderTaskStatusesRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.UpdateTaskStatusRequest;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import java.util.List;

public interface TaskStatusService {
  TaskStatusResponse createStatus(CreateTaskStatusRequest request);

  List<TaskStatusResponse> listStatusesByProject(Long projectId);

  TaskStatusResponse updateStatus(Long statusId, UpdateTaskStatusRequest request);

  List<TaskStatusResponse> reorderStatuses(Long projectId, ReorderTaskStatusesRequest request);

  void deleteStatus(Long statusId);
}
