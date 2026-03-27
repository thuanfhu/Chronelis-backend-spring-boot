package com.devloopsx.chronelis.dto.response.workspace;

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
public class WorkspaceMemberResponse {
    Long id;
    Long workspaceId;
    UserSummaryResponse user;
    WorkspaceMemberRoleType role;
    LocalDateTime joinedAt;
}
