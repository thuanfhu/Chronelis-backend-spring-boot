package com.devloopsx.chronelis.dto.request.taskstatus;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReorderTaskStatusesRequest {
  @NotEmpty(message = "INVALID_REQUEST_DATA")
  List<Long> statusIdsInOrder;
}
