package com.devloopsx.chronelis.dto.request.taskstatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskStatusRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  Long projectId;

  @NotBlank(message = "INVALID_REQUEST_DATA")
  @Size(max = 100, message = "INVALID_REQUEST_DATA")
  String name;

  @NotBlank(message = "INVALID_REQUEST_DATA")
  @Size(max = 50, message = "INVALID_REQUEST_DATA")
  String code;

  Integer position;

  Boolean isClosed;
}
