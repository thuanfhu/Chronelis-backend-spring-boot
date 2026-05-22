package com.devloopsx.chronelis.dto.response.permission;

import com.devloopsx.chronelis.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PermissionResponse extends AuditResponse {
  String permissionId;
  String name;
  String apiPath;
  String httpMethod;
  String module;
}
