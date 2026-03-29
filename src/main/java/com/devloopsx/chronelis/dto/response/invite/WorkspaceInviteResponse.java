package com.devloopsx.chronelis.dto.response.invite;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceInviteResponse {
    Long id;
    Long workspaceId;
    String workspaceName;
    String inviteCode;
    WorkspaceMemberRoleType roleToAssign;
    UserSummaryResponse createdBy;
    Integer maxUses;
    Integer usedCount;
    LocalDateTime expiresAt;
    Boolean isActive;
    LocalDateTime createdAt;
}
