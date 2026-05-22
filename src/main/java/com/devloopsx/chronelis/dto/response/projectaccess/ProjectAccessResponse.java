package com.devloopsx.chronelis.dto.response.projectaccess;

import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectAccessResponse {
  Long id;
  Long projectId;
  ProjectAccessSubjectType subjectType;
  UserSummaryResponse user;
  WorkspaceTeamResponse team;
  ProjectAccessRoleType role;
  UserSummaryResponse grantedBy;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
