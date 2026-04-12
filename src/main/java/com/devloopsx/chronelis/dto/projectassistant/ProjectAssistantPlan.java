package com.devloopsx.chronelis.dto.projectassistant;

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
public class ProjectAssistantPlan {
    String summary;

    @Builder.Default
    List<String> warnings = new ArrayList<>();

    @Builder.Default
    List<ProjectAssistantPlannedAction> actions = new ArrayList<>();
}