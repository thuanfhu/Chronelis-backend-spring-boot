package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.taskschedule.CreateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.UpdateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.taskschedule.TaskScheduleResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskScheduleService {
  TaskScheduleResponse createSchedule(CreateTaskScheduleRequest request);

  TaskScheduleResponse updateSchedule(Long scheduleId, UpdateTaskScheduleRequest request);

  void deleteSchedule(Long scheduleId);

  List<TaskScheduleResponse> listSchedulesByTask(Long taskId);

  PaginationResponse getProjectCalendar(
      Long projectId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

  PaginationResponse getWorkspaceCalendar(
      Long workspaceId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
