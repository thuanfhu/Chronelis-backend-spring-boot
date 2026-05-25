package com.devloopsx.chronelis.dto.response.task;

import com.devloopsx.chronelis.constant.SourceViewType;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponse {
  Long id;
  Long workspaceId;
  Long projectId;
  Long goalId;
  TaskStatusResponse status;
  String title;
  String description;
  String notesHtml;
  TaskPriorityType priority;
  TaskTypeResponse taskType;
  SourceViewType sourceView;
  UserSummaryResponse assignee;
  UserSummaryResponse createdBy;
  LocalDateTime dueDate;
  LocalDateTime scheduledStart;
  LocalDateTime scheduledEnd;
  Integer estimatedMinutes;
  Integer boardPosition;
  String blockerNote;
  Boolean blocked;
  String blockedReason;
  Integer blockedByOpenCount;
  Integer blockingTaskCount;
  Boolean isCompleted;
  LocalDateTime completedAt;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
