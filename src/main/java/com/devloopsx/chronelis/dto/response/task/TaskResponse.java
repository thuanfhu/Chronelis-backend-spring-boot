package com.devloopsx.chronelis.dto.response.task;

import com.devloopsx.chronelis.constant.ImportanceLevel;
import com.devloopsx.chronelis.constant.SourceViewType;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.devloopsx.chronelis.constant.UrgencyLevel;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponse {
    Long id;
    Long projectId;
    Long goalId;
    TaskStatusResponse status;
    String title;
    String description;
    TaskPriorityType priority;
    TaskTypeResponse taskType;
    ImportanceLevel importanceLevel;
    UrgencyLevel urgencyLevel;
    SourceViewType sourceView;
    UserSummaryResponse assignee;
    UserSummaryResponse createdBy;
    LocalDateTime dueDate;
    Integer estimatedMinutes;
    Integer boardPosition;
    Boolean isCompleted;
    LocalDateTime completedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    int checkItemCount;
    int checkItemDoneCount;
}
