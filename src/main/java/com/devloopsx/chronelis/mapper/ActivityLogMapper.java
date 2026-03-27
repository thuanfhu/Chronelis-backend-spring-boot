package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.ActivityLog;
import com.devloopsx.chronelis.dto.response.activitylog.ActivityLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserSummaryMapper.class)
public interface ActivityLogMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")
    ActivityLogResponse toResponse(ActivityLog activityLog);
}
