package com.devloopsx.chronelis.dto.request.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWorkspaceRequest {
  @NotBlank(message = "INVALID_REQUEST_DATA")
  @Size(max = 150, message = "INVALID_REQUEST_DATA")
  String name;
}
