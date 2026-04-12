package com.devloopsx.chronelis.dto.response.projectassistant;

import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantActionType;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ProjectAssistantStatusResponse {
    Boolean enabled;

    Boolean configured;

    Boolean ready;

    String provider;

    String model;

    Integer maxPreviewActions;

    @Builder.Default
    List<ProjectAssistantActionType> supportedActions = new ArrayList<>();

    String message;
}