package com.devloopsx.chronelis.dto.response.task;

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
public class TaskAnalyticsResponse {

    List<DailyTrendPoint> trend;
    List<PriorityEstimatePoint> estimatedByPriority;
    int totalAssigned;
    int totalCompleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DailyTrendPoint {
        String date;
        int created;
        int completed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PriorityEstimatePoint {
        String priority;
        long totalMinutes;
        long taskCount;
    }
}
