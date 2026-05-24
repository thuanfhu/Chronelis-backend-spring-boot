package com.devloopsx.chronelis.dto.request.team;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWorkspaceTeamRequest {
  @Size(max = 150, message = "INVALID_REQUEST_DATA")
  String name;

  @Size(max = 1000, message = "INVALID_REQUEST_DATA")
  String imageUrl;

  @Size(max = 2000, message = "INVALID_REQUEST_DATA")
  String description;
}
