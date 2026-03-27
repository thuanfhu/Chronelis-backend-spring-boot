# Collaborative Work Management Backend

## 1. System Overview

This module adds a realtime collaborative work-management domain to Chronelis backend with these core scopes:

- Workspace (top-level collaboration scope)
- Project (workspace-contained work unit)
- Goal (project objective)
- Task Status (project Kanban columns)
- Task (core execution item)
- Task Schedule (calendar blocks)
- Task Comment (discussion thread)
- Notification (recipient user in-app alerts)
- Activity Log (workspace-scoped feed/audit-like events)

Existing stable auth/security/websocket/exception modules are reused without redesign.

## 2. Business Flow

1. User creates workspace -> owner member record is auto-created.
2. Workspace manager adds members.
3. Member creates project -> default Kanban statuses are auto-created.
4. Member creates goals and tasks under project.
5. Task can be assigned/moved/reordered/completed.
6. Task can have many schedule blocks and comments.
7. Important actions generate activity logs and notifications.
8. Realtime events are published for frontend sync.

## 3. Entity/Table Explanation

### workspaces

- id, name, owner_id, created_at, updated_at
- one owner user

### workspace_members

- id, workspace_id, user_id, role, joined_at
- unique(workspace_id, user_id)

### projects

- id, workspace_id, name, status, created_by, created_at, updated_at

### goals

- id, project_id, title, goal_type, status, progress_percent, created_by, created_at, updated_at
- progress range validated in service and DB check

### task_statuses

- id, project_id, name, code, position, is_closed, created_at
- unique(project_id, code)

### tasks

- id, project_id, goal_id, status_id, title, priority, assignee_id, created_by,
  due_date, estimated_minutes, board_position, is_completed, completed_at, created_at, updated_at

### task_schedules

- id, task_id, scheduled_start, scheduled_end, scheduled_date, created_by, created_at, updated_at
- scheduled_end > scheduled_start

### task_comments

- id, task_id, user_id, content, created_at, updated_at

### notifications

- id, user_id, type, title, message, reference_type, reference_id, is_read, created_at

### activity_logs

- id, workspace_id, actor_id, action_type, target_type, target_id, description, created_at

## 4. Relationships

- users(1) -> workspaces(N) by owner_id
- users(1) -> workspace_members(N) by user_id
- workspaces(1) -> workspace_members(N)
- workspaces(1) -> projects(N)
- projects(1) -> goals(N)
- projects(1) -> task_statuses(N)
- projects(1) -> tasks(N)
- goals(1) -> tasks(N, nullable goal_id)
- task_statuses(1) -> tasks(N)
- tasks(1) -> task_schedules(N)
- tasks(1) -> task_comments(N)
- users(1) -> task_comments(N)
- users(1) -> notifications(N)
- workspaces(1) -> activity_logs(N)
- users(1) -> activity_logs(N)

Cross-table consistency is enforced in service layer:

- task.status.project must equal task.project
- task.goal.project must equal task.project when goal exists
- assignee must belong to workspace

## 5. API List

### Workspace

- POST /api/v1/workspaces
- PATCH /api/v1/workspaces/{workspaceId}
- GET /api/v1/workspaces/{workspaceId}
- GET /api/v1/workspaces
- POST /api/v1/workspaces/{workspaceId}/members
- GET /api/v1/workspaces/{workspaceId}/members
- PATCH /api/v1/workspaces/{workspaceId}/members/{userId}/role
- DELETE /api/v1/workspaces/{workspaceId}/members/{userId}

### Project

- POST /api/v1/projects
- PATCH /api/v1/projects/{projectId}
- PATCH /api/v1/projects/{projectId}/status
- GET /api/v1/projects/{projectId}
- GET /api/v1/projects/workspace/{workspaceId}

### Goal

- POST /api/v1/goals
- PATCH /api/v1/goals/{goalId}
- GET /api/v1/goals/{goalId}
- GET /api/v1/goals/project/{projectId}
- DELETE /api/v1/goals/{goalId}

### Task Status

- POST /api/v1/task-statuses
- GET /api/v1/task-statuses/project/{projectId}
- PATCH /api/v1/task-statuses/{statusId}
- PATCH /api/v1/task-statuses/project/{projectId}/reorder
- DELETE /api/v1/task-statuses/{statusId}

### Task

- POST /api/v1/tasks
- PATCH /api/v1/tasks/{taskId}
- GET /api/v1/tasks/{taskId}
- GET /api/v1/tasks/project/{projectId}
- GET /api/v1/tasks/goal/{goalId}
- PATCH /api/v1/tasks/{taskId}/move
- PATCH /api/v1/tasks/{taskId}/reorder
- PATCH /api/v1/tasks/{taskId}/assignee
- PATCH /api/v1/tasks/{taskId}/completion
- DELETE /api/v1/tasks/{taskId}

### Task Schedule

- POST /api/v1/task-schedules
- PATCH /api/v1/task-schedules/{scheduleId}
- DELETE /api/v1/task-schedules/{scheduleId}
- GET /api/v1/task-schedules/task/{taskId}
- GET /api/v1/task-schedules/calendar/project/{projectId}
- GET /api/v1/task-schedules/calendar/workspace/{workspaceId}

### Task Comment

- POST /api/v1/task-comments
- PATCH /api/v1/task-comments/{commentId}
- DELETE /api/v1/task-comments/{commentId}
- GET /api/v1/task-comments/task/{taskId}

### Notification

- GET /api/v1/notifications
- GET /api/v1/notifications/unread-count
- PATCH /api/v1/notifications/{notificationId}/read
- PATCH /api/v1/notifications/read-all

### Activity Log

- GET /api/v1/activity-logs/workspace/{workspaceId}

## 6. Request/Response Details

All APIs follow existing wrapper:

- success
- message
- data
- meta(timestamp, instance)

Main write request DTOs:

- CreateWorkspaceRequest, AddWorkspaceMemberRequest
- CreateProjectRequest
- CreateGoalRequest
- CreateTaskStatusRequest
- CreateTaskRequest, MoveTaskRequest, AssignTaskRequest, UpdateTaskCompletionRequest
- CreateTaskScheduleRequest
- CreateTaskCommentRequest

Main response DTOs:

- WorkspaceResponse, WorkspaceMemberResponse
- ProjectResponse
- GoalResponse
- TaskStatusResponse
- TaskResponse
- TaskScheduleResponse
- TaskCommentResponse
- NotificationResponse, NotificationUnreadCountResponse
- ActivityLogResponse

Pagination APIs use PaginationResponse(meta, content).

## 7. WebSocket Events

Module publishes STOMP events via existing SimpMessagingTemplate infrastructure.

Broadcast scopes:

- /public/workspaces/{workspaceId}/events
- /public/workspaces/{workspaceId}/projects/{projectId}/events
- /public/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/events

Private user scopes:

- /user/private/notifications
- /user/private/notifications/unread-count

Event types emitted:

- workspace.created, workspace.updated
- workspace.member.added, workspace.member.role-updated, workspace.member.removed
- project.created, project.updated, project.status-updated
- goal.created, goal.updated, goal.deleted
- task-status.created, task-status.updated, task-status.reordered, task-status.deleted
- task.created, task.updated, task.moved, task.reordered, task.assigned, task.unassigned, task.completion-updated, task.deleted
- task-schedule.created, task-schedule.updated, task-schedule.deleted
- task-comment.added, task-comment.updated, task-comment.deleted
- notification.created

## 8. Validation Notes for React Frontend

- Use enum values exactly as backend expects.
- For schedule inputs, enforce end > start before submit.
- For goal progress, clamp [0,100].
- For task reorder/move, send zero-based targetPosition.
- For calendar APIs, send fromDate/toDate as ISO date (yyyy-MM-dd).
- For activity logs, filter params are optional.

## 9. Assumptions and Limitations

1. Existing users table uses String user_id UUID; foreign keys in new module follow this existing contract.
2. Existing project baseline has unrelated compile blockers (Hub/Pledge/RentalOrder classes missing); this module is implemented independently and does not modify those locked modules.
3. Endpoint permission seeding for role-permission matrix is not auto-expanded in this change; if permission interceptor is active, add permissions through existing permission management flow.
4. Goal deletion is safe-reject when linked tasks exist.
5. Status deletion is safe-reject when tasks exist.
