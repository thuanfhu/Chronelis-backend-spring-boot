package com.devloopsx.chronelis.dto.request.tasktype;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskTypeRequest {
  @Size(max = 100, message = "INVALID_REQUEST_DATA")
  String name;

  @Size(max = 2000, message = "INVALID_REQUEST_DATA")
  String description;

  Long goalId;

  Boolean clearGoal;

  @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "INVALID_REQUEST_DATA")
  String color;

  @Size(max = 50, message = "INVALID_REQUEST_DATA")
  String icon;
}
