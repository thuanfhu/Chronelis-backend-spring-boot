# API Postman-Style (Chronelis)

## Common Conventions

| Ký hiệu    | Ý nghĩa                                              |
| ---------- | ---------------------------------------------------- |
| `[PUBLIC]` | Endpoint công khai, không cần `Authorization` header |
| `[AUTH]`   | Cần `Authorization: Bearer <access_token>`           |

**Base URL**: `/api/v1`

**Response wrapper chuẩn**:

```json
{
    "success": true,
    "message": "Mô tả kết quả",
    "data": {},
    "meta": {
        "timestamp": "2026-04-01T08:30:00Z",
        "instance": "/api/v1/..."
    }
}
```

**Response lỗi**:

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
        "timestamp": "2026-04-01T08:30:00Z",
        "instance": "/api/v1/..."
    }
}
```

**Pagination request params**:

```text
?page=1&size=10&sort=createdAt,desc
```

Lưu ý: backend bật `spring.data.web.pageable.one-indexed-parameters=true`, nên trang đầu tiên là `page=1`.

**Filter query (SpringFilter DSL)**:

```text
?filter=name:'ADMIN' and active:true
```

## Security Model

- Public endpoint chỉ nằm trong nhóm Auth:
    - `POST /api/v1/auth/register`
    - `POST /api/v1/auth/verify-active-account`
    - `POST /api/v1/auth/resend-verify`
    - `POST /api/v1/auth/login`
    - `GET /api/v1/auth/refresh` (cần cookie `refresh_token`)
    - `POST /api/v1/auth/forgot-password`
    - `POST /api/v1/auth/reset-password`
- Tất cả endpoint còn lại là `[AUTH]`.
- Ngoài JWT, backend còn check permission động qua `PermissionInterceptor` theo cặp `apiPath + httpMethod` trong DB.

## Auth Flow (Frontend-aligned)

1. Register -> Verify account:
    - `POST /auth/register` gửi email kích hoạt.
    - `POST /auth/verify-active-account` trả `accessToken` + set `refresh_token` cookie.
2. Login / Refresh:
    - `POST /auth/login` trả `accessToken` + set `refresh_token`.
    - `GET /auth/refresh` dùng cookie để cấp lại access token.
3. Forgot / Reset password:
    - `POST /auth/forgot-password` gửi link reset.
    - `POST /auth/reset-password` đặt mật khẩu mới.
4. Change email (đang đăng nhập):
    - `PUT /users/update-email` gửi token xác thực sang email mới.
    - `POST /users/verify-change-email` xác nhận email mới và buộc đăng nhập lại.
5. Change password (đang đăng nhập):
    - `PUT /users/update-password` đổi mật khẩu và buộc đăng nhập lại.

Gợi ý route frontend tương ứng link email:

- `/auth/verify-active-account?token=...`
- `/auth/verify-change-email?token=...`
- `/auth/reset-password?token=...`

---

## Module 1: AUTH (9 APIs)

| API      | Method | URL                                  | Auth       | Mô tả                                  |
| -------- | ------ | ------------------------------------ | ---------- | -------------------------------------- |
| AUTH-001 | POST   | `/api/v1/auth/register`              | `[PUBLIC]` | Đăng ký tài khoản                      |
| AUTH-002 | POST   | `/api/v1/auth/verify-active-account` | `[PUBLIC]` | Xác thực email kích hoạt               |
| AUTH-003 | POST   | `/api/v1/auth/resend-verify`         | `[PUBLIC]` | Gửi lại email xác thực                 |
| AUTH-004 | POST   | `/api/v1/auth/login`                 | `[PUBLIC]` | Đăng nhập bằng email hoặc phone        |
| AUTH-005 | POST   | `/api/v1/auth/logout`                | `[AUTH]`   | Đăng xuất, thu hồi token/cookie        |
| AUTH-006 | GET    | `/api/v1/auth/account`               | `[AUTH]`   | Lấy thông tin user hiện tại            |
| AUTH-007 | GET    | `/api/v1/auth/refresh`               | `[PUBLIC]` | Cấp lại access token từ refresh cookie |
| AUTH-008 | POST   | `/api/v1/auth/forgot-password`       | `[PUBLIC]` | Gửi email reset password               |
| AUTH-009 | POST   | `/api/v1/auth/reset-password`        | `[PUBLIC]` | Đặt lại mật khẩu                       |

### Payload mẫu nhanh

**Register**

```json
{
    "email": "user@gmail.com",
    "phoneNumber": "0912345678",
    "password": "Password@123",
    "confirmPassword": "Password@123",
    "firstName": "Nguyen",
    "lastName": "An"
}
```

**Login (email hoặc phoneNumber)**

```json
{
    "email": "user@gmail.com",
    "password": "Password@123"
}
```

**Reset password**

```json
{
    "token": "<jwt-token-trong-email>",
    "newPassword": "NewPassword@123",
    "confirmPassword": "NewPassword@123"
}
```

---

## Module 2: USERS + RBAC (24 APIs)

### 2.1 Users (10 APIs)

| API      | Method | URL                                 | Auth     | Mô tả                                    |
| -------- | ------ | ----------------------------------- | -------- | ---------------------------------------- |
| USER-001 | PATCH  | `/api/v1/users/update-profile`      | `[AUTH]` | Cập nhật hồ sơ cá nhân                   |
| USER-002 | PUT    | `/api/v1/users/update-password`     | `[AUTH]` | Đổi mật khẩu (logout sau khi đổi)        |
| USER-003 | PUT    | `/api/v1/users/update-email`        | `[AUTH]` | Yêu cầu đổi email (gửi token xác thực)   |
| USER-004 | POST   | `/api/v1/users/verify-change-email` | `[AUTH]` | Xác thực email mới (logout sau khi đổi)  |
| USER-005 | GET    | `/api/v1/users`                     | `[AUTH]` | Danh sách user có phân trang + filter    |
| USER-006 | GET    | `/api/v1/users/{userId}`            | `[AUTH]` | Lấy chi tiết user                        |
| USER-007 | PATCH  | `/api/v1/users/{userId}`            | `[AUTH]` | Admin cập nhật user                      |
| USER-008 | DELETE | `/api/v1/users/{userId}`            | `[AUTH]` | Admin xóa user                           |
| USER-009 | DELETE | `/api/v1/users/{userId}/roles`      | `[AUTH]` | Gỡ role khỏi user                        |
| USER-010 | POST   | `/api/v1/users/staff-requests`      | `[AUTH]` | Endpoint nâng vai trò theo logic service |

Ghi chú behavior:

- `PATCH /users/{userId}` chỉ add thêm role qua `roleIds`, không thay thế toàn bộ role hiện có.
- Muốn gỡ role phải gọi `DELETE /users/{userId}/roles`.

### 2.2 Roles (6 APIs)

| API      | Method | URL                                  | Auth     | Mô tả                            |
| -------- | ------ | ------------------------------------ | -------- | -------------------------------- |
| ROLE-001 | POST   | `/api/v1/roles`                      | `[AUTH]` | Tạo role                         |
| ROLE-002 | GET    | `/api/v1/roles/{roleId}`             | `[AUTH]` | Lấy chi tiết role                |
| ROLE-003 | GET    | `/api/v1/roles`                      | `[AUTH]` | Danh sách role (filter + paging) |
| ROLE-004 | PATCH  | `/api/v1/roles/{roleId}`             | `[AUTH]` | Cập nhật role                    |
| ROLE-005 | DELETE | `/api/v1/roles/{roleId}/permissions` | `[AUTH]` | Gỡ permission khỏi role          |
| ROLE-006 | DELETE | `/api/v1/roles/{roleId}`             | `[AUTH]` | Xóa role                         |

Ghi chú behavior:

- `PATCH /roles/{roleId}` với `permissionIds` là add thêm permission, không replace.
- Muốn gỡ permission phải dùng endpoint `DELETE /roles/{roleId}/permissions`.

### 2.3 Permissions (8 APIs)

| API      | Method | URL                                  | Auth     | Mô tả                                   |
| -------- | ------ | ------------------------------------ | -------- | --------------------------------------- |
| PERM-001 | POST   | `/api/v1/permissions/module`         | `[AUTH]` | Tạo module mới cho danh sách permission |
| PERM-002 | DELETE | `/api/v1/permissions/module/{name}`  | `[AUTH]` | Xóa module (gỡ module khỏi permissions) |
| PERM-003 | GET    | `/api/v1/permissions/modules`        | `[AUTH]` | Lấy danh sách tên module                |
| PERM-004 | POST   | `/api/v1/permissions`                | `[AUTH]` | Tạo permission                          |
| PERM-005 | PATCH  | `/api/v1/permissions/{permissionId}` | `[AUTH]` | Cập nhật permission                     |
| PERM-006 | GET    | `/api/v1/permissions/{permissionId}` | `[AUTH]` | Lấy chi tiết permission                 |
| PERM-007 | GET    | `/api/v1/permissions`                | `[AUTH]` | Danh sách permission (filter + paging)  |
| PERM-008 | DELETE | `/api/v1/permissions/{permissionId}` | `[AUTH]` | Xóa permission                          |

---

## Module 3: Workspace Collaboration (22 APIs)

### 3.1 Workspaces (9 APIs)

| API    | Method | URL                                                      | Auth     | Mô tả                              |
| ------ | ------ | -------------------------------------------------------- | -------- | ---------------------------------- |
| WS-001 | POST   | `/api/v1/workspaces`                                     | `[AUTH]` | Tạo workspace                      |
| WS-002 | PATCH  | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | Cập nhật workspace                 |
| WS-003 | GET    | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | Lấy chi tiết workspace             |
| WS-004 | GET    | `/api/v1/workspaces`                                     | `[AUTH]` | Danh sách workspace nhìn thấy được |
| WS-005 | POST   | `/api/v1/workspaces/{workspaceId}/members`               | `[AUTH]` | Thêm thành viên vào workspace      |
| WS-006 | GET    | `/api/v1/workspaces/{workspaceId}/members`               | `[AUTH]` | Danh sách thành viên workspace     |
| WS-007 | PATCH  | `/api/v1/workspaces/{workspaceId}/members/{userId}/role` | `[AUTH]` | Cập nhật role thành viên           |
| WS-008 | DELETE | `/api/v1/workspaces/{workspaceId}/members/{userId}`      | `[AUTH]` | Xóa thành viên khỏi workspace      |
| WS-009 | DELETE | `/api/v1/workspaces/{workspaceId}`                       | `[AUTH]` | Xóa workspace                      |

Ghi chú:

- `AddWorkspaceMemberRequest.userId` có thể truyền `userId` hoặc email (service hỗ trợ resolve theo email).
- Cập nhật role thành viên không cho phép set `OWNER`.

### 3.2 Workspace Invites (5 APIs)

| API     | Method | URL                                                 | Auth     | Mô tả                           |
| ------- | ------ | --------------------------------------------------- | -------- | ------------------------------- |
| INV-001 | POST   | `/api/v1/workspace-invites`                         | `[AUTH]` | Tạo invite code                 |
| INV-002 | GET    | `/api/v1/workspace-invites/workspace/{workspaceId}` | `[AUTH]` | Danh sách invite đang active    |
| INV-003 | PATCH  | `/api/v1/workspace-invites/{inviteId}/revoke`       | `[AUTH]` | Thu hồi invite                  |
| INV-004 | GET    | `/api/v1/workspace-invites/validate/{inviteCode}`   | `[AUTH]` | Validate invite code            |
| INV-005 | POST   | `/api/v1/workspace-invites/join`                    | `[AUTH]` | Join workspace bằng invite code |

Ghi chú:

- Invite role `OWNER` bị chặn ở service.

### 3.3 Workspace Teams (8 APIs)

| API      | Method | URL                                                 | Auth     | Mô tả                         |
| -------- | ------ | --------------------------------------------------- | -------- | ----------------------------- |
| TEAM-001 | POST   | `/api/v1/workspace-teams`                           | `[AUTH]` | Tạo team                      |
| TEAM-002 | PATCH  | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | Cập nhật team                 |
| TEAM-003 | GET    | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | Lấy chi tiết team             |
| TEAM-004 | GET    | `/api/v1/workspace-teams/workspace/{workspaceId}`   | `[AUTH]` | Danh sách team theo workspace |
| TEAM-005 | DELETE | `/api/v1/workspace-teams/{teamId}`                  | `[AUTH]` | Xóa team                      |
| TEAM-006 | POST   | `/api/v1/workspace-teams/{teamId}/members`          | `[AUTH]` | Thêm member vào team          |
| TEAM-007 | DELETE | `/api/v1/workspace-teams/{teamId}/members/{userId}` | `[AUTH]` | Xóa member khỏi team          |
| TEAM-008 | GET    | `/api/v1/workspace-teams/{teamId}/members`          | `[AUTH]` | Danh sách member trong team   |

---

## Module 4: Project Delivery Execution (37 APIs)

### 4.1 Projects (6 APIs)

| API      | Method | URL                                        | Auth     |
| -------- | ------ | ------------------------------------------ | -------- |
| PROJ-001 | POST   | `/api/v1/projects`                         | `[AUTH]` |
| PROJ-002 | PATCH  | `/api/v1/projects/{projectId}`             | `[AUTH]` |
| PROJ-003 | PATCH  | `/api/v1/projects/{projectId}/status`      | `[AUTH]` |
| PROJ-004 | GET    | `/api/v1/projects/{projectId}`             | `[AUTH]` |
| PROJ-005 | GET    | `/api/v1/projects/workspace/{workspaceId}` | `[AUTH]` |
| PROJ-006 | DELETE | `/api/v1/projects/{projectId}`             | `[AUTH]` |

### 4.2 Goals (5 APIs)

| API      | Method | URL                                 | Auth     |
| -------- | ------ | ----------------------------------- | -------- |
| GOAL-001 | POST   | `/api/v1/goals`                     | `[AUTH]` |
| GOAL-002 | PATCH  | `/api/v1/goals/{goalId}`            | `[AUTH]` |
| GOAL-003 | GET    | `/api/v1/goals/{goalId}`            | `[AUTH]` |
| GOAL-004 | GET    | `/api/v1/goals/project/{projectId}` | `[AUTH]` |
| GOAL-005 | DELETE | `/api/v1/goals/{goalId}`            | `[AUTH]` |

### 4.3 Task Statuses (5 APIs)

| API         | Method | URL                                                 | Auth     |
| ----------- | ------ | --------------------------------------------------- | -------- |
| TSTATUS-001 | POST   | `/api/v1/task-statuses`                             | `[AUTH]` |
| TSTATUS-002 | GET    | `/api/v1/task-statuses/project/{projectId}`         | `[AUTH]` |
| TSTATUS-003 | PATCH  | `/api/v1/task-statuses/{statusId}`                  | `[AUTH]` |
| TSTATUS-004 | PATCH  | `/api/v1/task-statuses/project/{projectId}/reorder` | `[AUTH]` |
| TSTATUS-005 | DELETE | `/api/v1/task-statuses/{statusId}`                  | `[AUTH]` |

### 4.4 Task Types (5 APIs)

| API       | Method | URL                                      | Auth     |
| --------- | ------ | ---------------------------------------- | -------- |
| TTYPE-001 | POST   | `/api/v1/task-types`                     | `[AUTH]` |
| TTYPE-002 | PATCH  | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` |
| TTYPE-003 | GET    | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` |
| TTYPE-004 | GET    | `/api/v1/task-types/project/{projectId}` | `[AUTH]` |
| TTYPE-005 | DELETE | `/api/v1/task-types/{taskTypeId}`        | `[AUTH]` |

### 4.5 Tasks (10 APIs)

| API      | Method | URL                                 | Auth     |
| -------- | ------ | ----------------------------------- | -------- |
| TASK-001 | POST   | `/api/v1/tasks`                     | `[AUTH]` |
| TASK-002 | PATCH  | `/api/v1/tasks/{taskId}`            | `[AUTH]` |
| TASK-003 | GET    | `/api/v1/tasks/{taskId}`            | `[AUTH]` |
| TASK-004 | GET    | `/api/v1/tasks/project/{projectId}` | `[AUTH]` |
| TASK-005 | GET    | `/api/v1/tasks/goal/{goalId}`       | `[AUTH]` |
| TASK-006 | PATCH  | `/api/v1/tasks/{taskId}/move`       | `[AUTH]` |
| TASK-007 | PATCH  | `/api/v1/tasks/{taskId}/reorder`    | `[AUTH]` |
| TASK-008 | PATCH  | `/api/v1/tasks/{taskId}/assignee`   | `[AUTH]` |
| TASK-009 | PATCH  | `/api/v1/tasks/{taskId}/completion` | `[AUTH]` |
| TASK-010 | DELETE | `/api/v1/tasks/{taskId}`            | `[AUTH]` |

### 4.6 Task Schedules (6 APIs)

| API      | Method | URL                                                       | Auth     |
| -------- | ------ | --------------------------------------------------------- | -------- |
| TSCH-001 | POST   | `/api/v1/task-schedules`                                  | `[AUTH]` |
| TSCH-002 | PATCH  | `/api/v1/task-schedules/{scheduleId}`                     | `[AUTH]` |
| TSCH-003 | DELETE | `/api/v1/task-schedules/{scheduleId}`                     | `[AUTH]` |
| TSCH-004 | GET    | `/api/v1/task-schedules/task/{taskId}`                    | `[AUTH]` |
| TSCH-005 | GET    | `/api/v1/task-schedules/calendar/project/{projectId}`     | `[AUTH]` |
| TSCH-006 | GET    | `/api/v1/task-schedules/calendar/workspace/{workspaceId}` | `[AUTH]` |

Calendar query bắt buộc:

- `fromDate=YYYY-MM-DD`
- `toDate=YYYY-MM-DD`
- `page`, `size`, `sort` (tuỳ chọn)

### 4.7 Task Comments (4 APIs)

| API      | Method | URL                                   | Auth     |
| -------- | ------ | ------------------------------------- | -------- |
| TCOM-001 | POST   | `/api/v1/task-comments`               | `[AUTH]` |
| TCOM-002 | PATCH  | `/api/v1/task-comments/{commentId}`   | `[AUTH]` |
| TCOM-003 | DELETE | `/api/v1/task-comments/{commentId}`   | `[AUTH]` |
| TCOM-004 | GET    | `/api/v1/task-comments/task/{taskId}` | `[AUTH]` |

---

## Module 5: Notifications + Activity Logs (5 APIs)

| API      | Method | URL                                             | Auth     | Mô tả                            |
| -------- | ------ | ----------------------------------------------- | -------- | -------------------------------- |
| NOTI-001 | GET    | `/api/v1/notifications`                         | `[AUTH]` | Danh sách thông báo              |
| NOTI-002 | GET    | `/api/v1/notifications/unread-count`            | `[AUTH]` | Số thông báo chưa đọc            |
| NOTI-003 | PATCH  | `/api/v1/notifications/{notificationId}/read`   | `[AUTH]` | Đánh dấu 1 thông báo đã đọc      |
| NOTI-004 | PATCH  | `/api/v1/notifications/read-all`                | `[AUTH]` | Đánh dấu tất cả đã đọc           |
| ACT-001  | GET    | `/api/v1/activity-logs/workspace/{workspaceId}` | `[AUTH]` | Lịch sử hoạt động theo workspace |

Activity log query hỗ trợ:

- `actorId`
- `actionType`
- `targetType`
- `fromDateTime` (ISO date-time)
- `toDateTime` (ISO date-time)
- `page`, `size`, `sort`

---

## Module 6: Storage (AWS S3) (6 APIs)

| API    | Method | URL                                                 | Auth     | Mô tả                |
| ------ | ------ | --------------------------------------------------- | -------- | -------------------- |
| S3-001 | POST   | `/api/v1/storage/aws-s3/upload/single`              | `[AUTH]` | Upload một file      |
| S3-002 | POST   | `/api/v1/storage/aws-s3/upload/multiple`            | `[AUTH]` | Upload nhiều file    |
| S3-003 | DELETE | `/api/v1/storage/aws-s3/delete/single?filePath=...` | `[AUTH]` | Xóa một file         |
| S3-004 | DELETE | `/api/v1/storage/aws-s3/delete/multiple`            | `[AUTH]` | Xóa nhiều file       |
| S3-005 | PUT    | `/api/v1/storage/aws-s3/move/single`                | `[AUTH]` | Di chuyển một file   |
| S3-006 | PUT    | `/api/v1/storage/aws-s3/move/multiple`              | `[AUTH]` | Di chuyển nhiều file |

Upload endpoint nhận `multipart/form-data`.

---

## Request Payload Snapshots (thường dùng)

### Create workspace

```json
{
    "name": "Product Team"
}
```

### Add workspace member

```json
{
    "userId": "b4ce2d9b-fd7a-4630-9a29-9e3fa9d5d6b2",
    "role": "MEMBER"
}
```

`userId` có thể truyền email để service resolve user.

### Create invite

```json
{
    "workspaceId": 12,
    "roleToAssign": "MEMBER",
    "maxUses": 10,
    "expiresAt": "2026-04-30T23:59:59"
}
```

### Create project

```json
{
    "workspaceId": 12,
    "name": "Chronelis Web Revamp",
    "description": "Revamp sprint Q2",
    "managerUserId": "user-id-optional",
    "managerTeamId": 34
}
```

### Create task

```json
{
    "projectId": 101,
    "goalId": 1001,
    "statusId": 5001,
    "title": "Implement admin dashboard",
    "description": "Users / Roles / Permissions tabs",
    "priority": "HIGH",
    "assigneeId": "8ec2...",
    "dueDate": "2026-04-12T18:00:00",
    "estimatedMinutes": 240,
    "taskTypeId": 44,
    "sourceView": "KANBAN"
}
```

### Move task

```json
{
    "statusId": 5002,
    "targetPosition": 3
}
```

### Reorder task statuses

```json
{
    "statusIdsInOrder": [5001, 5002, 5003]
}
```

### Task schedule calendar query

```text
GET /api/v1/task-schedules/calendar/project/101?fromDate=2026-04-01&toDate=2026-04-30&page=1&size=50
```

---

## WebSocket Reference

- Endpoint handshake: `ws://<host>/ws`
- Client gửi: prefix `/server`
- Client subscribe: prefix `/client`
- Broker prefixes: `/public`, `/private`

Channel thực tế:

- Workspace events: `/public/workspaces/{workspaceId}/events`
- Project events: `/public/workspaces/{workspaceId}/projects/{projectId}/events`
- Task events: `/public/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/events`
- User notifications: `/user/{userId}/private/notifications`
- Unread count: `/user/{userId}/private/notifications/unread-count`

## Ghi chú tích hợp Frontend

- Frontend cần gửi `withCredentials=true` để refresh cookie hoạt động.
- Sau khi đổi mật khẩu hoặc xác thực đổi email thành công, backend logout phiên hiện tại; frontend nên clear session và điều hướng về login.
- Role hệ thống mặc định: `ADMIN`, `USER`.
- Nếu gọi endpoint thấy 401/403 dù đã có token, cần kiểm tra mapping permission trong DB (seed `permissions` + `permission_roles`).
