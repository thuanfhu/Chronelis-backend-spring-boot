package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.dto.request.task.CreateTaskRequest;
import com.devloopsx.chronelis.dto.request.task.UpdateTaskRequest;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = {UserSummaryMapper.class, TaskStatusMapper.class, TaskTypeMapper.class})
public interface TaskMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "goal", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "lastOpenStatus", ignore = true)
  @Mapping(target = "assignee", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "completedAt", ignore = true)
  @Mapping(target = "isCompleted", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "schedules", ignore = true)
  @Mapping(target = "comments", ignore = true)
  @Mapping(target = "blockerNote", ignore = true)
  @Mapping(target = "taskType", ignore = true)
  @Mapping(target = "sourceView", ignore = true)
  Task toEntity(CreateTaskRequest request);

  @Mapping(target = "projectId", source = "project.id")
  @Mapping(target = "workspaceId", source = "project.workspace.id")
  @Mapping(
      target = "goalId",
      expression = "java(task.getGoal() != null ? task.getGoal().getId() : null)")
  @Mapping(target = "blocked", ignore = true)
  @Mapping(target = "blockedReason", ignore = true)
  @Mapping(target = "blockedByOpenCount", ignore = true)
  @Mapping(target = "blockingTaskCount", ignore = true)
  TaskResponse toResponse(Task task);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "goal", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "lastOpenStatus", ignore = true)
  @Mapping(target = "assignee", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "boardPosition", ignore = true)
  @Mapping(target = "isCompleted", ignore = true)
  @Mapping(target = "completedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "schedules", ignore = true)
  @Mapping(target = "comments", ignore = true)
  @Mapping(target = "blockerNote", ignore = true)
  @Mapping(target = "taskType", ignore = true)
  @Mapping(target = "sourceView", ignore = true)
  void updateEntity(@MappingTarget Task task, UpdateTaskRequest request);
}
