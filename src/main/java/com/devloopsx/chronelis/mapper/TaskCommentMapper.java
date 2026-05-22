package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.TaskComment;
import com.devloopsx.chronelis.dto.request.taskcomment.CreateTaskCommentRequest;
import com.devloopsx.chronelis.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.devloopsx.chronelis.dto.response.taskcomment.TaskCommentResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface TaskCommentMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "task", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "parentComment", ignore = true)
  @Mapping(target = "replies", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  TaskComment toEntity(CreateTaskCommentRequest request);

  @Mapping(target = "taskId", source = "task.id")
  @Mapping(target = "parentCommentId", source = "parentComment.id")
  TaskCommentResponse toResponse(TaskComment taskComment);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "task", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "parentComment", ignore = true)
  @Mapping(target = "replies", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(@MappingTarget TaskComment taskComment, UpdateTaskCommentRequest request);
}
