package com.devloopsx.chronelis.dto.projectassistant;

import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import com.devloopsx.chronelis.constant.ProjectStatusType;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
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
public class ProjectAssistantPlannedAction {
    String actionId;

    Integer order;

    ProjectAssistantActionType actionType;

    String actionTitle;

    String rationale;

    Boolean executable;

    @Builder.Default
    List<String> validationErrors = new ArrayList<>();

    Long goalId;

    Long taskId;

    Long scheduleId;

    String name;

    String title;

    String description;

    ProjectStatusType projectStatus;

    GoalType goalType;

    GoalStatusType goalStatus;

    BigDecimal progressPercent;

    TaskPriorityType priority;

    String statusCode;

    Integer targetPosition;

    Boolean clearGoal;

    LocalDateTime dueDate;

    Integer estimatedMinutes;

    Boolean completed;

    LocalDateTime scheduledStart;

    LocalDateTime scheduledEnd;
}