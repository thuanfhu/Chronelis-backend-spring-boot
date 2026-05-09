# 07. Frontend Sử Dụng API Như Thế Nào

## 1. Mục đích

File này mô tả cách frontend Chronelis đang map màn hình và feature với backend API. Tài liệu này hữu ích khi:

- viết phần phương pháp triển khai FE/BE trong báo cáo
- kiểm tra frontend đang gọi API nào
- đối chiếu màn hình với controller backend

## 2. Router frontend và nhóm màn hình chính

### 2.1. Public routes

- `/login`
- `/register`
- `/forgot-password`
- `/reset-password`
- `/auth/reset-password`
- `/verify-account`
- `/auth/verify-active-account`

### 2.2. Protected routes

- `/dashboard`
- `/workspaces`
- `/workspaces/:workspaceId`
- `/workspaces/:workspaceId/projects/:projectId`
- `/workspaces/:workspaceId/projects/:projectId/goals/:goalId/tasks`
- `/workspaces/:workspaceId/projects/:projectId/pomodoro/:taskId`
- `/workspaces/:workspaceId/projects/:projectId/tasks/:taskId/notes`
- `/workspaces/:workspaceId/projects/:projectId/focus/:taskId`
- `/my-work`
- `/notifications`
- `/profile`
- `/join`

### 2.3. Admin routes

- `/admin/users`
- `/admin/roles`
- `/admin/permissions`

## 3. Mapping màn hình với API backend

| Màn hình / route                   | API backend chính                                                                                                                                                      | Ghi chú                                           |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| Login/Register/Forgot/Reset/Verify | `/auth/*`, `/users/verify-change-email`                                                                                                                                | dùng access token + refresh cookie                |
| Dashboard                          | `GET /workspaces`, `GET /notifications/unread-count`, `GET /tasks/my-work`                                                                                             | workspace gần đây, thông báo, task đang phụ trách |
| Workspaces page                    | `GET /workspaces`, `POST /workspaces`                                                                                                                                  | tạo và liệt kê workspace                          |
| Workspace detail                   | `GET /workspaces/{id}`, `GET /workspaces/{id}/members`, `GET /projects/workspace/{id}`, `GET /workspace-invites/workspace/{id}`, `GET /workspace-teams/workspace/{id}` | là màn hình tổng hợp collaboration                |
| Join by invite                     | `GET /workspace-invites/validate/{code}`, `POST /workspace-invites/join`                                                                                               | tham gia workspace qua code                       |
| My Work (execution hub)            | `GET /tasks/my-work`                                                                                                                                                   | tất cả task được assign, blocked, sắp tới         |
| Tasks layout                       | `GET /projects/{id}`, `GET /tasks/project/{id}`, `GET /task-statuses/project/{id}`, `GET /goals/project/{id}`, `GET /activity-logs/workspace/{id}`                     | chia theo tab calendar/kanban/todo/goals/activity |
| Goal tasks page                    | `GET /goals/{goalId}`, `GET /tasks/goal/{goalId}`                                                                                                                      | màn hình task của một goal riêng biệt             |
| Calendar page                      | `GET /task-schedules/calendar/project/{projectId}` hoặc workspace calendar                                                                                             | cần `fromDate`, `toDate`                          |
| Task drawer / task detail          | `GET /tasks/{taskId}`, `PATCH /tasks/{taskId}`, `GET /tasks/{taskId}/dependencies`, `PUT /tasks/{taskId}/dependencies`, comment/schedule liên quan                     | sửa task, dependency và blocker note              |
| Focus Mode page                    | `GET /tasks/{taskId}`, `GET /tasks/{taskId}/dependencies`, `GET /task-comments/task/{taskId}`, `GET /task-schedules/task/{taskId}`, `PATCH /tasks/{taskId}/completion` | màn hình execution đơn task                       |
| Task notes page                    | `GET /tasks/{taskId}`, `PATCH /tasks/{taskId}`, storage upload                                                                                                         | notes lưu vào `notesHtml`                         |
| Task pomodoro page                 | `GET /tasks/{taskId}`                                                                                                                                                  | timer chủ yếu ở frontend, dữ liệu task lấy từ API |
| Notifications page                 | `GET /notifications`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`                                                                                | có realtime unread count                          |
| Profile page                       | `PATCH /users/update-profile`, `PUT /users/update-password`, `PUT /users/update-email`                                                                                 | profile cá nhân                                   |
| Admin users                        | `GET /users`, `PATCH /users/{userId}`, `DELETE /users/{userId}`, `DELETE /users/{userId}/roles`                                                                        | quản trị tài khoản                                |
| Admin roles                        | `GET /roles`, `POST /roles`, `PATCH /roles/{roleId}`, `DELETE /roles/{roleId}`                                                                                         | quản trị role                                     |
| Admin permissions                  | `GET /permissions`, `POST /permissions`, `PATCH /permissions/{permissionId}`, `DELETE /permissions/{permissionId}`, `/permissions/module*`                             | quản trị permission và module                     |

## 4. Cách frontend lấy dữ liệu

Frontend dùng `TanStack Query` để:

- cache response theo `queryKey`
- invalidate query khi mutation thành công
- hydrate UI từ dữ liệu API hiện tại
- giảm request lặp lại

Các nhóm queryKey lớn đã được tách sẵn như:

- auth
- workspaces
- projects
- goals
- tasks (bao gồm `tasks.myWork`, `tasks.dependencies(taskId)`)
- notifications
- invites
- teams
- admin

## 5. Cách frontend xử lý xác thực

1. Sau login, frontend giữ access token trong auth store.
2. Refresh token nằm ở cookie HttpOnly nên frontend không tự đọc trực tiếp.
3. Khi gặp `401`, frontend có thể clear session và chuyển về `/login`.
4. Khi gặp `403`, frontend chuyển sang trang `/forbidden` cho các ngữ cảnh phù hợp.

## 6. Cách frontend dùng realtime

### 6.1. Workspace-level realtime

`workspace-detail-page` dùng `useWorkspaceRealtime(workspaceId)` để đồng bộ thay đổi về members, teams, invites và project summary.

### 6.2. Task-level realtime

Task-related pages và drawer dùng hook domain realtime để nhận các event như:

- task.created
- task.updated
- task.moved
- task.reordered
- task.assigned
- task.completion-updated
- task.deleted
- task-comment.\*
- task-schedule.\*

### 6.3. Notification realtime

Notification được đẩy về channel private theo user để cập nhật:

- danh sách notification mới
- unread count

## 7. Chi tiết theo màn hình nổi bật

### 7.1. `WorkspaceDetailPage`

Màn hình này là nơi frontend gom nhiều API nhất:

- workspace detail
- workspace members
- workspace teams
- workspace invites
- projects của workspace
- mutation tạo project
- mutation thêm member
- mutation tạo invite
- mutation tạo team

Nó phản ánh rất rõ nghiệp vụ owner/admin/member của hệ thống.

### 7.2. `TasksLayout`

Đây là shell của project với các tab:

- `calendar`
- `kanban`
- `todo`
- `goals`
- `activity`

Mỗi tab lại dùng một nhóm API riêng nhưng vẫn chung ngữ cảnh `workspaceId` và `projectId`.

### 7.3. `GoalTasksPage`

Màn này chứng minh frontend không chỉ hiển thị goal dưới dạng card, mà còn xem task theo từng goal riêng biệt với search, filter, sort và pagination. Đây là giá trị tốt để đưa vào phần mô tả sản phẩm kỳ vọng.

### 7.4. `TaskNotesPage`

Màn notes dùng editor HTML và upload ảnh lên storage, sau đó lưu HTML vào `notesHtml`. Đây là điểm khác biệt quan trọng so với các hệ thống chỉ có textarea thô.

### 7.5. `TaskPomodoroPage`

Pomodoro chủ yếu là UX phía frontend, nhưng nó bám chặt vào context của task và `estimatedMinutes`, cho thấy Chronelis không chỉ quản lý việc mà còn hỗ trợ quy trình làm việc cá nhân.

## 8. Quy ước API mà frontend phải nhớ

1. Base URL là `/api/v1`.
2. Pagination của backend là one-indexed, tức `page=1` là trang đầu.
3. Một số API admin hỗ trợ `filter` theo SpringFilter DSL.
4. Response chuẩn luôn là `ApiResponse<T>`.
5. Endpoint storage upload dùng `multipart/form-data`.
6. Endpoint refresh cần cookie `refresh_token`.
7. Nếu muốn bỏ liên kết goal của task phải dùng `clearGoal=true`.

## 9. Kết luận

Frontend Chronelis đang bám khá sát backend. Mỗi màn hình chính đều có API tương ứng rõ ràng, đồng thời sử dụng query cache và realtime để giữ dữ liệu nhất quán. Đây là cơ sở tốt để mô tả trong đề cương rằng đề tài không chỉ có giao diện, mà đã có kiến trúc tích hợp frontend/backend tương đối hoàn chỉnh.
