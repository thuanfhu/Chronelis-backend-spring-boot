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
public class ProjectAssistantPromptContext {
    ProjectSnapshot project;

    @Builder.Default
    List<TaskStatusSnapshot> taskStatuses = new ArrayList<>();

    @Builder.Default
    List<GoalSnapshot> goals = new ArrayList<>();

    @Builder.Default
    List<TaskSnapshot> tasks = new ArrayList<>();

    @Builder.Default
    List<ScheduleSnapshot> schedules = new ArrayList<>();

    String defaultOpenStatusCode;

    @Builder.Default
    List<String> contextWarnings = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectSnapshot {
        Long id;

        String name;

        String description;

        ProjectStatusType status;

        String managerDisplayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaskStatusSnapshot {
        Long id;

        String code;

        String name;

        Integer position;

        Boolean closed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GoalSnapshot {
        Long id;

        String title;

        GoalType goalType;

        GoalStatusType status;

        BigDecimal progressPercent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaskSnapshot {
        Long id;

        String title;

        Long goalId;

        String goalTitle;

        String statusCode;

        String statusName;

        TaskPriorityType priority;

        String assigneeDisplayName;

        LocalDateTime dueDate;

        Integer estimatedMinutes;

        Boolean completed;

        Integer boardPosition;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScheduleSnapshot {
        Long id;

        Long taskId;

        String taskTitle;

        LocalDateTime scheduledStart;

        LocalDateTime scheduledEnd;
    }
}