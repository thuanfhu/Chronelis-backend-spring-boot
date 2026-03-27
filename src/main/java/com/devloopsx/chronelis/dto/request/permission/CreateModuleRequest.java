package com.devloopsx.chronelis.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateModuleRequest {
	@NotBlank(message = "PERMISSION_MODULE_NOT_BLANK")
	String moduleName;

	@NotEmpty(message = "PERMISSION_IDS_NOT_BLANK")
	List<String> permissionIds;
}
