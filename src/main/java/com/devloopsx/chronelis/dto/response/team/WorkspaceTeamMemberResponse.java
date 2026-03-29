package com.devloopsx.chronelis.dto.response.team;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceTeamMemberResponse {
    Long id;
    Long teamId;
    UserSummaryResponse user;
    LocalDateTime joinedAt;
}
