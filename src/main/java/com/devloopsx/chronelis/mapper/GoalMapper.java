package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Goal;
import com.devloopsx.chronelis.dto.request.goal.CreateGoalRequest;
import com.devloopsx.chronelis.dto.request.goal.UpdateGoalRequest;
import com.devloopsx.chronelis.dto.response.goal.GoalResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface GoalMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "managerUser", ignore = true)
  @Mapping(target = "managerTeam", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  Goal toEntity(CreateGoalRequest request);

  @Mapping(target = "projectId", source = "project.id")
  @Mapping(
      target = "managerTeamId",
      expression = "java(goal.getManagerTeam() != null ? goal.getManagerTeam().getId() : null)")
  @Mapping(
      target = "managerTeamName",
      expression = "java(goal.getManagerTeam() != null ? goal.getManagerTeam().getName() : null)")
  GoalResponse toResponse(Goal goal);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "project", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "managerUser", ignore = true)
  @Mapping(target = "managerTeam", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  void updateEntity(@MappingTarget Goal goal, UpdateGoalRequest request);
}
