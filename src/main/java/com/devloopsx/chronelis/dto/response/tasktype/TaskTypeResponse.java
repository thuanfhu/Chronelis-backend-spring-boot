package com.devloopsx.chronelis.dto.response.tasktype;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
