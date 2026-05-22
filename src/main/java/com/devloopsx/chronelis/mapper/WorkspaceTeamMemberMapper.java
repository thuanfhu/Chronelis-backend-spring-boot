package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.WorkspaceTeamMember;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface WorkspaceTeamMemberMapper {
  @Mapping(target = "teamId", source = "team.id")
  WorkspaceTeamMemberResponse toResponse(WorkspaceTeamMember member);
}
