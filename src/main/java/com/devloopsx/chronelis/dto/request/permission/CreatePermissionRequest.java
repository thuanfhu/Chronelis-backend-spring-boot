package com.devloopsx.chronelis.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreatePermissionRequest {
	@NotBlank(message = "PERMISSION_NAME_NOT_BLANK")
	String name;

	@NotBlank(message = "PERMISSION_API_PATH_NOT_BLANK")
	String apiPath;

	@NotBlank(message = "PERMISSION_HTTP_METHOD_NOT_BLANK")
	String httpMethod;

	@Pattern(regexp = "^(?!\\s*$).+", message = "PERMISSION_MODULE_NAME_INVALID")
	String module;
}
