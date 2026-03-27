package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.TaskStatus;
import com.devloopsx.chronelis.dto.request.taskstatus.CreateTaskStatusRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.UpdateTaskStatusRequest;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaskStatusMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    TaskStatus toEntity(CreateTaskStatusRequest request);

    @Mapping(target = "projectId", source = "project.id")
    TaskStatusResponse toResponse(TaskStatus taskStatus);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntity(@MappingTarget TaskStatus taskStatus, UpdateTaskStatusRequest request);
}
