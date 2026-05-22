package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.dto.request.project.CreateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface ProjectMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "workspace", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "managerUser", ignore = true)
  @Mapping(target = "managerTeam", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "goals", ignore = true)
  @Mapping(target = "taskStatuses", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  Project toEntity(CreateProjectRequest request);

  @Mapping(target = "workspaceId", source = "workspace.id")
  @Mapping(
      target = "managerTeamId",
      expression =
          "java(project.getManagerTeam() != null ? project.getManagerTeam().getId() : null)")
  @Mapping(
      target = "managerTeamName",
      expression =
          "java(project.getManagerTeam() != null ? project.getManagerTeam().getName() : null)")
  ProjectResponse toResponse(Project project);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "workspace", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "managerUser", ignore = true)
  @Mapping(target = "managerTeam", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "goals", ignore = true)
  @Mapping(target = "taskStatuses", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  void updateEntity(@MappingTarget Project project, UpdateProjectRequest request);
}
