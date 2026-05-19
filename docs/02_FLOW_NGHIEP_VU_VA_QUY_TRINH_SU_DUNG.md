# 02. Flow Nghiệp Vụ Và Quy Trình Sử Dụng

## 1. Flow xác thực và khởi tạo phiên làm việc

### 1.1. Đăng ký và kích hoạt tài khoản

1. Người dùng gửi `POST /api/v1/auth/register`.
2. Backend tạo user ở trạng thái chưa verify.
3. Hệ thống gửi email chứa token kích hoạt.
4. Frontend mở route `/auth/verify-active-account?token=...`.
5. Frontend gọi `POST /api/v1/auth/verify-active-account`.
6. Backend xác thực token, chuyển tài khoản sang trạng thái kích hoạt và trả `accessToken`.

### 1.2. Đăng nhập và refresh phiên

1. Người dùng đăng nhập bằng email hoặc số điện thoại.
2. Backend trả `accessToken` trong body và set `refresh_token` ở cookie HttpOnly.
3. Frontend dùng `GET /api/v1/auth/account` để hydrate thông tin user.
4. Khi access token hết hạn, frontend gọi `GET /api/v1/auth/refresh` bằng cookie.

### 1.3. Quên mật khẩu và đổi thông tin tài khoản

- Quên mật khẩu: `forgot-password` -> email reset -> `reset-password`.
- Đổi email: `PUT /users/update-email` -> email xác minh -> `POST /users/verify-change-email`.
- Đổi mật khẩu: `PUT /users/update-password`.

## 2. Flow tạo và quản lý workspace

### 2.1. Tạo workspace

1. User đã đăng nhập tạo workspace.
2. Backend lưu workspace mới.
3. User tạo workspace tự động được thêm vào bảng `workspace_members` với role `OWNER`.
4. Realtime event `workspace.created` được phát ra.

### 2.2. Quản lý thành viên workspace

1. Owner thêm thành viên bằng `POST /workspaces/{workspaceId}/members`.
2. `userId` có thể là UUID hoặc email, service sẽ resolve người dùng.
3. Role theo enum hiện tại là `OWNER` hoặc `MEMBER`; endpoint này chỉ owner workspace được gọi.
4. Khi thêm thành viên thành công:
    - ghi activity log
    - phát realtime event `workspace.member.added`
    - tạo notification cho người được thêm

### 2.3. Cập nhật role thành viên

1. Chỉ owner mới đổi role được.
2. Có thể đổi giữa `OWNER` và `MEMBER` qua endpoint cập nhật role.
3. Backend không cho hạ cấp `OWNER` cuối cùng của workspace để tránh workspace mất chủ sở hữu.

## 3. Flow team và cộng tác nội bộ trong workspace

### 3.1. Team trong workspace

Team dùng để gom nhiều thành viên nhằm phục vụ phân công manager theo nhóm hoặc điều phối công việc.

### 3.2. Quy trình team

1. Owner tạo team.
2. Owner thêm thành viên workspace vào team.
3. Team có thể được gán làm manager cho project hoặc goal.
4. Thành viên thuộc team manager sẽ có quyền quản lý tương ứng thông qua `CollaborationAccessService`.

### 3.3. Ràng buộc chính

- Tên team không được trùng trong cùng workspace.
- Chỉ thành viên đã thuộc workspace mới được thêm vào team.
- Chỉ owner workspace mới được tạo, sửa, xóa team hoặc thay đổi thành viên team.

## 4. Flow invite code tham gia workspace

### 4.1. Tạo invite

1. Owner tạo invite cho workspace.
2. Invite có thể khai báo:
    - `roleToAssign`
    - `maxUses`
    - `expiresAt`
3. Nếu không truyền role thì mặc định là `MEMBER`.
4. Code hiện tại nhận `OWNER` hoặc `MEMBER` cho `roleToAssign`; nếu không truyền role thì mặc định `MEMBER`.

### 4.2. Validate và join bằng invite

1. Frontend route `/join?code=...` gọi `GET /workspace-invites/validate/{inviteCode}`.
2. Nếu hợp lệ, user xác nhận tham gia.
3. Frontend gọi `POST /workspace-invites/join`.
4. Backend thêm user hiện tại vào workspace theo role đã cấu hình trong invite.
5. Tăng `usedCount`; nếu vượt `maxUses` thì invite tự ngắt hoạt động.

## 5. Flow project trong workspace

### 5.1. Tạo project

1. Chỉ owner workspace được tạo project.
2. Nếu request có `managerUserId` hoặc `managerTeamId`, backend vẫn yêu cầu người tạo là owner workspace.
3. Project mới có `visibility` mặc định là `PUBLIC` nếu request không truyền.
4. Project mới được tạo tự động 3 cột task status mặc định:
    - `To do`
    - `In Progress`
    - `Done`

### 5.2. Quyền quản lý project

Một user được xem là có quyền quản lý project nếu thuộc một trong các trường hợp sau:

- là owner workspace
- có effective project role `MANAGER` từ grant trực tiếp
- thuộc team có project access grant role `MANAGER`
- được gán làm manager user/team của project và backend đã sync assignment đó thành project access grant

## 6. Flow goal trong project

### 6.1. Vai trò của goal

Goal là lớp mục tiêu trung gian giữa project và task. Goal giúp gom task theo định hướng ngắn hạn, trung hạn hoặc dài hạn.

### 6.2. Quy trình goal

1. Người có quyền quản lý project tạo goal.
2. Có thể gán manager user hoặc manager team cho goal.
3. Nếu khai báo manager, backend yêu cầu người thao tác là owner workspace.
4. `progressPercent` được kiểm tra trong khoảng `0..100`.
5. Khi task được thêm vào, chuyển goal hoặc bỏ liên kết goal, service sẽ tính lại tiến độ goal.

## 7. Flow task và điều phối công việc

## 7.1. Tạo task

1. Task luôn thuộc một project và một task status.
2. Goal là tùy chọn.
3. Assignee là tùy chọn nhưng bắt buộc phải thuộc cùng workspace.
4. `sourceView` có thể là `KANBAN`, `TODO`, `CALENDAR`; nếu không truyền thì mặc định `KANBAN`.
5. `estimatedMinutes` mặc định về `0` nếu không truyền.
6. `boardPosition` được backend chuẩn hóa để tránh khoảng trống chỉ số.

### 7.2. Move task giữa các cột

1. Người có quyền quản lý task gọi `PATCH /tasks/{taskId}/move`.
2. Nếu status đích là cột closed thì task tự hoàn thành.
3. Nếu status đích là cột mở thì task bị bỏ trạng thái completed.
4. Hệ thống lưu `lastOpenStatus` để có thể khôi phục khi đánh dấu task chưa hoàn thành sau đó.

### 7.3. Reorder task trong cùng cột

1. Task được kéo thả trong cùng status.
2. Backend sắp lại `boardPosition` tuần tự.
3. Phát realtime event `task.reordered`.

### 7.4. Assign task

1. Chỉ người có quyền quản lý task mới assign được.
2. Assignee phải là thành viên workspace.
3. Nếu assignee khác người thao tác, hệ thống tạo notification `TASK_ASSIGNED`.

### 7.5. Mark complete / uncomplete

1. Nếu đánh dấu hoàn thành từ cột mở, backend tìm cột closed đầu tiên và chuyển task sang đó.
2. Nếu đánh dấu chưa hoàn thành từ cột closed, backend ưu tiên khôi phục về `lastOpenStatus`.
3. Nếu `lastOpenStatus` không còn tồn tại, backend chọn cột mở đầu tiên của project.

### 7.6. Update task chi tiết

Các thông tin có thể cập nhật:

- title
- description
- notesHtml
- goal
- priority
- dueDate
- estimatedMinutes
- taskType

Lưu ý quan trọng: nếu muốn bỏ liên kết goal, frontend phải dùng `clearGoal=true`, không chỉ gửi `goalId=null`.

## 8. Flow ghi chú, bình luận, lịch và Pomodoro

### 8.1. Task notes

- Ghi chú được lưu trực tiếp vào field `tasks.notes_html`.
- Không có bảng `notes` riêng.
- Frontend dùng TipTap editor và có thể upload ảnh vào notes thông qua Azure Blob Storage.

### 8.2. Task comments

- Hỗ trợ comment theo task.
- Có `parentCommentId` nên có thể mở rộng cho reply theo cây.
- Tác giả comment hoặc người có quyền phù hợp mới được sửa/xóa.

### 8.3. Task schedules và calendar

- Mỗi task có thể có nhiều lịch.
- Lịch lưu `scheduledStart`, `scheduledEnd`, `scheduledDate`.
- Có hai kiểu xem lịch:
    - theo project
    - theo workspace

### 8.4. Pomodoro

Pomodoro là màn hình frontend giúp người dùng tập trung theo chu kỳ `focus / short break / long break` và bám theo `estimatedMinutes` của task. Khi hoàn tất một phiên focus, frontend gọi `POST /api/v1/pomodoro/tasks/{taskId}` để lưu `durationMinutes`; lịch sử phiên có thể đọc bằng `GET /api/v1/pomodoro/tasks/{taskId}`.

## 9. Flow notification và activity log

### 9.1. Notification

Notification xuất hiện khi có các sự kiện chính như:

- được giao task
- task đổi trạng thái
- task đổi lịch
- được thêm vào workspace
- invite được sử dụng

### 9.2. Activity log

Activity log dùng để truy vết thao tác đã diễn ra trong workspace như:

- tạo/sửa/xóa workspace
- thêm/xóa member
- tạo/sửa/xóa project, goal, task, team
- tạo/revoke/use invite

## 10. Flow realtime

Backend phát sự kiện realtime qua STOMP WebSocket ở các mức:

- workspace scope
- project scope
- task scope
- user-private notification scope

Frontend đăng ký lắng nghe để cập nhật UI gần thời gian thực mà không cần refresh thủ công.

## 11. Flow quản trị hệ thống

Module admin tách biệt với collaboration module. Tại đây quản trị viên hệ thống có thể:

- xem và cập nhật user
- tạo/sửa/xóa role
- tạo/sửa/xóa permission
- gán role cho user
- gán permission cho role

Đây là lớp quản trị hệ thống, khác với role nội bộ của workspace.

## 12. Kết luận luồng nghiệp vụ

Luồng nghiệp vụ của Chronelis cho thấy đây là hệ thống quản lý công việc có chiều sâu: vừa có cộng tác nội bộ, vừa có quản trị quyền, vừa có luồng thực thi công việc theo thời gian thực. Điểm đáng chú ý là toàn bộ logic quanh workspace, project, goal, task, manager, invite, realtime và audit log đã được triển khai khá nhất quán giữa frontend và backend.
