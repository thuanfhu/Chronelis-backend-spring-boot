package com.devloopsx.chronelis.dto.response.project;

import com.devloopsx.chronelis.constant.ProjectStatusType;
import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
  Long id;
  Long workspaceId;
  String name;
  String imageUrl;
  String description;
  ProjectStatusType status;
  ProjectVisibilityType visibility;
  UserSummaryResponse createdBy;
  UserSummaryResponse managerUser;
  Long managerTeamId;
  String managerTeamName;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
