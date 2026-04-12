package com.devloopsx.chronelis.dto.response.projectassistant;

import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlan;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectAssistantPreviewResponse {
    Long projectId;

    String provider;

    String model;

    ProjectAssistantPlan plan;

    LocalDateTime generatedAt;
}