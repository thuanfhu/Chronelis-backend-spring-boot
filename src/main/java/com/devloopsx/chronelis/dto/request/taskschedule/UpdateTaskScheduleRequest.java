package com.devloopsx.chronelis.dto.request.taskschedule;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskScheduleRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  LocalDateTime scheduledStart;

  @NotNull(message = "INVALID_REQUEST_DATA")
  LocalDateTime scheduledEnd;
}
