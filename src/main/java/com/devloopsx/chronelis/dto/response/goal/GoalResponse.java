package com.devloopsx.chronelis.dto.response.goal;

import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoalResponse {
    Long id;
    Long projectId;
    String title;
    GoalType goalType;
    GoalStatusType status;
    BigDecimal progressPercent;
    UserSummaryResponse createdBy;
    UserSummaryResponse managerUser;
    Long managerTeamId;
    String managerTeamName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
