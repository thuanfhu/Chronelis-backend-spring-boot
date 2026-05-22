package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.ProjectAccessGrant;
import com.devloopsx.chronelis.dto.response.projectaccess.ProjectAccessResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectAccessMapper {
  UserSummaryMapper userSummaryMapper;
  WorkspaceTeamMapper workspaceTeamMapper;

  public ProjectAccessResponse toResponse(ProjectAccessGrant projectAccess) {
    if (projectAccess == null) {
      return null;
    }

    return ProjectAccessResponse.builder()
        .id(projectAccess.getId())
        .projectId(projectAccess.getProject().getId())
        .subjectType(projectAccess.getSubjectType())
        .user(
            projectAccess.getUser() != null
                ? userSummaryMapper.toSummary(projectAccess.getUser())
                : null)
        .team(
            projectAccess.getTeam() != null
                ? workspaceTeamMapper.toResponse(projectAccess.getTeam())
                : null)
        .role(projectAccess.getRole())
        .grantedBy(userSummaryMapper.toSummary(projectAccess.getGrantedBy()))
        .createdAt(projectAccess.getCreatedAt())
        .updatedAt(projectAccess.getUpdatedAt())
        .build();
  }
}
