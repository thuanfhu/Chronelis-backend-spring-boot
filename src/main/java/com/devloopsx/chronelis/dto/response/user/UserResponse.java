package com.devloopsx.chronelis.dto.response.user;

import com.devloopsx.chronelis.dto.response.common.AuditResponse;
import com.devloopsx.chronelis.dto.response.role.RoleSecureResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends AuditResponse {
  String userId;
  String email;
  String firstName;
  String lastName;
  String nickname;
  String phoneNumber;
  String biography;
  String avatarUrl;
  String city;
  String nationality;
  Boolean isVerified;
  List<RoleSecureResponse> roles;
}
