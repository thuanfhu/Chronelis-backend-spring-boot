package com.devloopsx.chronelis.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DeleteRoleFromUserRequest {
  @NotBlank(message = "ROLE_IDS_NOT_BLANK")
  List<String> roleIds;
}
