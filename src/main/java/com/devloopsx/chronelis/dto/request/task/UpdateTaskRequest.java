package com.devloopsx.chronelis.dto.request.task;

import com.devloopsx.chronelis.constant.ImportanceLevel;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.devloopsx.chronelis.constant.UrgencyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskRequest {
    @Size(max = 200, message = "INVALID_REQUEST_DATA")
    String title;

    @Size(max = 5000, message = "INVALID_REQUEST_DATA")
    String description;

    Long goalId;

    TaskPriorityType priority;

    LocalDateTime dueDate;

    @Min(value = 0, message = "INVALID_REQUEST_DATA")
    Integer estimatedMinutes;

    Long taskTypeId;

    ImportanceLevel importanceLevel;

    UrgencyLevel urgencyLevel;
}
