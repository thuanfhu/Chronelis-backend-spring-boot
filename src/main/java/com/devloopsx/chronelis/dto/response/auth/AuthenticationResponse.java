package com.devloopsx.chronelis.dto.response.auth;

import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
  String accessToken;
  UserSecureResponse userSecured;
}
