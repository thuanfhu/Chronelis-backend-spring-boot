package com.devloopsx.chronelis.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateRoleRequest {
  @NotBlank(message = "ROLE_NAME_NOT_BLANK")
  String name;

  String description;
  Boolean active;
  List<String> permissionIds;
}
