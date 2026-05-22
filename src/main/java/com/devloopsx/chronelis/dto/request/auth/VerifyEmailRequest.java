package com.devloopsx.chronelis.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VerifyEmailRequest {
  @NotBlank(message = "TOKEN_NOT_BLANK")
  String token;
}
