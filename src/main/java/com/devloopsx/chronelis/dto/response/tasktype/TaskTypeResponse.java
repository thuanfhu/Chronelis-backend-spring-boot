package com.devloopsx.chronelis.dto.response.tasktype;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskTypeResponse {
    Long id;
    Long workspaceId;
    Long projectId;
    Long goalId;
    String name;
    String description;
    String color;
    String icon;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
