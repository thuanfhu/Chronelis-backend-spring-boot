package com.devloopsx.chronelis.dto.request.checkitem;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskCheckItemRequest {
    @Size(max = 200, message = "INVALID_REQUEST_DATA")
    String title;

    Boolean isChecked;
}
