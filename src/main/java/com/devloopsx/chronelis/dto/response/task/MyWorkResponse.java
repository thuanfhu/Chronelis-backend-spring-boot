package com.devloopsx.chronelis.dto.response.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class MyWorkResponse {
    Integer assignedCount;
    Integer blockedCount;
    Integer overdueCount;
    Integer dueTodayCount;
    Integer highPriorityCount;
    Integer upcomingScheduledCount;

    @Builder.Default
    List<TaskResponse> assignedTasks = new ArrayList<>();

    @Builder.Default
    List<MyWorkScheduleItemResponse> upcomingSchedules = new ArrayList<>();

    LocalDateTime generatedAt;
}