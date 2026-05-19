package com.devloopsx.chronelis.dto.response.pomodoro;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroSessionResponse {
    Long id;
    Long taskId;
    UserSummaryResponse user;
    Integer durationMinutes;
    LocalDateTime startedAt;
    LocalDateTime endedAt;
    LocalDateTime createdAt;
}
