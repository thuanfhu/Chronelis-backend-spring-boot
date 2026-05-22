package com.devloopsx.chronelis.dto.response.project;

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
public class ProjectAnalyticsResponse {

  List<DailyTrendPoint> trend;
  double completionRate;
  int totalTasks;
  int completedTasks;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class DailyTrendPoint {
    String date;
    int created;
    int completed;
    int cumulative;
  }
}
