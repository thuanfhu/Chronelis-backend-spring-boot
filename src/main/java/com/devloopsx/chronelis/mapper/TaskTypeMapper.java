package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.TaskType;
import com.devloopsx.chronelis.dto.request.tasktype.CreateTaskTypeRequest;
import com.devloopsx.chronelis.dto.request.tasktype.UpdateTaskTypeRequest;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaskTypeMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "workspace", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "goal", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  TaskType toEntity(CreateTaskTypeRequest request);

  @Mapping(target = "workspaceId", source = "workspace.id")
  @Mapping(target = "projectId", source = "project.id")
  @Mapping(
      target = "goalId",
      expression = "java(taskType.getGoal() != null ? taskType.getGoal().getId() : null)")
  TaskTypeResponse toResponse(TaskType taskType);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "workspace", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "goal", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(@MappingTarget TaskType taskType, UpdateTaskTypeRequest request);
}
