package com.devloopsx.chronelis.dto.response.checkitem;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCheckItemResponse {
    Long id;
    Long taskId;
    String title;
    Boolean isChecked;
    Integer position;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
