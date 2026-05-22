package com.devloopsx.chronelis.dto.request.role;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateRoleRequest {
  String name;
  String description;
  Boolean active;
  List<String> permissionIds;
}
