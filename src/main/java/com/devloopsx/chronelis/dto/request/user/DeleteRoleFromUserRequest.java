package com.devloopsx.chronelis.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DeleteRoleFromUserRequest {
	@NotBlank(message = "ROLE_IDS_NOT_BLANK")
	List<String> roleIds;
}
