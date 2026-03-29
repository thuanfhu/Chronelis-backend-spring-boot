package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.WorkspaceTeam;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface WorkspaceTeamMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "memberCount", expression = "java(team.getMembers() != null ? team.getMembers().size() : 0)")
    WorkspaceTeamResponse toResponse(WorkspaceTeam team);
}
