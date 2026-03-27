package com.devloopsx.chronelis.dto.request.auth;

import com.devloopsx.chronelis.validation.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@PasswordMatches(passwordField = "newPassword", confirmPasswordField = "confirmPassword")
public class ResetPasswordRequest {
	@NotBlank(message = "TOKEN_NOT_BLANK")
	String token;

	@NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID_FORMAT")
	String newPassword;

	@NotBlank(message = "CONFIRM_PASSWORD_NOT_BLANK")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "CONFIRM_PASSWORD_INVALID_FORMAT")
	String confirmPassword;
}
