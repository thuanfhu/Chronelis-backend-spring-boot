package com.devloopsx.chronelis.dto.request.task;

import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskDependenciesRequest {
    @Builder.Default
    List<Long> dependencyTaskIds = new ArrayList<>();

    @Size(max = 1000, message = "INVALID_REQUEST_DATA")
    String blockerNote;
}