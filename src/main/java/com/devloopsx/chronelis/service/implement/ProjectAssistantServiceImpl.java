package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.configuration.properties.ProjectAssistantProperties;
import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import com.devloopsx.chronelis.constant.ProjectStatusType;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantActionType;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantExecutionResult;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlan;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlannedAction;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPromptContext;
import com.devloopsx.chronelis.dto.request.goal.CreateGoalRequest;
import com.devloopsx.chronelis.dto.request.goal.UpdateGoalRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantApplyRequest;
import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantPreviewRequest;
import com.devloopsx.chronelis.dto.request.task.CreateTaskRequest;
import com.devloopsx.chronelis.dto.request.task.MoveTaskRequest;
import com.devloopsx.chronelis.dto.request.task.UpdateTaskCompletionRequest;
import com.devloopsx.chronelis.dto.request.task.UpdateTaskRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.CreateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.UpdateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import com.devloopsx.chronelis.dto.response.goal.GoalResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantApplyResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantPreviewResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantStatusResponse;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import com.devloopsx.chronelis.dto.response.taskschedule.TaskScheduleResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.TaskStatusRepository;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.GoalService;
import com.devloopsx.chronelis.service.ProjectAssistantService;
import com.devloopsx.chronelis.service.ProjectService;
import com.devloopsx.chronelis.service.TaskScheduleService;
import com.devloopsx.chronelis.service.TaskService;
import com.devloopsx.chronelis.service.projectassistant.ProjectAssistantAiGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectAssistantServiceImpl implements ProjectAssistantService {
    static final List<ProjectAssistantActionType> SUPPORTED_ACTIONS = List.of(
            ProjectAssistantActionType.UPDATE_PROJECT,
            ProjectAssistantActionType.CREATE_GOAL,
            ProjectAssistantActionType.UPDATE_GOAL,
            ProjectAssistantActionType.CREATE_TASK,
            ProjectAssistantActionType.UPDATE_TASK,
            ProjectAssistantActionType.MOVE_TASK,
            ProjectAssistantActionType.UPDATE_TASK_COMPLETION,
            ProjectAssistantActionType.CREATE_TASK_SCHEDULE,
            ProjectAssistantActionType.UPDATE_TASK_SCHEDULE);

    ObjectProvider<ProjectAssistantAiGateway> aiGatewayProvider;
    ProjectAssistantProperties properties;
    CollaborationAccessService collaborationAccessService;
    ProjectService projectService;
    GoalService goalService;
    TaskService taskService;
    TaskScheduleService taskScheduleService;
    TaskStatusRepository taskStatusRepository;
    ObjectMapper objectMapper;

    @Override
    public ProjectAssistantStatusResponse getStatus() {
        boolean configured = this.properties.hasReadyGoogleConfiguration();
        boolean ready = this.aiGatewayProvider.getIfAvailable() != null;

        String message;
        if (!this.properties.isEnabled()) {
            message = "Project assistant đang tắt.";
        } else if (!configured) {
            message = "Project assistant đã bật nhưng còn thiếu cấu hình Gemini.";
        } else if (ready) {
            message = "Project assistant đã sẵn sàng.";
        } else {
            message = "Project assistant chưa sẵn sàng.";
        }

        return ProjectAssistantStatusResponse.builder()
                .enabled(this.properties.isEnabled())
                .configured(configured)
                .ready(ready)
                .provider("gemini")
                .model(this.properties.getGoogle().getModel())
                .maxPreviewActions(this.properties.getMaxPreviewActions())
                .supportedActions(SUPPORTED_ACTIONS)
                .message(message)
                .build();
    }

    @Override
    public ProjectAssistantPreviewResponse previewProject(Long projectId, ProjectAssistantPreviewRequest request) {
        this.collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        ProjectAssistantAiGateway gateway = requireAiGateway();

        ProjectAssistantPromptContext context = buildPromptContext(projectId);
        ProjectAssistantPlan rawPlan = gateway.generatePlan(buildSystemPrompt(),
                buildUserPrompt(projectId, request, context));
        ProjectAssistantPlan normalizedPlan = normalizePlan(rawPlan, context);

        return ProjectAssistantPreviewResponse.builder()
                .projectId(projectId)
                .provider("gemini")
                .model(this.properties.getGoogle().getModel())
                .plan(normalizedPlan)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public ProjectAssistantApplyResponse applyPlan(Long projectId, ProjectAssistantApplyRequest request) {
        this.collaborationAccessService.ensureCurrentUserCanAccessProject(projectId);
        ensureAssistantFeatureEnabled();

        ProjectAssistantPromptContext context = buildPromptContext(projectId);
        ProjectAssistantPlan normalizedPlan = normalizePlan(request.getPlan(), context);

        Set<String> selectedActionIds = new LinkedHashSet<>(request.getActionIds());
        if (selectedActionIds.size() != request.getActionIds().size()) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_PLAN_INVALID,
                    "Danh sách action được chọn chứa giá trị trùng lặp.");
        }

        List<ProjectAssistantPlannedAction> selectedActions = normalizedPlan.getActions().stream()
                .filter(action -> selectedActionIds.contains(action.getActionId()))
                .toList();

        if (selectedActions.size() != selectedActionIds.size()) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_PLAN_INVALID,
                    "Có action được chọn không tồn tại trong kế hoạch đã gửi.");
        }

        List<String> invalidMessages = selectedActions.stream()
                .filter(action -> !Boolean.TRUE.equals(action.getExecutable()))
                .map(action -> action.getActionId() + ": " + String.join("; ", action.getValidationErrors()))
                .toList();

        if (!invalidMessages.isEmpty()) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_PLAN_INVALID,
                    "Kế hoạch chứa action không hợp lệ: " + String.join(" | ", invalidMessages));
        }

        List<ProjectAssistantExecutionResult> results = new ArrayList<>();
        List<String> warnings = new ArrayList<>(normalizedPlan.getWarnings());

        for (ProjectAssistantPlannedAction action : selectedActions) {
            results.add(executeAction(projectId, action));
        }

        return ProjectAssistantApplyResponse.builder()
                .projectId(projectId)
                .requestedCount(selectedActions.size())
                .appliedCount(results.size())
                .results(results)
                .warnings(warnings)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    private ProjectAssistantAiGateway requireAiGateway() {
        ProjectAssistantAiGateway gateway = this.aiGatewayProvider.getIfAvailable();
        if (!this.properties.isEnabled() || gateway == null) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_UNAVAILABLE,
                    "Project assistant chưa được bật hoặc Gemini chưa sẵn sàng.");
        }
        return gateway;
    }

    private void ensureAssistantFeatureEnabled() {
        if (!this.properties.isEnabled()) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_UNAVAILABLE,
                    "Project assistant chưa được bật.");
        }
    }

    private ProjectAssistantPromptContext buildPromptContext(Long projectId) {
        ProjectResponse project = this.projectService.getProject(projectId);

        List<ProjectAssistantPromptContext.TaskStatusSnapshot> taskStatuses = this.taskStatusRepository
                .findByProjectIdOrderByPositionAsc(projectId)
                .stream()
                .map(status -> ProjectAssistantPromptContext.TaskStatusSnapshot.builder()
                        .id(status.getId())
                        .code(status.getCode())
                        .name(status.getName())
                        .position(status.getPosition())
                        .closed(status.getIsClosed())
                        .build())
                .toList();

        PaginationResponse goalsPage = this.goalService.listGoalsByProject(projectId,
                PageRequest.of(0, this.properties.getContextGoalLimit()));
        List<GoalResponse> goalResponses = convertPageContent(goalsPage, new TypeReference<List<GoalResponse>>() {
        });

        PaginationResponse tasksPage = this.taskService.listTasksByProject(projectId,
                PageRequest.of(0, this.properties.getContextTaskLimit()));
        List<TaskResponse> taskResponses = convertPageContent(tasksPage, new TypeReference<List<TaskResponse>>() {
        });

        PaginationResponse schedulesPage = this.taskScheduleService.getProjectCalendar(projectId,
                LocalDate.now().minusMonths(3), LocalDate.now().plusYears(1),
                PageRequest.of(0, this.properties.getContextScheduleLimit()));
        List<TaskScheduleResponse> scheduleResponses = convertPageContent(schedulesPage,
                new TypeReference<List<TaskScheduleResponse>>() {
                });

        Map<Long, TaskResponse> taskById = taskResponses.stream()
                .collect(
                        Collectors.toMap(TaskResponse::getId, task -> task, (left, right) -> left, LinkedHashMap::new));
        Map<Long, GoalResponse> goalById = goalResponses.stream()
                .collect(
                        Collectors.toMap(GoalResponse::getId, goal -> goal, (left, right) -> left, LinkedHashMap::new));

        List<String> contextWarnings = new ArrayList<>();
        addContextLimitWarning(contextWarnings, "goals", goalsPage, goalResponses.size());
        addContextLimitWarning(contextWarnings, "tasks", tasksPage, taskResponses.size());
        addContextLimitWarning(contextWarnings, "schedules", schedulesPage, scheduleResponses.size());

        String defaultOpenStatusCode = taskStatuses.stream()
                .filter(status -> !Boolean.TRUE.equals(status.getClosed()))
                .map(ProjectAssistantPromptContext.TaskStatusSnapshot::getCode)
                .findFirst()
                .orElse(null);

        return ProjectAssistantPromptContext.builder()
                .project(ProjectAssistantPromptContext.ProjectSnapshot.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .status(project.getStatus())
                        .managerDisplayName(displayName(project.getManagerUser()))
                        .build())
                .taskStatuses(taskStatuses)
                .goals(goalResponses.stream()
                        .map(goal -> ProjectAssistantPromptContext.GoalSnapshot.builder()
                                .id(goal.getId())
                                .title(goal.getTitle())
                                .goalType(goal.getGoalType())
                                .status(goal.getStatus())
                                .progressPercent(goal.getProgressPercent())
                                .build())
                        .toList())
                .tasks(taskResponses.stream()
                        .map(task -> ProjectAssistantPromptContext.TaskSnapshot.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .goalId(task.getGoalId())
                                .goalTitle(task.getGoalId() != null && goalById.containsKey(task.getGoalId())
                                        ? goalById.get(task.getGoalId()).getTitle()
                                        : null)
                                .statusCode(task.getStatus() != null ? task.getStatus().getCode() : null)
                                .statusName(task.getStatus() != null ? task.getStatus().getName() : null)
                                .priority(task.getPriority())
                                .assigneeDisplayName(displayName(task.getAssignee()))
                                .dueDate(task.getDueDate())
                                .estimatedMinutes(task.getEstimatedMinutes())
                                .completed(task.getIsCompleted())
                                .boardPosition(task.getBoardPosition())
                                .build())
                        .toList())
                .schedules(scheduleResponses.stream()
                        .map(schedule -> ProjectAssistantPromptContext.ScheduleSnapshot.builder()
                                .id(schedule.getId())
                                .taskId(schedule.getTaskId())
                                .taskTitle(taskById.containsKey(schedule.getTaskId())
                                        ? taskById.get(schedule.getTaskId()).getTitle()
                                        : null)
                                .scheduledStart(schedule.getScheduledStart())
                                .scheduledEnd(schedule.getScheduledEnd())
                                .build())
                        .toList())
                .defaultOpenStatusCode(defaultOpenStatusCode)
                .contextWarnings(contextWarnings)
                .build();
    }

    private String buildSystemPrompt() {
        return """
                You are the Chronelis project assistant.
                Return only structured JSON that matches the schema exactly.
                You are planning changes for exactly one existing project.

                Supported action types:
                - UPDATE_PROJECT: use name, description, projectStatus.
                - CREATE_GOAL: use title, goalType, optional goalStatus and progressPercent.
                - UPDATE_GOAL: use goalId and any of title, goalType, goalStatus, progressPercent.
                - CREATE_TASK: use title, optional description, optional goalId, optional priority, optional dueDate, optional estimatedMinutes, and statusCode.
                - UPDATE_TASK: use taskId and any of title, description, goalId, clearGoal, priority, dueDate, estimatedMinutes.
                - MOVE_TASK: use taskId, statusCode, and optional targetPosition.
                - UPDATE_TASK_COMPLETION: use taskId and completed.
                - CREATE_TASK_SCHEDULE: use taskId, scheduledStart, scheduledEnd.
                - UPDATE_TASK_SCHEDULE: use scheduleId, scheduledStart, scheduledEnd.

                Rules:
                1. Use only IDs that already exist in the provided context.
                2. Use only exact statusCode values from the provided context.
                3. Never invent new IDs.
                4. Never create actions that depend on IDs produced by earlier create actions in the same plan.
                5. Do not propose deletes, comments, assignee changes, manager changes, team changes, or task type changes.
                6. Prefer the smallest safe plan.
                7. If the user request is ambiguous or unsafe, return fewer actions and explain blockers in warnings.
                8. Keep the plan at or below %d actions.
                9. Use the same language as the user's request for summary, actionTitle, and rationale.
                10. Use ISO-8601 local date-time values such as 2026-04-12T09:30:00.

                Allowed enum values:
                - projectStatus: ACTIVE, COMPLETED, ARCHIVED
                - goalType: SHORT_TERM, MEDIUM_TERM, LONG_TERM
                - goalStatus: NOT_STARTED, IN_PROGRESS, COMPLETED, ON_HOLD
                - priority: LOW, MEDIUM, HIGH, URGENT
                """
                .formatted(this.properties.getMaxPreviewActions());
    }

    private String buildUserPrompt(Long projectId, ProjectAssistantPreviewRequest request,
            ProjectAssistantPromptContext context) {
        try {
            String contextJson = this.objectMapper.writeValueAsString(context);
            return """
                    Current project ID: %d
                    Maximum actions: %d

                    Project context JSON:
                    %s

                    User request:
                    %s
                    """.formatted(projectId, this.properties.getMaxPreviewActions(), contextJson,
                    request.getPrompt().trim());
        } catch (JsonProcessingException exception) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_UNAVAILABLE,
                    "Không thể chuẩn bị project context cho Gemini.");
        }
    }

    private ProjectAssistantPlan normalizePlan(ProjectAssistantPlan rawPlan, ProjectAssistantPromptContext context) {
        if (rawPlan == null) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_PLAN_INVALID,
                    "Gemini không trả về kế hoạch có cấu trúc hợp lệ.");
        }

        ProjectAssistantPlan normalizedPlan = ProjectAssistantPlan.builder()
                .summary(trimToNull(rawPlan.getSummary()))
                .warnings(new ArrayList<>())
                .actions(new ArrayList<>())
                .build();

        if (rawPlan.getWarnings() != null) {
            rawPlan.getWarnings().stream()
                    .map(this::trimToNull)
                    .filter(Objects::nonNull)
                    .forEach(normalizedPlan.getWarnings()::add);
        }

        if (context.getContextWarnings() != null) {
            normalizedPlan.getWarnings().addAll(context.getContextWarnings());
        }

        List<ProjectAssistantPlannedAction> rawActions = rawPlan.getActions() == null ? List.of()
                : rawPlan.getActions();
        if (rawActions.size() > this.properties.getMaxPreviewActions()) {
            normalizedPlan.getWarnings().add(
                    "Kế hoạch AI vượt quá số action tối đa và đã bị cắt bớt về "
                            + this.properties.getMaxPreviewActions()
                            + " action.");
            rawActions = rawActions.subList(0, this.properties.getMaxPreviewActions());
        }

        ProjectAssistantContextIndex contextIndex = buildContextIndex(context);
        Set<String> usedActionIds = new LinkedHashSet<>();

        for (int index = 0; index < rawActions.size(); index++) {
            ProjectAssistantPlannedAction rawAction = rawActions.get(index);
            ProjectAssistantPlannedAction normalizedAction = normalizeAction(rawAction, index, contextIndex,
                    usedActionIds);
            normalizedPlan.getActions().add(normalizedAction);
        }

        if (!StringUtils.hasText(normalizedPlan.getSummary())) {
            normalizedPlan.setSummary(normalizedPlan.getActions().isEmpty()
                    ? "Không có action nào đủ an toàn để đề xuất."
                    : "Đã tạo kế hoạch hành động cho project.");
        }

        return normalizedPlan;
    }

    private ProjectAssistantPlannedAction normalizeAction(ProjectAssistantPlannedAction rawAction, int index,
            ProjectAssistantContextIndex contextIndex, Set<String> usedActionIds) {
        ProjectAssistantPlannedAction action = rawAction == null ? new ProjectAssistantPlannedAction() : rawAction;

        ProjectAssistantPlannedAction normalizedAction = ProjectAssistantPlannedAction.builder()
                .actionId(resolveActionId(action.getActionId(), index, usedActionIds))
                .order(action.getOrder() != null && action.getOrder() > 0 ? action.getOrder() : index + 1)
                .actionType(action.getActionType())
                .actionTitle(trimToNull(action.getActionTitle()))
                .rationale(trimToNull(action.getRationale()))
                .goalId(action.getGoalId())
                .taskId(action.getTaskId())
                .scheduleId(action.getScheduleId())
                .name(trimToNull(action.getName()))
                .title(trimToNull(action.getTitle()))
                .description(trimToNull(action.getDescription()))
                .projectStatus(action.getProjectStatus())
                .goalType(action.getGoalType())
                .goalStatus(action.getGoalStatus())
                .progressPercent(action.getProgressPercent())
                .priority(action.getPriority())
                .statusCode(normalizeStatusCode(action.getStatusCode()))
                .targetPosition(action.getTargetPosition())
                .clearGoal(Boolean.TRUE.equals(action.getClearGoal()))
                .dueDate(action.getDueDate())
                .estimatedMinutes(action.getEstimatedMinutes())
                .completed(action.getCompleted())
                .scheduledStart(action.getScheduledStart())
                .scheduledEnd(action.getScheduledEnd())
                .validationErrors(new ArrayList<>())
                .build();

        applyDefaults(normalizedAction, contextIndex);
        List<String> validationErrors = validateAction(normalizedAction, contextIndex);
        normalizedAction.setValidationErrors(validationErrors);
        normalizedAction.setExecutable(validationErrors.isEmpty());

        if (!StringUtils.hasText(normalizedAction.getActionTitle())) {
            normalizedAction.setActionTitle(buildFallbackActionTitle(normalizedAction));
        }

        return normalizedAction;
    }

    private void applyDefaults(ProjectAssistantPlannedAction action, ProjectAssistantContextIndex contextIndex) {
        if (action.getActionType() == ProjectAssistantActionType.CREATE_GOAL) {
            if (action.getGoalStatus() == null) {
                action.setGoalStatus(GoalStatusType.NOT_STARTED);
            }
            if (action.getProgressPercent() == null) {
                action.setProgressPercent(BigDecimal.ZERO);
            }
        }

        if (action.getActionType() == ProjectAssistantActionType.CREATE_TASK) {
            if (action.getPriority() == null) {
                action.setPriority(TaskPriorityType.MEDIUM);
            }
            if (action.getEstimatedMinutes() == null) {
                action.setEstimatedMinutes(0);
            }
            if (!StringUtils.hasText(action.getStatusCode())
                    && StringUtils.hasText(contextIndex.defaultOpenStatusCode())) {
                action.setStatusCode(contextIndex.defaultOpenStatusCode());
            }
        }
    }

    private List<String> validateAction(ProjectAssistantPlannedAction action,
            ProjectAssistantContextIndex contextIndex) {
        List<String> errors = new ArrayList<>();

        if (action.getActionType() == null) {
            errors.add("Thiếu actionType.");
            return errors;
        }

        switch (action.getActionType()) {
            case UPDATE_PROJECT -> {
                if (!StringUtils.hasText(action.getName()) && !StringUtils.hasText(action.getDescription())
                        && action.getProjectStatus() == null) {
                    errors.add("UPDATE_PROJECT cần ít nhất một thay đổi ở name, description hoặc projectStatus.");
                }
            }
            case CREATE_GOAL -> {
                if (!StringUtils.hasText(action.getTitle())) {
                    errors.add("CREATE_GOAL cần title.");
                }
                if (action.getGoalType() == null) {
                    errors.add("CREATE_GOAL cần goalType.");
                }
                validateProgress(action.getProgressPercent(), errors);
            }
            case UPDATE_GOAL -> {
                if (action.getGoalId() == null || !contextIndex.goalIds().contains(action.getGoalId())) {
                    errors.add("UPDATE_GOAL cần goalId hợp lệ.");
                }
                if (!StringUtils.hasText(action.getTitle()) && action.getGoalType() == null
                        && action.getGoalStatus() == null
                        && action.getProgressPercent() == null) {
                    errors.add("UPDATE_GOAL cần ít nhất một thay đổi hợp lệ.");
                }
                validateProgress(action.getProgressPercent(), errors);
            }
            case CREATE_TASK -> {
                if (!StringUtils.hasText(action.getTitle())) {
                    errors.add("CREATE_TASK cần title.");
                }
                if (action.getGoalId() != null && !contextIndex.goalIds().contains(action.getGoalId())) {
                    errors.add("CREATE_TASK dùng goalId không tồn tại trong context.");
                }
                if (!StringUtils.hasText(action.getStatusCode())) {
                    errors.add("CREATE_TASK cần statusCode hợp lệ.");
                } else if (!contextIndex.statusIdByCode().containsKey(action.getStatusCode())) {
                    errors.add("CREATE_TASK dùng statusCode không tồn tại.");
                }
                if (action.getEstimatedMinutes() != null && action.getEstimatedMinutes() < 0) {
                    errors.add("estimatedMinutes không được âm.");
                }
            }
            case UPDATE_TASK -> {
                if (action.getTaskId() == null || !contextIndex.taskIds().contains(action.getTaskId())) {
                    errors.add("UPDATE_TASK cần taskId hợp lệ.");
                }
                if (action.getGoalId() != null && !contextIndex.goalIds().contains(action.getGoalId())) {
                    errors.add("UPDATE_TASK dùng goalId không tồn tại trong context.");
                }
                if (Boolean.TRUE.equals(action.getClearGoal()) && action.getGoalId() != null) {
                    errors.add("UPDATE_TASK không thể vừa clearGoal vừa truyền goalId.");
                }
                if (!StringUtils.hasText(action.getTitle()) && !StringUtils.hasText(action.getDescription())
                        && action.getGoalId() == null && !Boolean.TRUE.equals(action.getClearGoal())
                        && action.getPriority() == null && action.getDueDate() == null
                        && action.getEstimatedMinutes() == null) {
                    errors.add("UPDATE_TASK cần ít nhất một thay đổi hợp lệ.");
                }
                if (action.getEstimatedMinutes() != null && action.getEstimatedMinutes() < 0) {
                    errors.add("estimatedMinutes không được âm.");
                }
            }
            case MOVE_TASK -> {
                if (action.getTaskId() == null || !contextIndex.taskIds().contains(action.getTaskId())) {
                    errors.add("MOVE_TASK cần taskId hợp lệ.");
                }
                if (!StringUtils.hasText(action.getStatusCode())
                        || !contextIndex.statusIdByCode().containsKey(action.getStatusCode())) {
                    errors.add("MOVE_TASK cần statusCode tồn tại trong context.");
                }
                if (action.getTargetPosition() != null && action.getTargetPosition() < 0) {
                    errors.add("targetPosition không được âm.");
                }
            }
            case UPDATE_TASK_COMPLETION -> {
                if (action.getTaskId() == null || !contextIndex.taskIds().contains(action.getTaskId())) {
                    errors.add("UPDATE_TASK_COMPLETION cần taskId hợp lệ.");
                }
                if (action.getCompleted() == null) {
                    errors.add("UPDATE_TASK_COMPLETION cần completed.");
                }
            }
            case CREATE_TASK_SCHEDULE -> {
                if (action.getTaskId() == null || !contextIndex.taskIds().contains(action.getTaskId())) {
                    errors.add("CREATE_TASK_SCHEDULE cần taskId hợp lệ.");
                }
                validateScheduleWindow(action.getScheduledStart(), action.getScheduledEnd(), errors);
            }
            case UPDATE_TASK_SCHEDULE -> {
                if (action.getScheduleId() == null || !contextIndex.scheduleIds().contains(action.getScheduleId())) {
                    errors.add("UPDATE_TASK_SCHEDULE cần scheduleId hợp lệ.");
                }
                validateScheduleWindow(action.getScheduledStart(), action.getScheduledEnd(), errors);
            }
            default -> errors.add("Action type không được hỗ trợ.");
        }

        return errors;
    }

    private ProjectAssistantExecutionResult executeAction(Long projectId, ProjectAssistantPlannedAction action) {
        return switch (action.getActionType()) {
            case UPDATE_PROJECT -> {
                ProjectResponse response = this.projectService.updateProject(projectId,
                        UpdateProjectRequest.builder()
                                .name(action.getName())
                                .description(action.getDescription())
                                .status(action.getProjectStatus())
                                .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã cập nhật project.")
                        .projectId(response.getId())
                        .build();
            }
            case CREATE_GOAL -> {
                GoalResponse response = this.goalService.createGoal(CreateGoalRequest.builder()
                        .projectId(projectId)
                        .title(action.getTitle())
                        .goalType(action.getGoalType())
                        .status(action.getGoalStatus())
                        .progressPercent(action.getProgressPercent())
                        .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã tạo goal mới.")
                        .projectId(projectId)
                        .goalId(response.getId())
                        .build();
            }
            case UPDATE_GOAL -> {
                GoalResponse response = this.goalService.updateGoal(action.getGoalId(), UpdateGoalRequest.builder()
                        .title(action.getTitle())
                        .goalType(action.getGoalType())
                        .status(action.getGoalStatus())
                        .progressPercent(action.getProgressPercent())
                        .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã cập nhật goal.")
                        .projectId(projectId)
                        .goalId(response.getId())
                        .build();
            }
            case CREATE_TASK -> {
                Long statusId = resolveStatusId(projectId, action.getStatusCode());
                TaskResponse response = this.taskService.createTask(CreateTaskRequest.builder()
                        .projectId(projectId)
                        .goalId(action.getGoalId())
                        .statusId(statusId)
                        .title(action.getTitle())
                        .description(action.getDescription())
                        .priority(action.getPriority())
                        .dueDate(action.getDueDate())
                        .estimatedMinutes(action.getEstimatedMinutes())
                        .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã tạo task mới.")
                        .projectId(projectId)
                        .goalId(response.getGoalId())
                        .taskId(response.getId())
                        .build();
            }
            case UPDATE_TASK -> {
                TaskResponse response = this.taskService.updateTask(action.getTaskId(), UpdateTaskRequest.builder()
                        .title(action.getTitle())
                        .description(action.getDescription())
                        .goalId(action.getGoalId())
                        .clearGoal(Boolean.TRUE.equals(action.getClearGoal()) ? Boolean.TRUE : null)
                        .priority(action.getPriority())
                        .dueDate(action.getDueDate())
                        .estimatedMinutes(action.getEstimatedMinutes())
                        .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã cập nhật task.")
                        .projectId(projectId)
                        .goalId(response.getGoalId())
                        .taskId(response.getId())
                        .build();
            }
            case MOVE_TASK -> {
                Long statusId = resolveStatusId(projectId, action.getStatusCode());
                TaskResponse response = this.taskService.moveTask(action.getTaskId(), MoveTaskRequest.builder()
                        .statusId(statusId)
                        .targetPosition(action.getTargetPosition())
                        .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã di chuyển task.")
                        .projectId(projectId)
                        .goalId(response.getGoalId())
                        .taskId(response.getId())
                        .build();
            }
            case UPDATE_TASK_COMPLETION -> {
                TaskResponse response = this.taskService.updateTaskCompletion(action.getTaskId(),
                        UpdateTaskCompletionRequest.builder()
                                .isCompleted(action.getCompleted())
                                .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã cập nhật trạng thái hoàn thành task.")
                        .projectId(projectId)
                        .goalId(response.getGoalId())
                        .taskId(response.getId())
                        .build();
            }
            case CREATE_TASK_SCHEDULE -> {
                TaskScheduleResponse response = this.taskScheduleService
                        .createSchedule(CreateTaskScheduleRequest.builder()
                                .taskId(action.getTaskId())
                                .scheduledStart(action.getScheduledStart())
                                .scheduledEnd(action.getScheduledEnd())
                                .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã tạo lịch cho task.")
                        .projectId(projectId)
                        .taskId(response.getTaskId())
                        .scheduleId(response.getId())
                        .build();
            }
            case UPDATE_TASK_SCHEDULE -> {
                TaskScheduleResponse response = this.taskScheduleService.updateSchedule(action.getScheduleId(),
                        UpdateTaskScheduleRequest.builder()
                                .scheduledStart(action.getScheduledStart())
                                .scheduledEnd(action.getScheduledEnd())
                                .build());

                yield ProjectAssistantExecutionResult.builder()
                        .actionId(action.getActionId())
                        .actionType(action.getActionType())
                        .actionTitle(action.getActionTitle())
                        .outcome("Đã cập nhật lịch task.")
                        .projectId(projectId)
                        .taskId(response.getTaskId())
                        .scheduleId(response.getId())
                        .build();
            }
        };
    }

    private Long resolveStatusId(Long projectId, String statusCode) {
        return this.taskStatusRepository.findByProjectIdAndCodeIgnoreCase(projectId, statusCode)
                .map(taskStatus -> taskStatus.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROJECT_ASSISTANT_PLAN_INVALID,
                        "Không tìm thấy task status với code: " + statusCode));
    }

    private ProjectAssistantContextIndex buildContextIndex(ProjectAssistantPromptContext context) {
        Map<String, Long> statusIdByCode = context.getTaskStatuses().stream()
                .filter(status -> StringUtils.hasText(status.getCode()))
                .collect(Collectors.toMap(
                        status -> normalizeStatusCode(status.getCode()),
                        ProjectAssistantPromptContext.TaskStatusSnapshot::getId,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Set<Long> goalIds = context.getGoals().stream()
                .map(ProjectAssistantPromptContext.GoalSnapshot::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> taskIds = context.getTasks().stream()
                .map(ProjectAssistantPromptContext.TaskSnapshot::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> scheduleIds = context.getSchedules().stream()
                .map(ProjectAssistantPromptContext.ScheduleSnapshot::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ProjectAssistantContextIndex(statusIdByCode, goalIds, taskIds, scheduleIds,
                normalizeStatusCode(context.getDefaultOpenStatusCode()));
    }

    private <T> List<T> convertPageContent(PaginationResponse page, TypeReference<List<T>> typeReference) {
        if (page == null || page.getContent() == null) {
            return List.of();
        }
        return this.objectMapper.convertValue(page.getContent(), typeReference);
    }

    private void addContextLimitWarning(List<String> warnings, String label, PaginationResponse page, int loadedCount) {
        if (page != null && page.getMeta() != null && page.getMeta().getTotalElements() > loadedCount) {
            warnings.add("AI context chỉ bao gồm " + loadedCount + " " + label + " đầu tiên.");
        }
    }

    private void validateProgress(BigDecimal progressPercent, List<String> errors) {
        if (progressPercent == null) {
            return;
        }
        if (progressPercent.compareTo(BigDecimal.ZERO) < 0 || progressPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            errors.add("progressPercent phải nằm trong khoảng 0..100.");
        }
    }

    private void validateScheduleWindow(LocalDateTime scheduledStart, LocalDateTime scheduledEnd, List<String> errors) {
        if (scheduledStart == null || scheduledEnd == null) {
            errors.add("scheduledStart và scheduledEnd là bắt buộc.");
            return;
        }
        if (!scheduledEnd.isAfter(scheduledStart)) {
            errors.add("scheduledEnd phải lớn hơn scheduledStart.");
        }
    }

    private String buildFallbackActionTitle(ProjectAssistantPlannedAction action) {
        return switch (action.getActionType()) {
            case UPDATE_PROJECT -> "Cập nhật project";
            case CREATE_GOAL -> StringUtils.hasText(action.getTitle()) ? "Tạo goal " + action.getTitle() : "Tạo goal";
            case UPDATE_GOAL -> action.getGoalId() != null ? "Cập nhật goal #" + action.getGoalId() : "Cập nhật goal";
            case CREATE_TASK -> StringUtils.hasText(action.getTitle()) ? "Tạo task " + action.getTitle() : "Tạo task";
            case UPDATE_TASK -> action.getTaskId() != null ? "Cập nhật task #" + action.getTaskId() : "Cập nhật task";
            case MOVE_TASK -> action.getTaskId() != null && StringUtils.hasText(action.getStatusCode())
                    ? "Di chuyển task #" + action.getTaskId() + " sang " + action.getStatusCode()
                    : "Di chuyển task";
            case UPDATE_TASK_COMPLETION -> action.getTaskId() != null
                    ? "Cập nhật hoàn thành task #" + action.getTaskId()
                    : "Cập nhật hoàn thành task";
            case CREATE_TASK_SCHEDULE ->
                action.getTaskId() != null ? "Tạo lịch cho task #" + action.getTaskId() : "Tạo lịch task";
            case UPDATE_TASK_SCHEDULE -> action.getScheduleId() != null
                    ? "Cập nhật lịch #" + action.getScheduleId()
                    : "Cập nhật lịch task";
        };
    }

    private String displayName(UserSummaryResponse user) {
        if (user == null) {
            return null;
        }

        String fullName = List.of(trimToNull(user.getFirstName()), trimToNull(user.getLastName())).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        if (StringUtils.hasText(fullName) && StringUtils.hasText(user.getEmail())) {
            return fullName + " (" + user.getEmail() + ")";
        }
        if (StringUtils.hasText(fullName)) {
            return fullName;
        }
        return trimToNull(user.getEmail());
    }

    private String resolveActionId(String rawActionId, int index, Set<String> usedActionIds) {
        String candidate = trimToNull(rawActionId);
        if (!StringUtils.hasText(candidate) || usedActionIds.contains(candidate)) {
            candidate = "A" + (index + 1);
            while (usedActionIds.contains(candidate)) {
                candidate = candidate + "_";
            }
        }
        usedActionIds.add(candidate);
        return candidate;
    }

    private String normalizeStatusCode(String statusCode) {
        String normalized = trimToNull(statusCode);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private record ProjectAssistantContextIndex(
            Map<String, Long> statusIdByCode,
            Set<Long> goalIds,
            Set<Long> taskIds,
            Set<Long> scheduleIds,
            String defaultOpenStatusCode) {
    }
}