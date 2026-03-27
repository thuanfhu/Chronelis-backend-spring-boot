package com.devloopsx.chronelis.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ForgotPasswordRequest {
	@NotBlank(message = "EMAIL_NOT_BLANK")
	@Email(message = "EMAIL_INVALID")
	@Pattern(regexp = "^[\\w._%+-]+@(gmail\\.com|yopmail\\.com)$", message = "EMAIL_PROVIDER_INVALID")
	String email;
}
