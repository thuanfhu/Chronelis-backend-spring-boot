package com.devloopsx.chronelis.dto.request.project;

import com.devloopsx.chronelis.constant.ProjectStatusType;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProjectRequest {
    @Size(max = 150, message = "INVALID_REQUEST_DATA")
    String name;

    ProjectStatusType status;
}
