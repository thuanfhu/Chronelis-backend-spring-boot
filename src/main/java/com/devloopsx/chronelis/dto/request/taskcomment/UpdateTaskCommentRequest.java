package com.devloopsx.chronelis.dto.request.taskcomment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskCommentRequest {
  @NotBlank(message = "INVALID_REQUEST_DATA")
  String content;
}
