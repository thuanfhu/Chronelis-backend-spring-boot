package com.devloopsx.chronelis.dto.request.workspace;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWorkspaceMemberRoleRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    WorkspaceMemberRoleType role;
}
