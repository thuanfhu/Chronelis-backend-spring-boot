package com.devloopsx.chronelis.dto.request.project;

import com.devloopsx.chronelis.constant.ProjectStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProjectStatusRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  ProjectStatusType status;
}
