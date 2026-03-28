package com.devloopsx.chronelis.dto.request.task;

import com.devloopsx.chronelis.constant.TaskPriorityType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long projectId;

    Long goalId;

    @NotNull(message = "INVALID_REQUEST_DATA")
    Long statusId;

    @NotBlank(message = "INVALID_REQUEST_DATA")
    @Size(max = 200, message = "INVALID_REQUEST_DATA")
    String title;

    @Size(max = 5000, message = "INVALID_REQUEST_DATA")
    String description;

    @NotNull(message = "INVALID_REQUEST_DATA")
    TaskPriorityType priority;

    String assigneeId;

    LocalDateTime dueDate;

    @Min(value = 0, message = "INVALID_REQUEST_DATA")
    Integer estimatedMinutes;

    Integer boardPosition;
}
