package com.devloopsx.chronelis.dto.request.projectassistant;

import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ProjectAssistantApplyRequest {
    @Valid
    @NotNull(message = "INVALID_REQUEST_DATA")
    ProjectAssistantPlan plan;

    @NotEmpty(message = "INVALID_REQUEST_DATA")
    List<@NotBlank(message = "INVALID_REQUEST_DATA") String> actionIds;
}