package com.devloopsx.chronelis.dto.response.taskschedule;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskScheduleResponse {
  Long id;
  Long taskId;
  LocalDateTime scheduledStart;
  LocalDateTime scheduledEnd;
  LocalDate scheduledDate;
  UserSummaryResponse createdBy;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
