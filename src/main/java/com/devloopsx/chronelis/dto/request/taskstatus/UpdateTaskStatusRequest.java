package com.devloopsx.chronelis.dto.request.taskstatus;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskStatusRequest {
    @Size(max = 100, message = "INVALID_REQUEST_DATA")
    String name;

    @Size(max = 50, message = "INVALID_REQUEST_DATA")
    String code;

    Integer position;

    Boolean isClosed;
}
