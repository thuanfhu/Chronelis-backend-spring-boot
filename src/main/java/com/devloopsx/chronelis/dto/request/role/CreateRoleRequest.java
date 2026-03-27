package com.devloopsx.chronelis.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
