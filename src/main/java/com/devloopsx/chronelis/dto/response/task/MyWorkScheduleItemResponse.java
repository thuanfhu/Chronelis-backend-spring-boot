package com.devloopsx.chronelis.dto.response.task;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyWorkScheduleItemResponse {
    Long scheduleId;
    Long taskId;
    LocalDateTime scheduledStart;
    LocalDateTime scheduledEnd;
    TaskResponse task;
}