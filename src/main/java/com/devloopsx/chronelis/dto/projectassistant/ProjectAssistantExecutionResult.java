package com.devloopsx.chronelis.dto.projectassistant;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ProjectAssistantExecutionResult {
    String actionId;

    ProjectAssistantActionType actionType;

    String actionTitle;

    String outcome;

    Long projectId;

    Long goalId;

    Long taskId;

    Long scheduleId;
}