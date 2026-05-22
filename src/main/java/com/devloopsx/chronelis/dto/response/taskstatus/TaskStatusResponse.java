package com.devloopsx.chronelis.dto.response.taskstatus;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStatusResponse {
  Long id;
  Long projectId;
  String name;
  String code;
  Integer position;
  Boolean isClosed;
  LocalDateTime createdAt;
}
