package com.devloopsx.chronelis.dto.request.pomodoro;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SavePomodoroSessionRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  @Min(value = 1, message = "INVALID_REQUEST_DATA")
  @Max(value = 240, message = "INVALID_REQUEST_DATA")
  Integer durationMinutes;

  LocalDateTime startedAt;

  LocalDateTime endedAt;
}
