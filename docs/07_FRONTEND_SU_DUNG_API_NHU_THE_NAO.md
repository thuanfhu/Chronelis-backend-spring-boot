# 07. Frontend Sử Dụng API Như Thế Nào

## 1. Mục đích

File này mô tả cách frontend Chronelis đang map màn hình và feature với backend API. Tài liệu này hữu ích khi:

- viết phần phương pháp triển khai FE/BE trong báo cáo
- kiểm tra frontend đang gọi API nào
- đối chiếu màn hình với controller backend
- rà soát nhanh khi backend đổi contract API

## 2. Router frontend và nhóm màn hình chính

### 2.1. Public routes

- `/`
- `/login`
- `/register`
- `/forgot-password`
- `/reset-password`
- `/auth/reset-password`
- `/verify-account`
- `/auth/verify-active-account`

Các route auth public nằm sau `PublicOnlyGuard`. Nếu đã đăng nhập, user sẽ được chuyển về dashboard, trừ các màn token-based như verify/reset.

### 2.2. Protected routes

- `/verify-change-email`
- `/auth/verify-change-email`
- `/dashboard`
- `/my-work`
- `/workspaces`
- `/workspaces/:workspaceId`
- `/workspaces/:workspaceId/projects/:projectId`
- `/workspaces/:workspaceId/projects/:projectId/goals/:goalId/tasks`
- `/workspaces/:workspaceId/projects/:projectId/pomodoro/:taskId`
- `/workspaces/:workspaceId/projects/:projectId/tasks/:taskId/notes`
- `/notifications`
- `/profile`
- `/join`

`/workspaces/:workspaceId/projects/:projectId` render `TasksLayout`. Các view con của project dùng query `?view=calendar|kanban|todo|goals|activity|settings`.

### 2.3. Admin routes

- `/admin`
- `/admin/:section`

`section` hiện dùng cho `users`, `roles`, `permissions`. Nhóm route này đi qua `AdminGuard` và `AdminShell`.

## 3. Mapping màn hình với API backend

| Màn hình / route | API backend chính | Ghi chú |
| ---------------- | ----------------- | ------- |
| Landing page | không bắt buộc API; đọc auth store nếu đã login | trang giới thiệu, CTA sang auth/app |
| Login/Register/Forgot/Reset/Verify | `/auth/register`, `/auth/verify-active-account`, `/auth/resend-verify`, `/auth/login`, `/auth/logout`, `/auth/account`, `/auth/refresh`, `/auth/forgot-password`, `/auth/reset-password` | dùng access token + refresh cookie |
| Verify change email | `/users/verify-change-email` | token đổi email |
| Dashboard | `GET /workspaces`, `GET /notifications/unread-count`, `GET /tasks/my-work` | workspace gần đây, notification, task cá nhân |
| My Work | `GET /tasks/my-work`, `GET /tasks/analytics`, `GET /projects/{id}` theo task | execution hub cá nhân, chart task và danh sách blocked/ready |
| Workspaces page | `GET /workspaces`, `POST /workspaces`, `PATCH /workspaces/{id}`, `DELETE /workspaces/{id}` | tạo/sửa/xóa workspace, có undo delete phía frontend |
| Workspace detail | `GET /workspaces/{id}`, `GET /workspaces/{id}/members`, `GET /projects/workspace/{id}`, `GET /workspace-invites/workspace/{id}`, `GET /workspace-teams/workspace/{id}`, `GET /workspace-teams/{teamId}/members` | màn tổng hợp collaboration |
| Join by invite | `GET /workspace-invites/validate/{code}`, `POST /workspace-invites/join` | tham gia workspace qua code |
| Tasks layout | `GET /projects/{id}`, `GET /projects/{id}/access/me`, `GET /tasks/project/{id}`, `GET /task-statuses/project/{id}`, `GET /goals/project/{id}`, `GET /activity-logs/workspace/{id}` | shell project, đổi view bằng query `view` |
| Kanban page | task/status APIs, `PATCH /tasks/{taskId}/move`, `PATCH /tasks/{taskId}/reorder`, `PATCH /task-statuses/project/{projectId}/reorder` | drag/drop cột và task |
| Todo page | task/status/goal/schedule APIs, `PATCH /tasks/{taskId}/completion` | list task theo ngày/goal/filter |
| Calendar page | `GET /task-schedules/calendar/project/{projectId}`, `POST/PATCH/DELETE /task-schedules`, task APIs | FullCalendar, kéo/thả và resize lịch |
| Goals page | `GET /goals/project/{projectId}`, `POST/PATCH/DELETE /goals`, task project APIs | quản lý goal và task theo goal |
| Goal tasks page | `GET /goals/{goalId}`, `GET /tasks/goal/{goalId}` | màn task của một goal riêng biệt |
| Project settings | `GET/PATCH /projects/{projectId}`, `GET/POST/PATCH/DELETE /task-types`, `/projects/{projectId}/access*` | cấu hình project, CRUD task type, visibility và access grant |
| Task drawer / task detail | `GET /tasks/{taskId}`, `PATCH /tasks/{taskId}`, `GET/PUT /tasks/{taskId}/dependencies`, comment/schedule APIs | sửa task, dependency, schedule, comment |
| Task notes page | `GET /tasks/{taskId}`, `PATCH /tasks/{taskId}`, `POST /storage/azure-blob/upload/single` | rich text notes lưu vào `notesHtml`, ảnh upload Azure Blob và chỉnh size/căn/xóa trong editor |
| Task pomodoro page | `GET /tasks/{taskId}`, `POST /pomodoro/tasks/{taskId}`, `GET /pomodoro/tasks/{taskId}` | timer ở frontend, lưu session hoàn tất của user hiện tại |
| Notifications page | `GET /notifications`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`, `GET /notifications/unread-count` | có realtime unread count |
| Profile page | `PATCH /users/update-profile`, `PUT /users/update-password`, `PUT /users/update-email` | profile cá nhân |
| Admin users | `GET /users`, `GET /users/{userId}`, `PATCH /users/{userId}`, `DELETE /users/{userId}`, `DELETE /users/{userId}/roles`, `POST /users/staff-requests` | quản trị tài khoản |
| Admin roles | `GET /roles`, `POST /roles`, `PATCH /roles/{roleId}`, `DELETE /roles/{roleId}`, `DELETE /roles/{roleId}/permissions` | quản trị role và permission gắn role |
| Admin permissions | `GET /permissions`, `POST /permissions`, `PATCH /permissions/{permissionId}`, `DELETE /permissions/{permissionId}`, `/permissions/module*` | quản trị permission và module |

## 4. Cách frontend lấy dữ liệu

Frontend dùng `TanStack Query` để:

- cache response theo `queryKey`
- invalidate query khi mutation thành công
- hydrate UI từ dữ liệu API hiện tại
- giảm request lặp lại
- làm optimistic update cho các luồng drag/drop, complete task, comment, schedule và undo delete

Các nhóm query key lớn:

- `auth`
- `users`
- `workspaces`
- `projects`
- `projectAccess`
- `goals`
- `tasks`
- `taskStatus`
- `taskTypes`
- `taskSchedules`
- `taskComments`
- `notifications`
- `invites`
- `teams`
- `activityLogs`
- `admin`

## 5. Cách frontend xử lý xác thực

1. Sau login, frontend giữ access token trong `auth-store`.
2. Refresh token nằm ở cookie HttpOnly nên frontend không tự đọc trực tiếp.
3. `http.ts` gắn `Authorization: Bearer <token>` cho request thường.
4. Khi gặp `401`, frontend chỉ clear session nếu token thật sự thiếu hoặc hết hạn.
5. Khi gặp `403`, frontend chuyển sang `/forbidden` trong các ngữ cảnh cần chặn truy cập.
6. `ProtectedGuard`, `PublicOnlyGuard` và `AdminGuard` chịu trách nhiệm bảo vệ route.

## 6. Cách frontend dùng realtime

### 6.1. Workspace-level realtime

`WorkspaceDetailPage` dùng `useWorkspaceRealtime(workspaceId)` để đồng bộ thay đổi về members, teams, invites và project summary.

### 6.2. Project/task-level realtime

Các màn task và drawer dùng hook realtime theo project/task để nhận event:

- task created/updated/deleted
- task moved/reordered
- task assigned/completion-updated
- task-comment created/updated/deleted
- task-schedule created/updated/deleted
- goal/status/type thay đổi theo project

### 6.3. Notification realtime

Notification được đẩy về private channel theo user để cập nhật:

- notification mới
- unread count

## 7. Chi tiết theo màn hình nổi bật

### 7.1. `WorkspaceDetailPage`

Đây là màn frontend gom nhiều API collaboration nhất:

- workspace detail
- workspace members
- workspace teams và team members
- workspace invites
- project list của workspace
- mutation tạo/sửa/xóa project
- mutation thêm/xóa member
- mutation tạo/revoke invite
- mutation tạo/sửa/xóa team và thêm/xóa member khỏi team

Quyền thao tác phụ thuộc vào owner workspace và effective project access từ backend.

### 7.2. `TasksLayout`

Đây là shell của project. Các tab hiện tại:

- `calendar`
- `kanban`
- `todo`
- `goals`
- `activity`
- `settings`

Mỗi tab dùng một nhóm API riêng nhưng chung context `workspaceId`, `projectId` và `useProjectPermissions(projectId)`.

### 7.3. `ProjectSettingsPage`

Màn settings gom ba nhóm logic:

- sửa project name/description/status/visibility
- CRUD task type
- quản lý project access grant cho user/team với role `MANAGER`, `CONTRIBUTOR`, `VIEWER`

Đây là nơi frontend cần bám sát `EffectiveProjectAccessResponse` nhất.

### 7.4. `TaskDetailsDrawer`

Drawer task là component dùng chung toàn app, mở từ Kanban, Todo, Calendar, My Work và các chart/card. Nó gom:

- chi tiết task
- dependency/blocker
- schedule
- comment thread
- optimistic completion
- duplicate/edit/view mode
- delete confirm có undo

### 7.5. `TaskNotesPage`

Màn notes dùng TipTap editor. Nội dung HTML lưu vào `notesHtml`; ảnh paste/drag/drop/upload được đẩy lên Azure Blob rồi chèn URL vào editor. Editor hiện hỗ trợ toolbar kiểu Word cho heading, bold/italic/underline, list, quote, code, màu chữ, highlight, cỡ chữ và thao tác ảnh như zoom, căn trái/giữa/phải, xóa ảnh.

### 7.6. `TaskPomodoroPage`

Pomodoro bám vào task và `estimatedMinutes`. Khi phiên focus hoàn tất, frontend gọi `POST /api/v1/pomodoro/tasks/{taskId}` để lưu `durationMinutes`, `startedAt`, `endedAt`; lịch sử hiển thị bằng `GET /api/v1/pomodoro/tasks/{taskId}`.

## 8. Quy ước API mà frontend phải nhớ

1. Base URL là `/api/v1`.
2. Pagination của backend là one-indexed, tức `page=1` là trang đầu.
3. Response chuẩn luôn là `ApiResponse<T>`.
4. Endpoint storage upload dùng `multipart/form-data`.
5. Storage hiện tại là Azure Blob, không còn dùng AWS S3 endpoint.
6. Endpoint refresh cần cookie `refresh_token`.
7. Nếu muốn bỏ liên kết goal của task phải dùng `clearGoal=true`.
8. Project có `visibility=PUBLIC|PRIVATE`; quyền thao tác chi tiết lấy từ `/projects/{projectId}/access/me`.
9. Workspace member role hiện tại là `OWNER` hoặc `MEMBER`; project-level role mới là nơi phân quyền `MANAGER`, `CONTRIBUTOR`, `VIEWER`.

## 9. Ghi chú đối chiếu code hiện tại

- `ProjectOverviewPage` vẫn tồn tại trong source frontend nhưng chưa được route chính trong `AppRouter`.
- Các link cũ dạng `/focus/:taskId` không còn trong router hiện tại; Pomodoro dùng `/pomodoro/:taskId`.
- `TasksLayout` dùng query `view`, không dùng route con `/kanban`, `/goals`.
- Admin users UI có một số đường tạo user còn phụ thuộc implementation API hiện tại; khi mở rộng staff onboarding cần rà lại `UsersProvider`.

## 10. Kết luận

Frontend Chronelis đang bám sát backend qua API wrapper, query keys, optimistic cache và realtime hooks. Với các module mới như project access, task analytics, project analytics, Azure Blob và Pomodoro session, frontend đã có đủ nền để mô tả hệ thống như một product quản lý công việc cộng tác, không chỉ là giao diện CRUD cơ bản.
