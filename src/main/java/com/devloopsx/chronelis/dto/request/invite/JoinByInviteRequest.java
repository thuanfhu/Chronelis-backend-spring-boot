package com.devloopsx.chronelis.dto.request.invite;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinByInviteRequest {
  @NotBlank(message = "INVALID_REQUEST_DATA")
  String inviteCode;
}
