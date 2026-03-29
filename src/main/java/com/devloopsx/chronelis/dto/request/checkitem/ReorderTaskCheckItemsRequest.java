package com.devloopsx.chronelis.dto.request.checkitem;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReorderTaskCheckItemsRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long taskId;

    @NotEmpty(message = "INVALID_REQUEST_DATA")
    List<Long> itemIdsInOrder;
}
