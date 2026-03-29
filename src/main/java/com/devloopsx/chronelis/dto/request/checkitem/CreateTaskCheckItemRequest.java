package com.devloopsx.chronelis.dto.request.checkitem;

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
public class CreateTaskCheckItemRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long taskId;

    @NotBlank(message = "INVALID_REQUEST_DATA")
    @Size(max = 200, message = "INVALID_REQUEST_DATA")
    String title;
}
