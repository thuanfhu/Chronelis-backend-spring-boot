package com.devloopsx.chronelis.dto.response.projectassistant;

import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantExecutionResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectAssistantApplyResponse {
    Long projectId;

    Integer requestedCount;

    Integer appliedCount;

    @Builder.Default
    List<ProjectAssistantExecutionResult> results = new ArrayList<>();

    @Builder.Default
    List<String> warnings = new ArrayList<>();

    LocalDateTime appliedAt;
}