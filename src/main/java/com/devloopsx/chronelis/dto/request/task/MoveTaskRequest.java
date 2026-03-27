package com.devloopsx.chronelis.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoveTaskRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long statusId;

    Integer targetPosition;
}
