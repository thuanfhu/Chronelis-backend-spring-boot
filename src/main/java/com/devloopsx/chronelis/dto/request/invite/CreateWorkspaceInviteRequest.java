package com.devloopsx.chronelis.dto.request.invite;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWorkspaceInviteRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  Long workspaceId;

  WorkspaceMemberRoleType roleToAssign;

  Integer maxUses;

  LocalDateTime expiresAt;
}
