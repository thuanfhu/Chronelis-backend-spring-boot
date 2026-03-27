package com.devloopsx.chronelis.dto.request.auth;

import com.devloopsx.chronelis.validation.annotation.LoginIdentifierValid;
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
@LoginIdentifierValid
public class LoginRequest {
	@Email(message = "EMAIL_INVALID")
	@Pattern(regexp = "^[\\w._%+-]+@(gmail\\.com|yopmail\\.com)$", message = "EMAIL_PROVIDER_INVALID")
	String email;

	@Pattern(regexp = "^(\\+84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$", message = "PHONE_NUMBER_VN_INVALID")
	String phoneNumber;

	@NotBlank(message = "PASSWORD_NOT_BLANK")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID_FORMAT")
	String password;
}
