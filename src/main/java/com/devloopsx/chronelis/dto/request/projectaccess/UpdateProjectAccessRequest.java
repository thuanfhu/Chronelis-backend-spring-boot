package com.devloopsx.chronelis.dto.request.projectaccess;

import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProjectAccessRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  ProjectAccessRoleType role;
}
