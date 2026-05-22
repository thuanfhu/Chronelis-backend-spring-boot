package com.devloopsx.chronelis.dto.response.workspace;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceResponse {
  Long id;
  String name;
  UserSummaryResponse owner;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
