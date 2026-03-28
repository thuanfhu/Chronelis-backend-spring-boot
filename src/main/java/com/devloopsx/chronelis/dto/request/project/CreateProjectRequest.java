package com.devloopsx.chronelis.dto.request.project;

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
public class CreateProjectRequest {
    @NotNull(message = "INVALID_REQUEST_DATA")
    Long workspaceId;

    @NotBlank(message = "INVALID_REQUEST_DATA")
    @Size(max = 150, message = "INVALID_REQUEST_DATA")
    String name;

    @Size(max = 2000, message = "INVALID_REQUEST_DATA")
    String description;
}
