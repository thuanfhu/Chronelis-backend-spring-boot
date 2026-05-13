package com.devloopsx.chronelis.dto.request.projectaccess;

import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpsertProjectAccessRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    ProjectAccessSubjectType subjectType;

    String userId;

    Long teamId;

    @NotNull(message = "INVALID_REQUEST_DATA")
    ProjectAccessRoleType role;
}
