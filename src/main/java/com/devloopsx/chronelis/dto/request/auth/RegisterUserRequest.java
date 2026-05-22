package com.devloopsx.chronelis.dto.request.auth;

import com.devloopsx.chronelis.validation.annotation.PasswordMatches;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@PasswordMatches(passwordField = "password", confirmPasswordField = "confirmPassword")
public class RegisterUserRequest {
  @NotBlank(message = "EMAIL_NOT_BLANK")
  @Email(message = "EMAIL_INVALID")
  @Pattern(regexp = "^[\\w._%+-]+@(gmail\\.com|yopmail\\.com)$", message = "EMAIL_PROVIDER_INVALID")
  String email;

  @NotBlank(message = "PHONE_NUMBER_NOT_BLANK")
  @Pattern(
      regexp = "^(\\+84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$",
      message = "PHONE_NUMBER_VN_INVALID")
  String phoneNumber;

  @NotBlank(message = "PASSWORD_NOT_BLANK")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "PASSWORD_INVALID_FORMAT")
  String password;

  @NotBlank(message = "CONFIRM_PASSWORD_NOT_BLANK")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "CONFIRM_PASSWORD_INVALID_FORMAT")
  String confirmPassword;

  @NotBlank(message = "FIRSTNAME_NOT_BLANK")
  @Size(min = 2, max = 50, message = "FIRSTNAME_INVALID_LENGTH")
  String firstName;

  @NotBlank(message = "LASTNAME_NOT_BLANK")
  @Size(min = 2, max = 50, message = "LASTNAME_INVALID_LENGTH")
  String lastName;
}
