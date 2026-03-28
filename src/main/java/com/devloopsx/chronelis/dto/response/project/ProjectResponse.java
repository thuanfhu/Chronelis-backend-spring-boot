package com.devloopsx.chronelis.dto.response.project;

import com.devloopsx.chronelis.constant.ProjectStatusType;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
    Long id;
    Long workspaceId;
    String name;
    String description;
    ProjectStatusType status;
    UserSummaryResponse createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
