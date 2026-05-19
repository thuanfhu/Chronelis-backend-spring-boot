# API Description - Chronelis

## 1. Phạm vi tài liệu

Tài liệu này mô tả API của Chronelis theo style Postman, bám theo code hiện tại ở thời điểm rà soát:

- backend: controller, request DTO, response DTO, service rule
- frontend: route và cách gọi API thực tế

Mục tiêu của file này là để frontend, QA và người viết báo cáo có thể biết rõ:

1. endpoint nào đang tồn tại
2. URL request là gì
3. body request gồm các field nào
4. response data có cấu trúc ra sao
5. các ràng buộc nghiệp vụ đi kèm từng endpoint

## 2. Quy ước chung

### 2.1. Base URL

`/api/v1`

### 2.2. Phân loại endpoint

- `[PUBLIC]`: không cần `Authorization`
- `[AUTH]`: cần `Authorization: Bearer <access_token>`

### 2.3. Auth model

- Access token trả trong response body.
- Refresh token nằm ở cookie `refresh_token` kiểu HttpOnly.
- Ngoài JWT, backend còn kiểm tra permission động theo `apiPath + httpMethod` trong database.

### 2.4. Response wrapper chuẩn

```json
{
    "success": true,
    "message": "Mô tả kết quả",
    "data": {},
    "meta": {
        "timestamp": "2026-04-11T10:00:00",
        "instance": "/api/v1/..."
    }
}
```

### 2.5. Response lỗi chuẩn

```json
{
    "success": false,
    "errors": [
        {
            "code": 1001,
            "message": "Mô tả lỗi",
            "field": "optional",
            "resource": "optional"
        }
    ],
    "meta": {
        "timestamp": "2026-04-11T10:00:00",
        "instance": "/api/v1/..."
    }
}
```

### 2.6. Pagination query params

Backend bật `spring.data.web.pageable.one-indexed-parameters=true`, vì vậy:

- trang đầu tiên là `page=1`
- query mẫu: `?page=1&size=10&sort=createdAt,desc`

### 2.7. Filter DSL

Một số API admin hỗ trợ `SpringFilter DSL`, ví dụ:

`?filter=name:'ADMIN' and active:true`

Áp dụng cho:

- `GET /users`
- `GET /roles`
- `GET /permissions`

## 3. Danh sách endpoint theo module

## 3.1. Module Auth

| ID       | Method | URL                                  | Auth       | Request                    | Response data            | Ghi chú                                |
| -------- | ------ | ------------------------------------ | ---------- | -------------------------- | ------------------------ | -------------------------------------- |
| AUTH-001 | POST   | `/api/v1/auth/register`              | `[PUBLIC]` | `RegisterUserRequest`      | `null`                   | đăng ký và gửi mail verify             |
| AUTH-002 | POST   | `/api/v1/auth/verify-active-account` | `[PUBLIC]` | `VerifyEmailRequest`       | `AuthenticationResponse` | kích hoạt tài khoản                    |
| AUTH-003 | POST   | `/api/v1/auth/resend-verify`         | `[PUBLIC]` | `ResendVerifyEmailRequest` | `null`                   | gửi lại mail verify                    |
| AUTH-004 | POST   | `/api/v1/auth/login`                 | `[PUBLIC]` | `LoginRequest`             | `AuthenticationResponse` | set cookie refresh token               |
| AUTH-005 | POST   | `/api/v1/auth/logout`                | `[AUTH]`   | không có body              | `null`                   | blacklist token + clear refresh cookie |
| AUTH-006 | GET    | `/api/v1/auth/account`               | `[AUTH]`   | không có body              | `UserSecureResponse`     | lấy profile hiện tại                   |
| AUTH-007 | GET    | `/api/v1/auth/refresh`               | `[PUBLIC]` | cookie `refresh_token`     | `AuthenticationResponse` | cấp access token mới                   |
| AUTH-008 | POST   | `/api/v1/auth/forgot-password`       | `[PUBLIC]` | `ForgotPasswordRequest`    | `null`                   | gửi mail reset                         |
| AUTH-009 | POST   | `/api/v1/auth/reset-password`        | `[PUBLIC]` | `ResetPasswordRequest`     | `null`                   | đặt lại mật khẩu                       |

## 3.2. Module Users

| ID       | Method | URL                                 | Auth     | Request                                | Response data                      | Ghi chú                                       |
| -------- | ------ | ----------------------------------- | -------- | -------------------------------------- | ---------------------------------- | --------------------------------------------- |
| USER-001 | PATCH  | `/api/v1/users/update-profile`      | `[AUTH]` | `UpdateUserProfileRequest`             | `UserSecureResponse`               | cập nhật hồ sơ cá nhân                        |
| USER-002 | PUT    | `/api/v1/users/update-password`     | `[AUTH]` | `UpdateUserPasswordRequest`            | `UserSecureResponse`               | đổi mật khẩu                                  |
| USER-003 | PUT    | `/api/v1/users/update-email`        | `[AUTH]` | `UpdateUserEmailRequest`               | `null`                             | gửi mail xác minh email mới                   |
| USER-004 | POST   | `/api/v1/users/verify-change-email` | `[AUTH]` | `VerifyEmailRequest`                   | `UserSecureResponse`               | xác thực đổi email                            |
| USER-005 | GET    | `/api/v1/users`                     | `[AUTH]` | query `filter`, `page`, `size`, `sort` | `PaginationResponse<UserResponse>` | danh sách user                                |
| USER-006 | GET    | `/api/v1/users/{userId}`            | `[AUTH]` | path `userId`                          | `UserSecureResponse`               | chi tiết user                                 |
| USER-007 | PATCH  | `/api/v1/users/{userId}`            | `[AUTH]` | `UpdateUserForAdminRequest`            | `UserResponse`                     | admin cập nhật user                           |
| USER-008 | DELETE | `/api/v1/users/{userId}`            | `[AUTH]` | path `userId`                          | `null`                             | admin xóa user                                |
| USER-009 | DELETE | `/api/v1/users/{userId}/roles`      | `[AUTH]` | `DeleteRoleFromUserRequest`            | `null`                             | gỡ role khỏi user                             |
| USER-010 | POST   | `/api/v1/users/staff-requests`      | `[AUTH]` | không có body                          | `UserSecureResponse`               | endpoint đặc biệt, cần rà soát thêm semantics |

## 3.3. Module Roles

| ID       | Method | URL                                  | Auth     | Request                                | Response data                      | Ghi chú                 |
| -------- | ------ | ------------------------------------ | -------- | -------------------------------------- | ---------------------------------- | ----------------------- |
| ROLE-001 | POST   | `/api/v1/roles`                      | `[AUTH]` | `CreateRoleRequest`                    | `RoleResponse`                     | tạo role                |
| ROLE-002 | GET    | `/api/v1/roles/{roleId}`             | `[AUTH]` | path `roleId`                          | `RoleResponse`                     | chi tiết role           |
| ROLE-003 | GET    | `/api/v1/roles`                      | `[AUTH]` | query `filter`, `page`, `size`, `sort` | `PaginationResponse<RoleResponse>` | danh sách role          |
| ROLE-004 | PATCH  | `/api/v1/roles/{roleId}`             | `[AUTH]` | `UpdateRoleRequest`                    | `RoleResponse`                     | cập nhật role           |
| ROLE-005 | DELETE | `/api/v1/roles/{roleId}/permissions` | `[AUTH]` | `DeletePermissionFromRoleRequest`      | `null`                             | gỡ permission khỏi role |
| ROLE-006 | DELETE | `/api/v1/roles/{roleId}`             | `[AUTH]` | path `roleId`                          | `null`                             | xóa role                |

## 3.4. Module Permissions

| ID       | Method | URL                                  | Auth     | Request                                | Response data                            | Ghi chú                             |
| -------- | ------ | ------------------------------------ | -------- | -------------------------------------- | ---------------------------------------- | ----------------------------------- |
| PERM-001 | POST   | `/api/v1/permissions/module`         | `[AUTH]` | `CreateModuleRequest`                  | `List<PermissionResponse>`               | gắn module cho danh sách permission |
| PERM-002 | DELETE | `/api/v1/permissions/module/{name}`  | `[AUTH]` | path `name`                            | `null`                                   | xóa module theo tên                 |
| PERM-003 | GET    | `/api/v1/permissions/modules`        | `[AUTH]` | không có body                          | `List<String>`                           | danh sách module                    |
| PERM-004 | POST   | `/api/v1/permissions`                | `[AUTH]` | `CreatePermissionRequest`              | `PermissionResponse`                     | tạo permission                      |
| PERM-005 | PATCH  | `/api/v1/permissions/{permissionId}` | `[AUTH]` | `UpdatePermissionRequest`              | `PermissionResponse`                     | cập nhật permission                 |
| PERM-006 | GET    | `/api/v1/permissions/{permissionId}` | `[AUTH]` | path `permissionId`                    | `PermissionResponse`                     | chi tiết permission                 |
| PERM-007 | GET    | `/api/v1/permissions`                | `[AUTH]` | query `filter`, `page`, `size`, `sort` | `PaginationResponse<PermissionResponse>` | danh sách permission                |
| PERM-008 | DELETE | `/api/v1/permissions/{permissionId}` | `[AUTH]` | path `permissionId`                    | `null`                                   | xóa permission                      |

## 3.5. Module Workspaces

| ID     | Method | URL                                                      | Auth     | Request                            | Response data                           | Ghi chú                       |
| ------ | ------ | -------------------------------------------------------- | -------- | ---------------------------------- | --------------------------------------- | ----------------------------- |
| WS-001 | POST   | `/api/v1/workspaces`                                     | `[AUTH]` | `CreateWorkspaceRequest`           | `WorkspaceResponse`                     | tạo workspace                 |
| WS-002 | PATCH  | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | `UpdateWorkspaceRequest`           | `WorkspaceResponse`                     | owner workspace               |
| WS-003 | GET    | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | path `workspaceId`                 | `WorkspaceResponse`                     | member workspace mới xem được |
| WS-004 | GET    | `/api/v1/workspaces`                                     | `[AUTH]` | query `page`, `size`, `sort`       | `PaginationResponse<WorkspaceResponse>` | workspace nhìn thấy được      |
| WS-005 | POST   | `/api/v1/workspaces/{workspaceId}/members`               | `[AUTH]` | `AddWorkspaceMemberRequest`        | `WorkspaceMemberResponse`               | owner only                    |
| WS-006 | GET    | `/api/v1/workspaces/{workspaceId}/members`               | `[AUTH]` | path `workspaceId`                 | `List<WorkspaceMemberResponse>`         | member workspace              |
| WS-007 | PATCH  | `/api/v1/workspaces/{workspaceId}/members/{userId}/role` | `[AUTH]` | `UpdateWorkspaceMemberRoleRequest` | `WorkspaceMemberResponse`               | owner only                    |
| WS-008 | DELETE | `/api/v1/workspaces/{workspaceId}/members/{userId}`      | `[AUTH]` | path params                        | `null`                                  | owner only                    |
| WS-009 | DELETE | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | path `workspaceId`                 | `null`                                  | xóa workspace                 |

## 3.6. Module Workspace Invites

| ID      | Method | URL                                                 | Auth     | Request                        | Response data                   | Ghi chú                      |
| ------- | ------ | --------------------------------------------------- | -------- | ------------------------------ | ------------------------------- | ---------------------------- |
| INV-001 | POST   | `/api/v1/workspace-invites`                         | `[AUTH]` | `CreateWorkspaceInviteRequest` | `WorkspaceInviteResponse`       | owner only                   |
| INV-002 | GET    | `/api/v1/workspace-invites/workspace/{workspaceId}` | `[AUTH]` | path `workspaceId`             | `List<WorkspaceInviteResponse>` | member workspace             |
| INV-003 | PATCH  | `/api/v1/workspace-invites/{inviteId}/revoke`       | `[AUTH]` | path `inviteId`                | `null`                          | owner only                   |
| INV-004 | GET    | `/api/v1/workspace-invites/validate/{inviteCode}`   | `[AUTH]` | path `inviteCode`              | `WorkspaceInviteResponse`       | validate code trước khi join |
| INV-005 | POST   | `/api/v1/workspace-invites/join`                    | `[AUTH]` | `JoinByInviteRequest`          | `null`                          | tham gia workspace           |

## 3.7. Module Workspace Teams

| ID       | Method | URL                                                 | Auth     | Request                      | Response data                       | Ghi chú          |
| -------- | ------ | --------------------------------------------------- | -------- | ---------------------------- | ----------------------------------- | ---------------- |
| TEAM-001 | POST   | `/api/v1/workspace-teams`                           | `[AUTH]` | `CreateWorkspaceTeamRequest` | `WorkspaceTeamResponse`             | owner only       |
| TEAM-002 | PATCH  | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | `UpdateWorkspaceTeamRequest` | `WorkspaceTeamResponse`             | owner only       |
| TEAM-003 | GET    | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | path `teamId`                | `WorkspaceTeamResponse`             | member workspace |
| TEAM-004 | GET    | `/api/v1/workspace-teams/workspace/{workspaceId}`   | `[AUTH]` | path `workspaceId`           | `List<WorkspaceTeamResponse>`       | member workspace |
| TEAM-005 | DELETE | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | path `teamId`                | `null`                              | owner only       |
| TEAM-006 | POST   | `/api/v1/workspace-teams/{teamId}/members`          | `[AUTH]` | `AddTeamMemberRequest`       | `WorkspaceTeamMemberResponse`       | owner only       |
| TEAM-007 | DELETE | `/api/v1/workspace-teams/{teamId}/members/{userId}` | `[AUTH]` | path params                  | `null`                              | owner only       |
| TEAM-008 | GET    | `/api/v1/workspace-teams/{teamId}/members`          | `[AUTH]` | path `teamId`                | `List<WorkspaceTeamMemberResponse>` | member workspace |

## 3.8. Module Projects

| ID       | Method | URL                                        | Auth     | Request                      | Response data                         | Ghi chú                                          |
| -------- | ------ | ------------------------------------------ | -------- | ---------------------------- | ------------------------------------- | ------------------------------------------------ |
| PROJ-001 | POST   | `/api/v1/projects`                         | `[AUTH]` | `CreateProjectRequest`       | `ProjectResponse`                     | owner workspace; set manager yêu cầu owner |
| PROJ-002 | PATCH  | `/api/v1/projects/{projectId}`             | `[AUTH]` | `UpdateProjectRequest`       | `ProjectResponse`                     | project manager hoặc owner workspace       |
| PROJ-003 | PATCH  | `/api/v1/projects/{projectId}/status`      | `[AUTH]` | `UpdateProjectStatusRequest` | `ProjectResponse`                     | đổi trạng thái project                           |
| PROJ-004 | GET    | `/api/v1/projects/{projectId}`             | `[AUTH]` | path `projectId`             | `ProjectResponse`                     | member workspace                                 |
| PROJ-005 | GET    | `/api/v1/projects/workspace/{workspaceId}` | `[AUTH]` | query `page`, `size`, `sort` | `PaginationResponse<ProjectResponse>` | project theo workspace                           |
| PROJ-006 | DELETE | `/api/v1/projects/{projectId}`             | `[AUTH]` | path `projectId`             | `null`                                | xóa project                                      |
| PROJ-007 | GET    | `/api/v1/projects/{projectId}/analytics`   | `[AUTH]` | path `projectId`             | `ProjectAnalyticsResponse`            | thống kê trend/completion của project            |

## 3.9. Module Goals

| ID       | Method | URL                                 | Auth     | Request                      | Response data                      | Ghi chú                                         |
| -------- | ------ | ----------------------------------- | -------- | ---------------------------- | ---------------------------------- | ----------------------------------------------- |
| GOAL-001 | POST   | `/api/v1/goals`                     | `[AUTH]` | `CreateGoalRequest`          | `GoalResponse`                     | manager project; set manager goal yêu cầu owner |
| GOAL-002 | PATCH  | `/api/v1/goals/{goalId}`            | `[AUTH]` | `UpdateGoalRequest`          | `GoalResponse`                     | manager goal/project                            |
| GOAL-003 | GET    | `/api/v1/goals/{goalId}`            | `[AUTH]` | path `goalId`                | `GoalResponse`                     | member workspace                                |
| GOAL-004 | GET    | `/api/v1/goals/project/{projectId}` | `[AUTH]` | query `page`, `size`, `sort` | `PaginationResponse<GoalResponse>` | goals theo project                              |
| GOAL-005 | DELETE | `/api/v1/goals/{goalId}`            | `[AUTH]` | path `goalId`                | `null`                             | xóa goal                                        |

## 3.10. Module Task Statuses

| ID          | Method | URL                                                 | Auth     | Request                      | Response data              | Ghi chú             |
| ----------- | ------ | --------------------------------------------------- | -------- | ---------------------------- | -------------------------- | ------------------- |
| TSTATUS-001 | POST   | `/api/v1/task-statuses`                             | `[AUTH]` | `CreateTaskStatusRequest`    | `TaskStatusResponse`       | tạo cột Kanban      |
| TSTATUS-002 | GET    | `/api/v1/task-statuses/project/{projectId}`         | `[AUTH]` | path `projectId`             | `List<TaskStatusResponse>` | danh sách cột       |
| TSTATUS-003 | PATCH  | `/api/v1/task-statuses/{statusId}`                  | `[AUTH]` | `UpdateTaskStatusRequest`    | `TaskStatusResponse`       | sửa cột             |
| TSTATUS-004 | PATCH  | `/api/v1/task-statuses/project/{projectId}/reorder` | `[AUTH]` | `ReorderTaskStatusesRequest` | `List<TaskStatusResponse>` | reorder toàn bộ cột |
| TSTATUS-005 | DELETE | `/api/v1/task-statuses/{statusId}`                  | `[AUTH]` | path `statusId`              | `null`                     | xóa cột             |

## 3.11. Module Task Types

| ID        | Method | URL                                      | Auth     | Request                 | Response data            | Ghi chú            |
| --------- | ------ | ---------------------------------------- | -------- | ----------------------- | ------------------------ | ------------------ |
| TTYPE-001 | POST   | `/api/v1/task-types`                     | `[AUTH]` | `CreateTaskTypeRequest` | `TaskTypeResponse`       | tạo loại task      |
| TTYPE-002 | PATCH  | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` | `UpdateTaskTypeRequest` | `TaskTypeResponse`       | sửa loại task      |
| TTYPE-003 | GET    | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` | path `taskTypeId`       | `TaskTypeResponse`       | chi tiết task type |
| TTYPE-004 | GET    | `/api/v1/task-types/project/{projectId}` | `[AUTH]` | path `projectId`        | `List<TaskTypeResponse>` | list theo project  |
| TTYPE-005 | DELETE | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` | path `taskTypeId`       | `null`                   | xóa task type      |

## 3.12. Module Tasks

| ID       | Method | URL                                   | Auth     | Request                         | Response data                      | Ghi chú                            |
| -------- | ------ | ------------------------------------- | -------- | ------------------------------- | ---------------------------------- | ---------------------------------- |
| TASK-001 | POST   | `/api/v1/tasks`                       | `[AUTH]` | `CreateTaskRequest`             | `TaskResponse`                     | tạo task                           |
| TASK-002 | PATCH  | `/api/v1/tasks/{taskId}`              | `[AUTH]` | `UpdateTaskRequest`             | `TaskResponse`                     | cập nhật task                      |
| TASK-003 | GET    | `/api/v1/tasks/{taskId}`              | `[AUTH]` | path `taskId`                   | `TaskResponse`                     | chi tiết task                      |
| TASK-004 | GET    | `/api/v1/tasks/project/{projectId}`   | `[AUTH]` | query `page`, `size`, `sort`    | `PaginationResponse<TaskResponse>` | list theo project                  |
| TASK-005 | GET    | `/api/v1/tasks/goal/{goalId}`         | `[AUTH]` | query `page`, `size`, `sort`    | `PaginationResponse<TaskResponse>` | list theo goal                     |
| TASK-006 | PATCH  | `/api/v1/tasks/{taskId}/move`         | `[AUTH]` | `MoveTaskRequest`               | `TaskResponse`                     | đổi cột task                       |
| TASK-007 | PATCH  | `/api/v1/tasks/{taskId}/reorder`      | `[AUTH]` | `ReorderTaskRequest`            | `TaskResponse`                     | reorder trong cột                  |
| TASK-008 | PATCH  | `/api/v1/tasks/{taskId}/assignee`     | `[AUTH]` | `AssignTaskRequest`             | `TaskResponse`                     | assign/unassign                    |
| TASK-009 | PATCH  | `/api/v1/tasks/{taskId}/completion`   | `[AUTH]` | `UpdateTaskCompletionRequest`   | `TaskResponse`                     | mark complete / incomplete         |
| TASK-010 | DELETE | `/api/v1/tasks/{taskId}`              | `[AUTH]` | path `taskId`                   | `null`                             | xóa task                           |
| TASK-011 | GET    | `/api/v1/tasks/my-work`               | `[AUTH]` | không có body                   | `MyWorkResponse`                   | execution hub cá nhân              |
| TASK-012 | GET    | `/api/v1/tasks/{taskId}/dependencies` | `[AUTH]` | path `taskId`                   | `TaskDependencyDetailsResponse`    | dependency + blocker của task      |
| TASK-013 | PUT    | `/api/v1/tasks/{taskId}/dependencies` | `[AUTH]` | `UpdateTaskDependenciesRequest` | `TaskDependencyDetailsResponse`    | ghi đè toàn bộ danh sách phụ thuộc |
| TASK-014 | GET    | `/api/v1/tasks/analytics`             | `[AUTH]` | không có body                   | `TaskAnalyticsResponse`            | thống kê task cá nhân hiện tại     |

## 3.13. Module Task Schedules

| ID       | Method | URL                                                       | Auth     | Request                                            | Response data                              | Ghi chú             |
| -------- | ------ | --------------------------------------------------------- | -------- | -------------------------------------------------- | ------------------------------------------ | ------------------- |
| TSCH-001 | POST   | `/api/v1/task-schedules`                                  | `[AUTH]` | `CreateTaskScheduleRequest`                        | `TaskScheduleResponse`                     | tạo lịch task       |
| TSCH-002 | PATCH  | `/api/v1/task-schedules/{scheduleId}`                     | `[AUTH]` | `UpdateTaskScheduleRequest`                        | `TaskScheduleResponse`                     | sửa lịch task       |
| TSCH-003 | DELETE | `/api/v1/task-schedules/{scheduleId}`                     | `[AUTH]` | path `scheduleId`                                  | `null`                                     | xóa lịch task       |
| TSCH-004 | GET    | `/api/v1/task-schedules/task/{taskId}`                    | `[AUTH]` | path `taskId`                                      | `List<TaskScheduleResponse>`               | lịch của task       |
| TSCH-005 | GET    | `/api/v1/task-schedules/calendar/project/{projectId}`     | `[AUTH]` | query `fromDate`, `toDate`, `page`, `size`, `sort` | `PaginationResponse<TaskScheduleResponse>` | lịch theo project   |
| TSCH-006 | GET    | `/api/v1/task-schedules/calendar/workspace/{workspaceId}` | `[AUTH]` | query `fromDate`, `toDate`, `page`, `size`, `sort` | `PaginationResponse<TaskScheduleResponse>` | lịch theo workspace |

## 3.14. Module Task Comments

| ID       | Method | URL                                   | Auth     | Request                    | Response data               | Ghi chú                |
| -------- | ------ | ------------------------------------- | -------- | -------------------------- | --------------------------- | ---------------------- |
| TCOM-001 | POST   | `/api/v1/task-comments`               | `[AUTH]` | `CreateTaskCommentRequest` | `TaskCommentResponse`       | thêm bình luận         |
| TCOM-002 | PATCH  | `/api/v1/task-comments/{commentId}`   | `[AUTH]` | `UpdateTaskCommentRequest` | `TaskCommentResponse`       | sửa bình luận          |
| TCOM-003 | DELETE | `/api/v1/task-comments/{commentId}`   | `[AUTH]` | path `commentId`           | `null`                      | xóa bình luận          |
| TCOM-004 | GET    | `/api/v1/task-comments/task/{taskId}` | `[AUTH]` | path `taskId`              | `List<TaskCommentResponse>` | list comment theo task |

## 3.15. Module Notifications

| ID       | Method | URL                                           | Auth     | Request                      | Response data                              | Ghi chú                        |
| -------- | ------ | --------------------------------------------- | -------- | ---------------------------- | ------------------------------------------ | ------------------------------ |
| NOTI-001 | GET    | `/api/v1/notifications`                       | `[AUTH]` | query `page`, `size`, `sort` | `PaginationResponse<NotificationResponse>` | notification của user hiện tại |
| NOTI-002 | GET    | `/api/v1/notifications/unread-count`          | `[AUTH]` | không có body                | `NotificationUnreadCountResponse`          | số chưa đọc                    |
| NOTI-003 | PATCH  | `/api/v1/notifications/{notificationId}/read` | `[AUTH]` | path `notificationId`        | `null`                                     | đánh dấu 1 notification        |
| NOTI-004 | PATCH  | `/api/v1/notifications/read-all`              | `[AUTH]` | không có body                | `null`                                     | đánh dấu tất cả                |

## 3.16. Module Activity Logs

| ID      | Method | URL                                             | Auth     | Request                                                                                           | Response data                             | Ghi chú                  |
| ------- | ------ | ----------------------------------------------- | -------- | ------------------------------------------------------------------------------------------------- | ----------------------------------------- | ------------------------ |
| ACT-001 | GET    | `/api/v1/activity-logs/workspace/{workspaceId}` | `[AUTH]` | query `actorId`, `actionType`, `targetType`, `fromDateTime`, `toDateTime`, `page`, `size`, `sort` | `PaginationResponse<ActivityLogResponse>` | audit log theo workspace |

## 3.17. Module Project Access

| ID       | Method | URL                                           | Auth     | Request                      | Response data                    | Ghi chú                                      |
| -------- | ------ | --------------------------------------------- | -------- | ---------------------------- | -------------------------------- | -------------------------------------------- |
| PACC-001 | GET    | `/api/v1/projects/{projectId}/access`         | `[AUTH]` | path `projectId`             | `List<ProjectAccessResponse>`    | danh sách grant của project                  |
| PACC-002 | GET    | `/api/v1/projects/{projectId}/access/me`      | `[AUTH]` | path `projectId`             | `EffectiveProjectAccessResponse` | quyền hiệu lực của user hiện tại             |
| PACC-003 | POST   | `/api/v1/projects/{projectId}/access`         | `[AUTH]` | `UpsertProjectAccessRequest` | `ProjectAccessResponse`          | cấp/cập nhật quyền user hoặc team            |
| PACC-004 | PATCH  | `/api/v1/projects/{projectId}/access/{accessId}` | `[AUTH]` | `UpdateProjectAccessRequest` | `ProjectAccessResponse`          | đổi role của grant                           |
| PACC-005 | DELETE | `/api/v1/projects/{projectId}/access/{accessId}` | `[AUTH]` | path `projectId`, `accessId` | `null`                           | thu hồi grant                                |

## 3.18. Module Pomodoro

| ID       | Method | URL                               | Auth     | Request                          | Response data            | Ghi chú                       |
| -------- | ------ | --------------------------------- | -------- | -------------------------------- | ------------------------ | ----------------------------- |
| POMO-001 | POST   | `/api/v1/pomodoro/tasks/{taskId}` | `[AUTH]` | `SavePomodoroSessionRequest`     | `PomodoroSessionResponse` | lưu một phiên focus đã hoàn tất |
| POMO-002 | GET    | `/api/v1/pomodoro/tasks/{taskId}` | `[AUTH]` | path `taskId`                    | `List<PomodoroSessionResponse>` | lịch sử Pomodoro của user hiện tại trên task |

## 3.19. Module Storage Azure Blob

| ID       | Method | URL                                                        | Auth     | Request                               | Response data          | Ghi chú              |
| -------- | ------ | ---------------------------------------------------------- | -------- | ------------------------------------- | ---------------------- | -------------------- |
| AZBL-001 | POST   | `/api/v1/storage/azure-blob/upload/single`                 | `[AUTH]` | `SingleUploadFileRequest` form-data   | `SingleFileResponse`   | upload 1 file        |
| AZBL-002 | POST   | `/api/v1/storage/azure-blob/upload/multiple`               | `[AUTH]` | `MultipleUploadFileRequest` form-data | `MultipleFileResponse` | upload nhiều file    |
| AZBL-003 | DELETE | `/api/v1/storage/azure-blob/delete/single?filePath=...`    | `[AUTH]` | query `filePath`                      | `String/null`          | xóa 1 file           |
| AZBL-004 | DELETE | `/api/v1/storage/azure-blob/delete/multiple`               | `[AUTH]` | `MultipleDeleteFileRequest`           | `String/null`          | xóa nhiều file       |
| AZBL-005 | PUT    | `/api/v1/storage/azure-blob/move/single`                   | `[AUTH]` | `SingleMoveFileRequest`               | `String`               | di chuyển 1 file     |
| AZBL-006 | PUT    | `/api/v1/storage/azure-blob/move/multiple`                 | `[AUTH]` | `MultipleMoveFileRequest`             | `String`               | di chuyển nhiều file |

## 4. Chi tiết request DTO

## 4.1. Auth request DTOs

### RegisterUserRequest

| Field             | Type     | Bắt buộc | Mô tả                                                 |
| ----------------- | -------- | -------- | ----------------------------------------------------- |
| `email`           | `string` | có       | chỉ chấp nhận `gmail.com` hoặc `yopmail.com`          |
| `phoneNumber`     | `string` | có       | số điện thoại Việt Nam                                |
| `password`        | `string` | có       | tối thiểu 8 ký tự, có hoa, thường, số, ký tự đặc biệt |
| `confirmPassword` | `string` | có       | phải khớp `password`                                  |
| `firstName`       | `string` | có       | độ dài 2..50                                          |
| `lastName`        | `string` | có       | độ dài 2..50                                          |

### LoginRequest

| Field         | Type     | Bắt buộc | Mô tả                   |
| ------------- | -------- | -------- | ----------------------- |
| `email`       | `string` | tùy chọn | email đăng nhập         |
| `phoneNumber` | `string` | tùy chọn | số điện thoại đăng nhập |
| `password`    | `string` | có       | mật khẩu                |

Ghi chú: phải có `email` hoặc `phoneNumber`.

### VerifyEmailRequest

| Field   | Type     | Bắt buộc | Mô tả                                  |
| ------- | -------- | -------- | -------------------------------------- |
| `token` | `string` | có       | JWT từ email verify/reset/change-email |

### ResendVerifyEmailRequest / ForgotPasswordRequest

| Field   | Type     | Bắt buộc | Mô tả                           |
| ------- | -------- | -------- | ------------------------------- |
| `email` | `string` | có       | email hợp lệ theo rule hệ thống |

### ResetPasswordRequest

| Field             | Type     | Bắt buộc | Mô tả                   |
| ----------------- | -------- | -------- | ----------------------- |
| `token`           | `string` | có       | token từ email          |
| `newPassword`     | `string` | có       | mật khẩu mới            |
| `confirmPassword` | `string` | có       | phải khớp `newPassword` |

## 4.2. User/Admin request DTOs

### UpdateUserProfileRequest

| Field         | Type     | Bắt buộc | Mô tả        |
| ------------- | -------- | -------- | ------------ |
| `firstName`   | `string` | không    | 2..50 ký tự  |
| `lastName`    | `string` | không    | 2..50 ký tự  |
| `nickname`    | `string` | không    | 2..50 ký tự  |
| `avatarUrl`   | `string` | không    | URL avatar   |
| `biography`   | `string` | không    | tiểu sử ngắn |
| `city`        | `string` | không    | thành phố    |
| `nationality` | `string` | không    | quốc tịch    |

### UpdateUserPasswordRequest

| Field             | Type     | Bắt buộc | Mô tả                 |
| ----------------- | -------- | -------- | --------------------- |
| `currentPassword` | `string` | có       | mật khẩu hiện tại     |
| `newPassword`     | `string` | có       | mật khẩu mới          |
| `confirmPassword` | `string` | có       | xác nhận mật khẩu mới |

### UpdateUserEmailRequest

| Field      | Type     | Bắt buộc | Mô tả                             |
| ---------- | -------- | -------- | --------------------------------- |
| `newEmail` | `string` | có       | email mới, chỉ nhận gmail/yopmail |

### UpdateUserForAdminRequest

| Field         | Type       | Bắt buộc | Mô tả                       |
| ------------- | ---------- | -------- | --------------------------- |
| `firstName`   | `string`   | không    | 2..50 ký tự                 |
| `lastName`    | `string`   | không    | 2..50 ký tự                 |
| `email`       | `string`   | không    | email hợp lệ                |
| `phoneNumber` | `string`   | không    | số điện thoại VN            |
| `nickname`    | `string`   | không    | nickname                    |
| `avatarUrl`   | `string`   | không    | URL avatar                  |
| `biography`   | `string`   | không    | tiểu sử                     |
| `city`        | `string`   | không    | thành phố                   |
| `nationality` | `string`   | không    | quốc tịch                   |
| `isVerified`  | `boolean`  | không    | trạng thái xác thực         |
| `roleIds`     | `string[]` | không    | danh sách role cần add thêm |

### DeleteRoleFromUserRequest

| Field     | Type       | Bắt buộc | Mô tả                 |
| --------- | ---------- | -------- | --------------------- |
| `roleIds` | `string[]` | có       | role cần gỡ khỏi user |

## 4.3. Role/Permission request DTOs

### CreateRoleRequest / UpdateRoleRequest

| Field           | Type       | Bắt buộc ở create | Mô tả                |
| --------------- | ---------- | ----------------- | -------------------- |
| `name`          | `string`   | có                | tên role             |
| `description`   | `string`   | không             | mô tả role           |
| `active`        | `boolean`  | không             | trạng thái active    |
| `permissionIds` | `string[]` | không             | danh sách permission |

### DeletePermissionFromRoleRequest

| Field           | Type       | Bắt buộc | Mô tả             |
| --------------- | ---------- | -------- | ----------------- |
| `permissionIds` | `string[]` | có       | permission cần gỡ |

### CreatePermissionRequest / UpdatePermissionRequest

| Field        | Type     | Bắt buộc ở create | Mô tả                     |
| ------------ | -------- | ----------------- | ------------------------- |
| `name`       | `string` | có                | tên permission            |
| `apiPath`    | `string` | có                | path endpoint             |
| `httpMethod` | `string` | có                | GET/POST/PUT/PATCH/DELETE |
| `module`     | `string` | không             | tên module quản lý        |

### CreateModuleRequest

| Field           | Type       | Bắt buộc | Mô tả                        |
| --------------- | ---------- | -------- | ---------------------------- |
| `moduleName`    | `string`   | có       | tên module mới               |
| `permissionIds` | `string[]` | có       | permission sẽ gắn module này |

## 4.4. Workspace collaboration request DTOs

### CreateWorkspaceRequest / UpdateWorkspaceRequest

| Field  | Type     | Bắt buộc ở create | Mô tả                           |
| ------ | -------- | ----------------- | ------------------------------- |
| `name` | `string` | có                | tên workspace, tối đa 150 ký tự |

### AddWorkspaceMemberRequest

| Field    | Type                      | Bắt buộc | Mô tả                                     |
| -------- | ------------------------- | -------- | ----------------------------------------- |
| `userId` | `string`                  | có       | có thể là UUID hoặc email để resolve user |
| `role`   | `WorkspaceMemberRoleType` | có       | `OWNER` hoặc `MEMBER`; endpoint chỉ owner workspace được gọi |

### UpdateWorkspaceMemberRoleRequest

| Field  | Type                      | Bắt buộc | Mô tả                       |
| ------ | ------------------------- | -------- | --------------------------- |
| `role` | `WorkspaceMemberRoleType` | có       | `OWNER` hoặc `MEMBER`; không được hạ cấp `OWNER` cuối cùng |

### CreateWorkspaceInviteRequest

| Field          | Type                      | Bắt buộc | Mô tả                                |
| -------------- | ------------------------- | -------- | ------------------------------------ |
| `workspaceId`  | `long`                    | có       | workspace cần tạo invite             |
| `roleToAssign` | `WorkspaceMemberRoleType` | không    | `OWNER` hoặc `MEMBER`; mặc định `MEMBER` |
| `maxUses`      | `int`                     | không    | số lượt dùng tối đa                  |
| `expiresAt`    | `datetime`                | không    | thời điểm hết hạn                    |

### JoinByInviteRequest

| Field        | Type     | Bắt buộc | Mô tả  |
| ------------ | -------- | -------- | ------ |
| `inviteCode` | `string` | có       | mã mời |

### CreateWorkspaceTeamRequest / UpdateWorkspaceTeamRequest

| Field         | Type     | Bắt buộc ở create | Mô tả               |
| ------------- | -------- | ----------------- | ------------------- |
| `workspaceId` | `long`   | có                | workspace chứa team |
| `name`        | `string` | có ở create       | tên team            |
| `description` | `string` | không             | mô tả team          |

### AddTeamMemberRequest

| Field    | Type     | Bắt buộc | Mô tả                     |
| -------- | -------- | -------- | ------------------------- |
| `userId` | `string` | có       | UUID user thuộc workspace |

## 4.5. Project / Goal request DTOs

### CreateProjectRequest

| Field           | Type     | Bắt buộc | Mô tả                   |
| --------------- | -------- | -------- | ----------------------- |
| `workspaceId`   | `long`   | có       | workspace chứa project  |
| `name`          | `string` | có       | tên project, tối đa 150 |
| `description`   | `string` | không    | mô tả project           |
| `visibility`    | `ProjectVisibilityType` | không | `PUBLIC` hoặc `PRIVATE`, mặc định `PUBLIC` |
| `managerUserId` | `string` | không    | manager user            |
| `managerTeamId` | `long`   | không    | manager team            |

### UpdateProjectRequest

| Field           | Type                | Bắt buộc | Mô tả                    |
| --------------- | ------------------- | -------- | ------------------------ |
| `name`          | `string`            | không    | tên project              |
| `description`   | `string`            | không    | mô tả                    |
| `status`        | `ProjectStatusType` | không    | trạng thái project       |
| `visibility`    | `ProjectVisibilityType` | không | `PUBLIC` hoặc `PRIVATE` |
| `managerUserId` | `string`            | không    | gán hoặc bỏ manager user |
| `managerTeamId` | `long`              | không    | gán hoặc bỏ manager team |

Ghi chú: `managerUserId` rỗng có ý nghĩa bỏ manager user; `managerTeamId <= 0` có ý nghĩa bỏ manager team.

### UpdateProjectStatusRequest

| Field    | Type                | Bắt buộc | Mô tả          |
| -------- | ------------------- | -------- | -------------- |
| `status` | `ProjectStatusType` | có       | trạng thái mới |

### CreateGoalRequest

| Field             | Type             | Bắt buộc | Mô tả                                    |
| ----------------- | ---------------- | -------- | ---------------------------------------- |
| `projectId`       | `long`           | có       | project chứa goal                        |
| `title`           | `string`         | có       | tiêu đề goal                             |
| `goalType`        | `GoalType`       | có       | `SHORT_TERM`, `MEDIUM_TERM`, `LONG_TERM` |
| `status`          | `GoalStatusType` | không    | mặc định `NOT_STARTED`                   |
| `progressPercent` | `decimal`        | không    | mặc định `0`, range `0..100`             |
| `managerUserId`   | `string`         | không    | manager user                             |
| `managerTeamId`   | `long`           | không    | manager team                             |

### UpdateGoalRequest

| Field             | Type             | Bắt buộc | Mô tả           |
| ----------------- | ---------------- | -------- | --------------- |
| `title`           | `string`         | không    | tiêu đề mới     |
| `goalType`        | `GoalType`       | không    | loại goal       |
| `status`          | `GoalStatusType` | không    | trạng thái goal |
| `progressPercent` | `decimal`        | không    | 0..100          |
| `managerUserId`   | `string`         | không    | manager user    |
| `managerTeamId`   | `long`           | không    | manager team    |

## 4.6. Task request DTOs

### CreateTaskStatusRequest / UpdateTaskStatusRequest

| Field       | Type      | Bắt buộc ở create | Mô tả                  |
| ----------- | --------- | ----------------- | ---------------------- |
| `projectId` | `long`    | có ở create       | project chứa status    |
| `name`      | `string`  | có ở create       | tên cột                |
| `code`      | `string`  | có ở create       | mã cột                 |
| `position`  | `int`     | không             | vị trí cột             |
| `isClosed`  | `boolean` | không             | cột hoàn tất hay không |

### ReorderTaskStatusesRequest

| Field              | Type     | Bắt buộc | Mô tả                            |
| ------------------ | -------- | -------- | -------------------------------- |
| `statusIdsInOrder` | `long[]` | có       | danh sách status theo thứ tự mới |

### CreateTaskTypeRequest

| Field         | Type     | Bắt buộc | Mô tả                  |
| ------------- | -------- | -------- | ---------------------- |
| `workspaceId` | `long`   | có       | workspace chứa project |
| `projectId`   | `long`   | có       | project chứa task type |
| `goalId`      | `long`   | không    | goal liên kết          |
| `name`        | `string` | có       | tên loại task          |
| `description` | `string` | không    | mô tả                  |
| `color`       | `string` | không    | mã màu `#RRGGBB`, mặc định `#3B82F6` |
| `icon`        | `string` | không    | mã icon frontend, mặc định `tag` |

### UpdateTaskTypeRequest

| Field         | Type     | Bắt buộc | Mô tả         |
| ------------- | -------- | -------- | ------------- |
| `name`        | `string` | không    | tên loại task |
| `description` | `string` | không    | mô tả         |
| `goalId`      | `long`   | không    | goal liên kết |
| `clearGoal`   | `boolean` | không   | `true` để bỏ scope goal |
| `color`       | `string` | không    | mã màu `#RRGGBB` |
| `icon`        | `string` | không    | mã icon frontend |

Ghi chú: không được gửi đồng thời `goalId` và `clearGoal=true`. Backend sẽ kiểm `workspaceId` khớp project khi tạo task type.

### SavePomodoroSessionRequest

| Field             | Type       | Bắt buộc | Mô tả                                              |
| ----------------- | ---------- | -------- | -------------------------------------------------- |
| `durationMinutes` | `int`      | có       | số phút focus thực tế, range `1..240`              |
| `startedAt`       | `datetime` | không    | thời điểm bắt đầu; backend tự suy ra nếu bỏ trống  |
| `endedAt`         | `datetime` | không    | thời điểm kết thúc; mặc định thời điểm server nhận |

### CreateTaskRequest

| Field              | Type               | Bắt buộc | Mô tả                                     |
| ------------------ | ------------------ | -------- | ----------------------------------------- |
| `projectId`        | `long`             | có       | project chứa task                         |
| `goalId`           | `long`             | không    | goal liên kết                             |
| `statusId`         | `long`             | có       | cột Kanban hiện tại                       |
| `title`            | `string`           | có       | tiêu đề task                              |
| `description`      | `string`           | không    | mô tả                                     |
| `notesHtml`        | `string`           | không    | ghi chú HTML                              |
| `priority`         | `TaskPriorityType` | có       | `LOW/MEDIUM/HIGH/URGENT`                  |
| `assigneeId`       | `string`           | không    | người phụ trách                           |
| `dueDate`          | `datetime`         | không    | hạn xử lý                                 |
| `estimatedMinutes` | `int`              | không    | mặc định `0`, không âm                    |
| `boardPosition`    | `int`              | không    | vị trí trong cột                          |
| `taskTypeId`       | `long`             | không    | loại task                                 |
| `sourceView`       | `SourceViewType`   | không    | `KANBAN/TODO/CALENDAR`, mặc định `KANBAN` |

### UpdateTaskRequest

| Field              | Type               | Bắt buộc | Mô tả                      |
| ------------------ | ------------------ | -------- | -------------------------- |
| `title`            | `string`           | không    | tiêu đề mới                |
| `description`      | `string`           | không    | mô tả mới                  |
| `notesHtml`        | `string`           | không    | ghi chú HTML               |
| `goalId`           | `long`             | không    | goal mới                   |
| `clearGoal`        | `boolean`          | không    | `true` để bỏ liên kết goal |
| `priority`         | `TaskPriorityType` | không    | ưu tiên mới                |
| `dueDate`          | `datetime`         | không    | deadline mới               |
| `estimatedMinutes` | `int`              | không    | không âm                   |
| `taskTypeId`       | `long`             | không    | loại task mới              |

Ghi chú: không được gửi đồng thời `goalId` và `clearGoal=true`.

### MoveTaskRequest

| Field            | Type   | Bắt buộc | Mô tả                    |
| ---------------- | ------ | -------- | ------------------------ |
| `statusId`       | `long` | có       | status đích              |
| `targetPosition` | `int`  | không    | vị trí trong status đích |

### ReorderTaskRequest

| Field            | Type  | Bắt buộc | Mô tả                     |
| ---------------- | ----- | -------- | ------------------------- |
| `targetPosition` | `int` | có       | vị trí mới trong cùng cột |

### AssignTaskRequest

| Field        | Type     | Bắt buộc | Mô tả                               |
| ------------ | -------- | -------- | ----------------------------------- |
| `assigneeId` | `string` | không    | UUID assignee; bỏ trống để unassign |

### UpdateTaskCompletionRequest

| Field         | Type      | Bắt buộc | Mô tả                                    |
| ------------- | --------- | -------- | ---------------------------------------- |
| `isCompleted` | `boolean` | có       | đánh dấu hoàn thành hoặc chưa hoàn thành |

### UpdateTaskDependenciesRequest

| Field               | Type     | Bắt buộc | Mô tả                                                               |
| ------------------- | -------- | -------- | ------------------------------------------------------------------- |
| `dependencyTaskIds` | `long[]` | không    | danh sách task ID mà task này phụ thuộc vào; gửi `[]` để xóa tất cả |
| `blockerNote`       | `string` | không    | ghi chú về blocker, tối đa 1000 ký tự                               |

Ghi chú: đây là thao tác ghi đè (replace-all) — danh sách gửi lên sẽ trở thành toàn bộ dependency của task sau request.

## 4.7. Project Access request DTOs

### UpsertProjectAccessRequest

| Field         | Type                       | Bắt buộc | Mô tả                                                    |
| ------------- | -------------------------- | -------- | -------------------------------------------------------- |
| `subjectType` | `ProjectAccessSubjectType` | có       | `USER` hoặc `TEAM`                                       |
| `userId`      | `string`                   | tùy loại | UUID user khi `subjectType=USER`                         |
| `teamId`      | `long`                     | tùy loại | id team khi `subjectType=TEAM`                           |
| `role`        | `ProjectAccessRoleType`    | có       | `VIEWER`, `CONTRIBUTOR`, `MANAGER`                       |

Ghi chú: request chỉ được gửi một trong hai khóa `userId` hoặc `teamId` theo đúng `subjectType`.

### UpdateProjectAccessRequest

| Field  | Type                    | Bắt buộc | Mô tả                              |
| ------ | ----------------------- | -------- | ---------------------------------- |
| `role` | `ProjectAccessRoleType` | có       | role mới cho grant hiện có         |

## 4.8. Task schedule / comment request DTOs

### CreateTaskScheduleRequest / UpdateTaskScheduleRequest

| Field            | Type       | Bắt buộc ở create | Mô tả          |
| ---------------- | ---------- | ----------------- | -------------- |
| `taskId`         | `long`     | có ở create       | task chứa lịch |
| `scheduledStart` | `datetime` | có                | bắt đầu        |
| `scheduledEnd`   | `datetime` | có                | kết thúc       |

### CreateTaskCommentRequest

| Field             | Type     | Bắt buộc | Mô tả             |
| ----------------- | -------- | -------- | ----------------- |
| `taskId`          | `long`   | có       | task được comment |
| `parentCommentId` | `long`   | không    | comment cha       |
| `content`         | `string` | có       | nội dung comment  |

### UpdateTaskCommentRequest

| Field     | Type     | Bắt buộc | Mô tả        |
| --------- | -------- | -------- | ------------ |
| `content` | `string` | có       | nội dung mới |

## 4.9. Storage request DTOs

### SingleUploadFileRequest

| Field        | Type             | Bắt buộc | Mô tả              |
| ------------ | ---------------- | -------- | ------------------ |
| `file`       | `multipart file` | có       | file upload        |
| `folderName` | `string`         | không    | mặc định `uploads` |

### MultipleUploadFileRequest

| Field        | Type               | Bắt buộc | Mô tả              |
| ------------ | ------------------ | -------- | ------------------ |
| `files`      | `multipart file[]` | có       | danh sách file     |
| `folderName` | `string`           | không    | mặc định `uploads` |

### MultipleDeleteFileRequest

| Field       | Type       | Bắt buộc | Mô tả                           |
| ----------- | ---------- | -------- | ------------------------------- |
| `filePaths` | `string[]` | có       | danh sách file path/url cần xóa |

### SingleMoveFileRequest

| Field               | Type     | Bắt buộc | Mô tả        |
| ------------------- | -------- | -------- | ------------ |
| `sourceKey`         | `string` | có       | key nguồn    |
| `destinationFolder` | `string` | có       | thư mục đích |

### MultipleMoveFileRequest

| Field               | Type       | Bắt buộc | Mô tả               |
| ------------------- | ---------- | -------- | ------------------- |
| `sourceKeys`        | `string[]` | có       | danh sách key nguồn |
| `destinationFolder` | `string`   | có       | thư mục đích        |

## 5. Chi tiết response DTO

## 5.1. Common response DTOs

### PaginationMeta

| Field           | Type      | Mô tả                       |
| --------------- | --------- | --------------------------- |
| `currentPage`   | `int`     | trang hiện tại, one-indexed |
| `pageSize`      | `int`     | số phần tử mỗi trang        |
| `totalPages`    | `int`     | tổng số trang               |
| `totalElements` | `long`    | tổng số record              |
| `hasNext`       | `boolean` | còn trang sau hay không     |
| `hasPrevious`   | `boolean` | còn trang trước hay không   |

### PaginationResponse<T>

| Field     | Type             | Mô tả                |
| --------- | ---------------- | -------------------- |
| `meta`    | `PaginationMeta` | thông tin phân trang |
| `content` | `T[]`            | danh sách phần tử    |

### UserSummaryResponse

| Field       | Type     | Mô tả     |
| ----------- | -------- | --------- |
| `userId`    | `string` | UUID user |
| `email`     | `string` | email     |
| `firstName` | `string` | tên       |
| `lastName`  | `string` | họ        |

## 5.2. Auth / User response DTOs

### AuthenticationResponse

| Field         | Type                 | Mô tả                    |
| ------------- | -------------------- | ------------------------ |
| `accessToken` | `string`             | JWT access token         |
| `userSecured` | `UserSecureResponse` | thông tin user đăng nhập |

### UserSecureResponse

| Field          | Type                   | Mô tả                   |
| -------------- | ---------------------- | ----------------------- |
| `userId`       | `string`               | UUID                    |
| `email`        | `string`               | email                   |
| `firstName`    | `string`               | tên                     |
| `lastName`     | `string`               | họ                      |
| `nickname`     | `string`               | biệt danh               |
| `phoneNumber`  | `string`               | số điện thoại           |
| `biography`    | `string`               | mô tả cá nhân           |
| `avatarUrl`    | `string`               | avatar                  |
| `city`         | `string`               | thành phố               |
| `nationality`  | `string`               | quốc tịch               |
| `rolesSecured` | `RoleSecureResponse[]` | danh sách role hệ thống |

### UserResponse

Giống `UserSecureResponse` nhưng field role trả về là `roles` kiểu `RoleSecureResponse[]`.

## 5.3. RBAC response DTOs

### PermissionResponse

| Field          | Type     | Mô tả          |
| -------------- | -------- | -------------- |
| `permissionId` | `string` | mã permission  |
| `name`         | `string` | tên permission |
| `apiPath`      | `string` | path API       |
| `httpMethod`   | `string` | method HTTP    |
| `module`       | `string` | module         |

### RoleSecureResponse

| Field         | Type      | Mô tả      |
| ------------- | --------- | ---------- |
| `roleId`      | `string`  | mã role    |
| `name`        | `string`  | tên role   |
| `description` | `string`  | mô tả      |
| `active`      | `boolean` | trạng thái |

### RoleResponse

Giống `RoleSecureResponse`, có thêm:

| Field         | Type                   | Mô tả           |
| ------------- | ---------------------- | --------------- |
| `permissions` | `PermissionResponse[]` | danh sách quyền |

## 5.4. Collaboration response DTOs

### WorkspaceResponse

| Field       | Type                  | Mô tả              |
| ----------- | --------------------- | ------------------ |
| `id`        | `long`                | id workspace       |
| `name`      | `string`              | tên workspace      |
| `owner`     | `UserSummaryResponse` | chủ sở hữu         |
| `createdAt` | `datetime`            | thời gian tạo      |
| `updatedAt` | `datetime`            | thời gian cập nhật |

### WorkspaceMemberResponse

| Field         | Type                      | Mô tả             |
| ------------- | ------------------------- | ----------------- |
| `id`          | `long`                    | id bản ghi member |
| `workspaceId` | `long`                    | workspace         |
| `user`        | `UserSummaryResponse`     | thành viên        |
| `role`        | `WorkspaceMemberRoleType` | vai trò nội bộ    |
| `joinedAt`    | `datetime`                | ngày tham gia     |

### WorkspaceInviteResponse

| Field           | Type                      | Mô tả                  |
| --------------- | ------------------------- | ---------------------- |
| `id`            | `long`                    | id invite              |
| `workspaceId`   | `long`                    | workspace              |
| `workspaceName` | `string`                  | tên workspace          |
| `inviteCode`    | `string`                  | mã invite              |
| `roleToAssign`  | `WorkspaceMemberRoleType` | role sẽ gán khi join   |
| `createdBy`     | `UserSummaryResponse`     | người tạo              |
| `maxUses`       | `int`                     | số lượt tối đa         |
| `usedCount`     | `int`                     | số lượt đã dùng        |
| `expiresAt`     | `datetime`                | hạn dùng               |
| `isActive`      | `boolean`                 | còn hiệu lực hay không |
| `createdAt`     | `datetime`                | thời gian tạo          |

### WorkspaceTeamResponse

| Field         | Type                  | Mô tả               |
| ------------- | --------------------- | ------------------- |
| `id`          | `long`                | id team             |
| `workspaceId` | `long`                | workspace chứa team |
| `name`        | `string`              | tên team            |
| `description` | `string`              | mô tả               |
| `createdBy`   | `UserSummaryResponse` | người tạo           |
| `memberCount` | `int`                 | số thành viên       |
| `createdAt`   | `datetime`            | thời gian tạo       |
| `updatedAt`   | `datetime`            | thời gian cập nhật  |

### WorkspaceTeamMemberResponse

| Field      | Type                  | Mô tả         |
| ---------- | --------------------- | ------------- |
| `id`       | `long`                | id bản ghi    |
| `teamId`   | `long`                | team          |
| `user`     | `UserSummaryResponse` | thành viên    |
| `joinedAt` | `datetime`            | ngày vào team |

## 5.5. Project / Goal / Task response DTOs

### ProjectResponse

| Field             | Type                  | Mô tả                  |
| ----------------- | --------------------- | ---------------------- |
| `id`              | `long`                | id project             |
| `workspaceId`     | `long`                | workspace chứa project |
| `name`            | `string`              | tên project            |
| `description`     | `string`              | mô tả                  |
| `status`          | `ProjectStatusType`   | trạng thái             |
| `visibility`      | `ProjectVisibilityType` | `PUBLIC` hoặc `PRIVATE` |
| `createdBy`       | `UserSummaryResponse` | người tạo              |
| `managerUser`     | `UserSummaryResponse` | manager user           |
| `managerTeamId`   | `long`                | manager team id        |
| `managerTeamName` | `string`              | tên team manager       |
| `createdAt`       | `datetime`            | thời gian tạo          |
| `updatedAt`       | `datetime`            | thời gian cập nhật     |

### GoalResponse

| Field             | Type                  | Mô tả              |
| ----------------- | --------------------- | ------------------ |
| `id`              | `long`                | id goal            |
| `projectId`       | `long`                | project chứa goal  |
| `title`           | `string`              | tiêu đề goal       |
| `goalType`        | `GoalType`            | loại goal          |
| `status`          | `GoalStatusType`      | trạng thái         |
| `progressPercent` | `decimal`             | tiến độ            |
| `createdBy`       | `UserSummaryResponse` | người tạo          |
| `managerUser`     | `UserSummaryResponse` | manager user       |
| `managerTeamId`   | `long`                | id team manager    |
| `managerTeamName` | `string`              | tên team manager   |
| `createdAt`       | `datetime`            | thời gian tạo      |
| `updatedAt`       | `datetime`            | thời gian cập nhật |

### TaskStatusResponse

| Field       | Type       | Mô tả                  |
| ----------- | ---------- | ---------------------- |
| `id`        | `long`     | id status              |
| `projectId` | `long`     | project chứa status    |
| `name`      | `string`   | tên cột                |
| `code`      | `string`   | mã cột                 |
| `position`  | `int`      | vị trí                 |
| `isClosed`  | `boolean`  | cột hoàn tất hay không |
| `createdAt` | `datetime` | thời gian tạo          |

### TaskTypeResponse

| Field         | Type       | Mô tả              |
| ------------- | ---------- | ------------------ |
| `id`          | `long`     | id task type       |
| `workspaceId` | `long`     | workspace          |
| `projectId`   | `long`     | project            |
| `goalId`      | `long`     | goal liên kết      |
| `name`        | `string`   | tên                |
| `description` | `string`   | mô tả              |
| `color`       | `string`   | màu                |
| `icon`        | `string`   | icon               |
| `createdAt`   | `datetime` | thời gian tạo      |
| `updatedAt`   | `datetime` | thời gian cập nhật |

### TaskResponse

| Field                | Type                  | Mô tả                                         |
| -------------------- | --------------------- | --------------------------------------------- |
| `id`                 | `long`                | id task                                       |
| `workspaceId`        | `long`                | workspace chứa project của task               |
| `projectId`          | `long`                | project chứa task                             |
| `goalId`             | `long`                | goal hiện tại                                 |
| `status`             | `TaskStatusResponse`  | cột hiện tại                                  |
| `title`              | `string`              | tiêu đề task                                  |
| `description`        | `string`              | mô tả                                         |
| `notesHtml`          | `string`              | ghi chú HTML                                  |
| `priority`           | `TaskPriorityType`    | mức ưu tiên                                   |
| `taskType`           | `TaskTypeResponse`    | loại task                                     |
| `sourceView`         | `SourceViewType`      | nguồn view tạo task                           |
| `assignee`           | `UserSummaryResponse` | người phụ trách                               |
| `createdBy`          | `UserSummaryResponse` | người tạo                                     |
| `dueDate`            | `datetime`            | deadline                                      |
| `estimatedMinutes`   | `int`                 | thời lượng ước tính                           |
| `boardPosition`      | `int`                 | vị trí trong cột                              |
| `blockerNote`        | `string`              | ghi chú về blocker (nullable)                 |
| `blocked`            | `boolean`             | task đang bị blocked bởi dependency chưa xong |
| `blockedReason`      | `string`              | mô tả lý do blocked (nullable)                |
| `blockedByOpenCount` | `int`                 | số dependency chưa hoàn thành                 |
| `blockingTaskCount`  | `int`                 | số task khác đang phụ thuộc vào task này      |
| `isCompleted`        | `boolean`             | trạng thái hoàn thành                         |
| `completedAt`        | `datetime`            | thời gian hoàn thành                          |
| `createdAt`          | `datetime`            | thời gian tạo                                 |
| `updatedAt`          | `datetime`            | thời gian cập nhật                            |

Ghi chú: `lastOpenStatus`, `importanceLevel`, `urgencyLevel` tồn tại ở entity nhưng chưa được expose trong response DTO.

### TaskScheduleResponse

| Field            | Type                  | Mô tả              |
| ---------------- | --------------------- | ------------------ |
| `id`             | `long`                | id lịch            |
| `taskId`         | `long`                | task chứa lịch     |
| `scheduledStart` | `datetime`            | bắt đầu            |
| `scheduledEnd`   | `datetime`            | kết thúc           |
| `scheduledDate`  | `date`                | ngày diễn ra       |
| `createdBy`      | `UserSummaryResponse` | người tạo          |
| `createdAt`      | `datetime`            | thời gian tạo      |
| `updatedAt`      | `datetime`            | thời gian cập nhật |

### TaskCommentResponse

| Field             | Type                  | Mô tả              |
| ----------------- | --------------------- | ------------------ |
| `id`              | `long`                | id comment         |
| `taskId`          | `long`                | task               |
| `parentCommentId` | `long`                | comment cha        |
| `user`            | `UserSummaryResponse` | tác giả            |
| `content`         | `string`              | nội dung           |
| `createdAt`       | `datetime`            | thời gian tạo      |
| `updatedAt`       | `datetime`            | thời gian cập nhật |

### TaskDependencyTaskResponse

| Field        | Type               | Mô tả                    |
| ------------ | ------------------ | ------------------------ |
| `id`         | `long`             | id task                  |
| `projectId`  | `long`             | project chứa task        |
| `goalId`     | `long`             | goal liên kết (nullable) |
| `title`      | `string`           | tiêu đề task             |
| `statusName` | `string`           | tên cột hiện tại         |
| `statusCode` | `string`           | mã cột hiện tại          |
| `priority`   | `TaskPriorityType` | mức ưu tiên              |
| `dueDate`    | `datetime`         | deadline                 |
| `completed`  | `boolean`          | đã hoàn thành hay chưa   |

### TaskDependencyDetailsResponse

| Field                | Type                           | Mô tả                                      |
| -------------------- | ------------------------------ | ------------------------------------------ |
| `taskId`             | `long`                         | id task được truy vấn                      |
| `blockerNote`        | `string`                       | ghi chú về blocker (nullable)              |
| `blocked`            | `boolean`                      | có đang bị block hay không                 |
| `blockedReason`      | `string`                       | mô tả lý do blocked (nullable)             |
| `blockedByOpenCount` | `int`                          | số dependency chưa hoàn thành              |
| `blockingTaskCount`  | `int`                          | số task đang phụ thuộc vào task này        |
| `blockedByTasks`     | `TaskDependencyTaskResponse[]` | danh sách task mà task này phụ thuộc vào   |
| `blockingTasks`      | `TaskDependencyTaskResponse[]` | danh sách task đang phụ thuộc vào task này |

### MyWorkScheduleItemResponse

| Field            | Type           | Mô tả                            |
| ---------------- | -------------- | -------------------------------- |
| `scheduleId`     | `long`         | id lịch                          |
| `taskId`         | `long`         | task chứa lịch                   |
| `scheduledStart` | `datetime`     | bắt đầu                          |
| `scheduledEnd`   | `datetime`     | kết thúc                         |
| `task`           | `TaskResponse` | chi tiết task (có `workspaceId`) |

### MyWorkResponse

| Field                    | Type                           | Mô tả                                    |
| ------------------------ | ------------------------------ | ---------------------------------------- |
| `assignedCount`          | `int`                          | số task đang được assign cho user        |
| `blockedCount`           | `int`                          | số task đang bị blocked                  |
| `overdueCount`           | `int`                          | số task quá hạn mà chưa hoàn thành       |
| `dueTodayCount`          | `int`                          | số task đến hạn hôm nay                  |
| `highPriorityCount`      | `int`                          | số task HIGH hoặc URGENT chưa hoàn thành |
| `upcomingScheduledCount` | `int`                          | số lịch trong 7 ngày tới                 |
| `assignedTasks`          | `TaskResponse[]`               | danh sách tất cả task được assign        |
| `upcomingSchedules`      | `MyWorkScheduleItemResponse[]` | lịch sắp tới (tối đa 20 mục)             |
| `generatedAt`            | `datetime`                     | thời điểm snapshot được tạo              |

## 5.6. Project Access / Analytics / Pomodoro response DTOs

### ProjectAccessResponse

| Field       | Type                    | Mô tả                                 |
| ----------- | ----------------------- | ------------------------------------- |
| `id`        | `long`                  | id grant                              |
| `projectId` | `long`                  | project được cấp quyền                |
| `subjectType` | `ProjectAccessSubjectType` | loại subject: `USER` hoặc `TEAM` |
| `user`      | `UserSummaryResponse`   | user được cấp quyền, nullable         |
| `team`      | `WorkspaceTeamResponse` | team được cấp quyền, nullable         |
| `role`      | `ProjectAccessRoleType` | `VIEWER`, `CONTRIBUTOR`, `MANAGER`    |
| `grantedBy` | `UserSummaryResponse`   | người cấp quyền                       |
| `createdAt` | `datetime`              | thời gian tạo                         |
| `updatedAt` | `datetime`              | thời gian cập nhật                    |

### EffectiveProjectAccessResponse

| Field                         | Type                         | Mô tả                                      |
| ----------------------------- | ---------------------------- | ------------------------------------------ |
| `projectId`                   | `long`                       | project đang kiểm tra                      |
| `workspaceId`                 | `long`                       | workspace chứa project                     |
| `visibility`                  | `ProjectVisibilityType`      | `PUBLIC` hoặc `PRIVATE`                    |
| `effectiveRole`               | `EffectiveProjectAccessRoleType` | role hiệu lực sau khi gộp owner/direct/team/default |
| `workspaceOwner`              | `boolean`                    | user hiện tại là owner workspace hay không |
| `canViewProject`              | `boolean`                    | xem project                                |
| `canContribute`               | `boolean`                    | tạo/sửa công việc cơ bản                   |
| `canComment`                  | `boolean`                    | bình luận task                             |
| `canManageProjectWork`        | `boolean`                    | quản lý công việc ở mức project manager    |
| `canManageProjectAccess`      | `boolean`                    | quản lý grant của project                  |
| `canGrantManager`             | `boolean`                    | cấp role `MANAGER`                         |
| `canRevokeManager`            | `boolean`                    | thu hồi role `MANAGER`                     |
| `canManageManagerAccess`      | `boolean`                    | quyền tổng hợp cho grant manager           |
| `canChangeVisibility`         | `boolean`                    | đổi `PUBLIC/PRIVATE`                       |
| `canDeleteProject`            | `boolean`                    | xóa project                                |
| `canAssignOthers`             | `boolean`                    | assign task cho người khác                 |
| `canManageWorkspaceMembers`   | `boolean`                    | quản lý member workspace                   |
| `canManageWorkspaceTeams`     | `boolean`                    | quản lý team workspace                     |
| `canManageWorkspaceInvites`   | `boolean`                    | quản lý invite workspace                   |
| `canManageWorkspaceSettings`  | `boolean`                    | quản lý cấu hình workspace                 |

### ProjectAnalyticsResponse

| Field            | Type                | Mô tả                                    |
| ---------------- | ------------------- | ---------------------------------------- |
| `trend`          | `DailyTrendPoint[]` | số task tạo/hoàn tất theo ngày           |
| `completionRate` | `decimal`           | tỷ lệ hoàn thành                         |
| `totalTasks`     | `long`              | tổng task trong project                  |
| `completedTasks` | `long`              | số task đã hoàn thành                    |

`DailyTrendPoint` của project gồm `date`, `created`, `completed`, `cumulative`.

### TaskAnalyticsResponse

| Field                | Type                    | Mô tả                                |
| -------------------- | ----------------------- | ------------------------------------ |
| `trend`              | `DailyTrendPoint[]`     | task cá nhân tạo/hoàn tất theo ngày  |
| `estimatedByPriority` | `PriorityEstimatePoint[]` | tổng estimate theo mức ưu tiên    |
| `totalAssigned`      | `long`                  | tổng task đang gán cho user hiện tại |
| `totalCompleted`     | `long`                  | tổng task đã hoàn thành              |

`DailyTrendPoint` của task gồm `date`, `created`, `completed`. `PriorityEstimatePoint` gồm `priority`, `totalMinutes`, `taskCount`.

### PomodoroSessionResponse

| Field             | Type       | Mô tả                         |
| ----------------- | ---------- | ----------------------------- |
| `id`              | `long`     | id phiên Pomodoro             |
| `taskId`          | `long`     | task liên quan                |
| `user`            | `UserSummaryResponse` | user hoàn tất phiên |
| `durationMinutes` | `int`      | số phút focus                 |
| `startedAt`       | `datetime` | thời điểm bắt đầu             |
| `endedAt`         | `datetime` | thời điểm kết thúc            |
| `createdAt`       | `datetime` | thời điểm lưu phiên           |

## 5.7. Notification / Activity / Storage response DTOs

### NotificationResponse

| Field           | Type               | Mô tả                     |
| --------------- | ------------------ | ------------------------- |
| `id`            | `long`             | id notification           |
| `type`          | `NotificationType` | loại notification         |
| `title`         | `string`           | tiêu đề                   |
| `message`       | `string`           | nội dung                  |
| `referenceType` | `ReferenceType`    | loại tài nguyên liên quan |
| `referenceId`   | `long`             | id tài nguyên liên quan   |
| `isRead`        | `boolean`          | đã đọc hay chưa           |
| `createdAt`     | `datetime`         | thời gian tạo             |

### NotificationUnreadCountResponse

| Field         | Type   | Mô tả                 |
| ------------- | ------ | --------------------- |
| `unreadCount` | `long` | số thông báo chưa đọc |

### ActivityLogResponse

| Field         | Type                  | Mô tả           |
| ------------- | --------------------- | --------------- |
| `id`          | `long`                | id log          |
| `workspaceId` | `long`                | workspace       |
| `actor`       | `UserSummaryResponse` | người thao tác  |
| `actionType`  | `ActivityActionType`  | hành động       |
| `targetType`  | `ActivityTargetType`  | loại tài nguyên |
| `targetId`    | `long`                | id tài nguyên   |
| `description` | `string`              | mô tả log       |
| `createdAt`   | `datetime`            | thời gian tạo   |

### SingleFileResponse

| Field      | Type     | Mô tả         |
| ---------- | -------- | ------------- |
| `fileName` | `string` | tên file      |
| `fileUrl`  | `string` | URL đã upload |

### MultipleFileResponse

| Field   | Type                   | Mô tả          |
| ------- | ---------------------- | -------------- |
| `files` | `SingleFileResponse[]` | danh sách file |

## 6. Enum tham chiếu nhanh

### WorkspaceMemberRoleType

- `OWNER`
- `MEMBER`

### ProjectVisibilityType

- `PUBLIC`
- `PRIVATE`

### ProjectAccessSubjectType

- `USER`
- `TEAM`

### ProjectAccessRoleType

- `VIEWER`
- `CONTRIBUTOR`
- `MANAGER`

### EffectiveProjectAccessRoleType

- `NO_ACCESS`
- `VIEWER`
- `CONTRIBUTOR`
- `MANAGER`

### ProjectStatusType

- `ACTIVE`
- `COMPLETED`
- `ARCHIVED`

### GoalType

- `SHORT_TERM`
- `MEDIUM_TERM`
- `LONG_TERM`

### GoalStatusType

- `NOT_STARTED`
- `IN_PROGRESS`
- `COMPLETED`
- `ON_HOLD`

### TaskPriorityType

- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

### SourceViewType

- `KANBAN`
- `TODO`
- `CALENDAR`

### NotificationType

- `TASK_ASSIGNED`
- `TASK_COMMENTED`
- `TASK_RESCHEDULED`
- `TASK_STATUS_CHANGED`
- `GOAL_UPDATED`
- `WORKSPACE_MEMBER_ADDED`
- `WORKSPACE_MEMBER_REMOVED`
- `TASK_CREATED`
- `TASK_UPDATED`
- `WORKSPACE_INVITE_USED`

## 7. Payload mẫu kiểu Postman

## 7.1. Đăng ký tài khoản

**POST** `/api/v1/auth/register`

```json
{
    "email": "student@gmail.com",
    "phoneNumber": "0912345678",
    "password": "Password@123",
    "confirmPassword": "Password@123",
    "firstName": "Nguyen",
    "lastName": "An"
}
```

Response:

```json
{
    "success": true,
    "message": "Vui lòng kiểm tra email để xác thực tài khoản",
    "meta": {
        "timestamp": "2026-04-11T10:00:00",
        "instance": "/api/v1/auth/register"
    }
}
```

## 7.2. Đăng nhập

**POST** `/api/v1/auth/login`

```json
{
    "email": "student@gmail.com",
    "password": "Password@123"
}
```

Response data mẫu:

```json
{
    "accessToken": "<jwt-access-token>",
    "userSecured": {
        "userId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2",
        "email": "student@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An",
        "rolesSecured": [
            {
                "roleId": "admin-role-id",
                "name": "ADMIN",
                "description": "Quản trị viên",
                "active": true
            }
        ]
    }
}
```

## 7.3. Tạo workspace

**POST** `/api/v1/workspaces`

```json
{
    "name": "Chronelis Product Team"
}
```

Response data mẫu:

```json
{
    "id": 12,
    "name": "Chronelis Product Team",
    "owner": {
        "userId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2",
        "email": "student@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
    },
    "createdAt": "2026-04-11T10:05:00",
    "updatedAt": "2026-04-11T10:05:00"
}
```

## 7.4. Thêm thành viên vào workspace

**POST** `/api/v1/workspaces/12/members`

```json
{
    "userId": "member@gmail.com",
    "role": "MEMBER"
}
```

## 7.5. Tạo invite workspace

**POST** `/api/v1/workspace-invites`

```json
{
    "workspaceId": 12,
    "roleToAssign": "MEMBER",
    "maxUses": 5,
    "expiresAt": "2026-05-01T23:59:59"
}
```

## 7.6. Tạo project

**POST** `/api/v1/projects`

```json
{
    "workspaceId": 12,
    "name": "Chronelis Frontend Revamp",
    "description": "Thiết kế lại các màn hình collaboration",
    "managerUserId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2"
}
```

## 7.7. Tạo goal

**POST** `/api/v1/goals`

```json
{
    "projectId": 21,
    "title": "Hoàn thiện tài liệu kỹ thuật và quy trình nghiệp vụ",
    "goalType": "SHORT_TERM",
    "status": "IN_PROGRESS",
    "progressPercent": 35,
    "managerUserId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2"
}
```

## 7.8. Tạo task

**POST** `/api/v1/tasks`

```json
{
    "projectId": 21,
    "goalId": 8,
    "statusId": 33,
    "title": "Soạn API_DESCRIPTION.md",
    "description": "Viết lại tài liệu API theo style Postman",
    "priority": "HIGH",
    "assigneeId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2",
    "estimatedMinutes": 180,
    "sourceView": "TODO"
}
```

## 7.9. Bỏ liên kết goal khỏi task

**PATCH** `/api/v1/tasks/101`

```json
{
    "clearGoal": true
}
```

## 7.10. Đánh dấu hoàn thành task

**PATCH** `/api/v1/tasks/101/completion`

```json
{
    "isCompleted": true
}
```

Ghi chú: backend có thể tự chuyển task sang cột closed đầu tiên của project.

## 7.11. Tạo lịch task

**POST** `/api/v1/task-schedules`

```json
{
    "taskId": 101,
    "scheduledStart": "2026-04-12T08:00:00",
    "scheduledEnd": "2026-04-12T10:00:00"
}
```

## 7.12. Thêm bình luận task

**POST** `/api/v1/task-comments`

```json
{
    "taskId": 101,
    "content": "Đã hoàn thiện phần auth và workspace, đang bổ sung tài liệu API."
}
```

## 7.13. Upload ảnh cho task notes

**POST** `/api/v1/storage/azure-blob/upload/single`

Form-data:

- `file`: chọn file ảnh
- `folderName`: `task-notes/101`

Response data mẫu:

```json
{
    "fileName": "diagram.png",
    "fileUrl": "https://<account>.blob.core.windows.net/<container>/task-notes/101/diagram.png"
}
```

## 7.14. Response mẫu cho API có phân trang

```json
{
    "success": true,
    "message": "Lấy danh sách task theo project thành công",
    "data": {
        "meta": {
            "currentPage": 1,
            "pageSize": 10,
            "totalPages": 3,
            "totalElements": 21,
            "hasNext": true,
            "hasPrevious": false
        },
        "content": [
            {
                "id": 101,
                "projectId": 21,
                "goalId": 8,
                "title": "Soạn API_DESCRIPTION.md",
                "priority": "HIGH",
                "sourceView": "TODO",
                "estimatedMinutes": 180,
                "isCompleted": false
            }
        ]
    },
    "meta": {
        "timestamp": "2026-04-11T10:15:00",
        "instance": "/api/v1/tasks/project/21?page=1&size=10"
    }
}
```

## 8. Ghi chú nghiệp vụ quan trọng khi dùng API

1. Hầu hết API ngoài nhóm auth đều cần cả JWT hợp lệ và permission mapping hợp lệ trong DB.
2. `OWNER` không thể được gán qua invite hoặc thêm member trực tiếp.
3. Chỉnh manager của project/goal yêu cầu owner workspace.
4. `AddWorkspaceMemberRequest.userId` có thể là email để service resolve user.
5. `clearGoal=true` là contract chính thức để bỏ liên kết goal khỏi task.
6. Tạo project sẽ tự sinh 3 cột mặc định: `To do`, `In Progress`, `Done`.
7. Mark complete/incomplete có thể làm thay đổi `status` của task.
8. Notes task lưu ở `notesHtml`, không có bảng notes riêng.
9. Endpoint storage upload nhận `multipart/form-data`.
10. `UNAUTHORIZED_ACCESS` hiện đang map ra HTTP 401 trong backend.

## 9. Kết luận

`API_DESCRIPTION.md` này nên được xem là tài liệu API chính thức ở mức mô tả kỹ thuật cho Chronelis trong giai đoạn hiện tại. Khi code có thay đổi về controller, DTO hoặc rule nghiệp vụ, file này cần được cập nhật cùng lúc để giữ đúng vai trò là nguồn tham chiếu cho frontend, kiểm thử và viết báo cáo.
