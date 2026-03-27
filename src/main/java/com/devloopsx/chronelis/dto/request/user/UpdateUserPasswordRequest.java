package com.devloopsx.chronelis.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateUserPasswordRequest {
	@NotBlank(message = "CURRENT_PASSWORD_NOT_BLANK")
	String currentPassword;

	@NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
	String newPassword;

	@NotBlank(message = "CONFIRM_PASSWORD_NOT_BLANK")
	String confirmPassword;
}
