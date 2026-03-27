package com.devloopsx.chronelis.dto.response.activitylog;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityLogResponse {
    Long id;
    Long workspaceId;
    UserSummaryResponse actor;
    ActivityActionType actionType;
    ActivityTargetType targetType;
    Long targetId;
    String description;
    LocalDateTime createdAt;
}
