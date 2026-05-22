package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.TaskSchedule;
import com.devloopsx.chronelis.dto.request.taskschedule.CreateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.UpdateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.response.taskschedule.TaskScheduleResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface TaskScheduleMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "task", ignore = true)
  @Mapping(target = "scheduledDate", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  TaskSchedule toEntity(CreateTaskScheduleRequest request);

  @Mapping(target = "taskId", source = "task.id")
  TaskScheduleResponse toResponse(TaskSchedule taskSchedule);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "task", ignore = true)
  @Mapping(target = "scheduledDate", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(@MappingTarget TaskSchedule taskSchedule, UpdateTaskScheduleRequest request);
}
