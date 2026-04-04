package com.devloopsx.chronelis.dto.request.taskcomment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskCommentRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long taskId;

    Long parentCommentId;

    @NotBlank(message = "INVALID_REQUEST_DATA")
    String content;
}
