package com.devloopsx.chronelis.dto.response.workspace;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceMemberResponse {
  Long id;
  Long workspaceId;
  UserSummaryResponse user;
  WorkspaceMemberRoleType role;
  LocalDateTime joinedAt;
}
