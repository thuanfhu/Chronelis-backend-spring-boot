package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.WorkspaceInvite;
import com.devloopsx.chronelis.dto.response.invite.WorkspaceInviteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface WorkspaceInviteMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "workspaceName", source = "workspace.name")
    WorkspaceInviteResponse toResponse(WorkspaceInvite invite);
}
