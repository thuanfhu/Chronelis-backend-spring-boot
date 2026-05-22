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
public class DeletePermissionFromRoleRequest {
  @NotBlank(message = "PERMISSION_IDS_NOT_BLANK")
  List<String> permissionIds;
}
