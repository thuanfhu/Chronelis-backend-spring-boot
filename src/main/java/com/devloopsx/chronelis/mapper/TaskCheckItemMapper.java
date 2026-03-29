package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.TaskCheckItem;
import com.devloopsx.chronelis.dto.request.checkitem.CreateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.request.checkitem.UpdateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.response.checkitem.TaskCheckItemResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaskCheckItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "isChecked", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskCheckItem toEntity(CreateTaskCheckItemRequest request);

    @Mapping(target = "taskId", source = "task.id")
    TaskCheckItemResponse toResponse(TaskCheckItem item);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget TaskCheckItem item, UpdateTaskCheckItemRequest request);
}
