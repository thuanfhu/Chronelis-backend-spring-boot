package com.devloopsx.chronelis.dto.request.projectassistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ProjectAssistantPreviewRequest {
    @NotBlank(message = "INVALID_REQUEST_DATA")
    @Size(max = 4000, message = "INVALID_REQUEST_DATA")
    String prompt;
}