package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.WorkspaceMember;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface WorkspaceMemberMapper {
  @Mapping(target = "workspaceId", source = "workspace.id")
  WorkspaceMemberResponse toResponse(WorkspaceMember workspaceMember);
}
