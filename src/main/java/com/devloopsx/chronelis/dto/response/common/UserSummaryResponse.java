package com.devloopsx.chronelis.dto.response.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSummaryResponse {
  String userId;
  String email;
  String firstName;
  String lastName;
}
