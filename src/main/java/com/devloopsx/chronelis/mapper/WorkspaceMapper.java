package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.dto.request.workspace.CreateWorkspaceRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceRequest;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface WorkspaceMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "projects", ignore = true)
  @Mapping(target = "members", ignore = true)
  @Mapping(target = "activityLogs", ignore = true)
  Workspace toEntity(CreateWorkspaceRequest request);

  WorkspaceResponse toResponse(Workspace workspace);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "projects", ignore = true)
  @Mapping(target = "members", ignore = true)
  @Mapping(target = "activityLogs", ignore = true)
  void updateEntity(@MappingTarget Workspace workspace, UpdateWorkspaceRequest request);
}
