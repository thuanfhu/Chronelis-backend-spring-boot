package com.devloopsx.chronelis.dto.response.task;

import com.devloopsx.chronelis.constant.TaskPriorityType;
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
public class TaskDependencyTaskResponse {
    Long id;
    Long projectId;
    Long goalId;
    String title;
    String statusName;
    String statusCode;
    TaskPriorityType priority;
    LocalDateTime dueDate;
    Boolean completed;
}