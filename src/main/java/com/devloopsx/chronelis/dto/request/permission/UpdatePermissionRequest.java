package com.devloopsx.chronelis.dto.request.permission;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdatePermissionRequest {
  String name;
  String apiPath;
  String httpMethod;

  @Pattern(regexp = "^(?!\\s*$).+", message = "PERMISSION_MODULE_NAME_INVALID")
  String module;
}
