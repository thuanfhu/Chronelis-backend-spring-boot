package com.devloopsx.chronelis.dto.response.team;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceTeamResponse {
  Long id;
  Long workspaceId;
  String name;
  String description;
  UserSummaryResponse createdBy;
  int memberCount;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
