package com.devloopsx.chronelis.dto.response.task;

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
public class TaskDependencyDetailsResponse {
    Long taskId;
    String blockerNote;
    Boolean blocked;
    String blockedReason;
    Integer blockedByOpenCount;
    Integer blockingTaskCount;

    @Builder.Default
    List<TaskDependencyTaskResponse> blockedByTasks = new ArrayList<>();

    @Builder.Default
    List<TaskDependencyTaskResponse> blockingTasks = new ArrayList<>();
}