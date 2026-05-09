# 01. Tổng Quan Hệ Thống Chronelis

## 1. Chronelis là hệ thống gì

Chronelis là nền tảng cộng tác và quản lý tiến độ công việc theo thời gian thực, được xây dựng theo mô hình:

`Workspace -> Project -> Goal -> Task`

Mỗi workspace đại diện cho một không gian làm việc của nhóm. Trong workspace có nhiều project. Mỗi project có thể có goal, task, cột Kanban, task type, lịch làm việc, bình luận và lịch sử hoạt động. Hệ thống hỗ trợ cả quản trị nội bộ lẫn thao tác cộng tác hằng ngày của thành viên nhóm.

## 2. Đề tài đang làm có thể gọi tên như thế nào

Tên đề tài phù hợp để dùng trong đề cương thực tập tốt nghiệp:

**Phân tích, thiết kế và xây dựng hệ thống cộng tác và quản lý tiến độ công việc thời gian thực Chronelis**

Biến thể ngắn hơn nếu cần:

**Xây dựng hệ thống quản lý công việc cộng tác thời gian thực cho nhóm làm việc với Chronelis**

## 3. Bài toán mà Chronelis giải quyết

Qua code hiện tại, Chronelis đang giải quyết các vấn đề phổ biến của nhóm phát triển/phòng ban khi làm việc chung:

1. Công việc bị phân tán giữa nhiều bảng Kanban, chat, ghi chú và lịch rời rạc.
2. Thiếu cơ chế gán quyền rõ ràng giữa owner, quản lý và thành viên.
3. Khó theo dõi mục tiêu trung hạn và dài hạn nếu chỉ nhìn task rời rạc.
4. Thiếu lịch sử hoạt động để biết ai đã thay đổi gì và khi nào.
5. Thiếu cập nhật thời gian thực, dẫn đến dữ liệu giữa các thành viên bị lệch.
6. Ghi chú công việc, lịch làm việc và phiên tập trung chưa nằm chung trong một hệ thống.

Chronelis gom các nhu cầu đó vào một nền tảng thống nhất với workspace, team, project, goal, task, lịch, notification, activity log và phân quyền theo vai trò.

## 4. Mức độ cần thiết của đề tài

Đề tài có tính cần thiết vì:

- Nhiều nhóm nhỏ và vừa cần một công cụ quản lý công việc theo ngữ cảnh riêng của dự án thay vì dùng bảng việc rời rạc.
- Các công cụ phổ biến thường mạnh ở Kanban hoặc chat, nhưng chưa gắn chặt goal, notes, pomodoro, lịch và audit log trong cùng luồng.
- Doanh nghiệp và nhóm thực tập cần kiểm soát ai được làm gì trong workspace bằng quyền chi tiết hơn mô hình chỉ có admin/user.
- Nhu cầu cập nhật thời gian thực ngày càng quan trọng khi nhiều người cùng thao tác trên một task hoặc project.

## 5. Mục tiêu của hệ thống

### 5.1. Mục tiêu tổng quát

Xây dựng hệ thống hỗ trợ cộng tác nhóm và quản lý tiến độ công việc theo thời gian thực, có phân quyền, có khả năng theo dõi lịch sử hoạt động và phù hợp cho nhóm làm việc theo project.

### 5.2. Mục tiêu cụ thể

1. Cho phép người dùng đăng ký, xác thực email, đăng nhập và quản lý hồ sơ cá nhân.
2. Tạo và quản lý workspace, thành viên, role nội bộ và team làm việc.
3. Quản lý project với người quản lý theo user hoặc theo team.
4. Theo dõi goal của project theo loại ngắn hạn, trung hạn, dài hạn.
5. Quản lý task theo nhiều góc nhìn: calendar, Kanban, to-do, goal-focused.
6. Hỗ trợ bình luận, ghi chú HTML, lịch task và Pomodoro để làm việc tập trung.
7. Phát thông báo và sự kiện realtime để cập nhật thay đổi ngay cho người dùng.
8. Cung cấp module admin để quản trị user, role và permission ở cấp hệ thống.

## 6. Đối tượng người dùng

### 6.1. Quản trị viên hệ thống

- Quản lý users, roles, permissions.
- Kiểm soát truy cập API bằng permission động từ database.

### 6.2. Chủ sở hữu workspace

- Tạo workspace.
- Thêm/xóa thành viên.
- Cập nhật vai trò thành viên.
- Tạo team.
- Gán manager cho project/goal theo user hoặc team.
- Tạo và thu hồi invite.

### 6.3. Quản lý workspace hoặc quản lý project

- Tạo project, goal, task.
- Phân công và điều phối công việc.
- Theo dõi trạng thái thực hiện.

### 6.4. Thành viên workspace

- Tham gia dự án, cập nhật task, xem lịch, bình luận, ghi chú, nhận thông báo.

## 7. Phạm vi chức năng hiện có

### 7.1. Nhóm chức năng xác thực và tài khoản

- Đăng ký.
- Xác thực email kích hoạt tài khoản.
- Đăng nhập bằng email hoặc số điện thoại.
- Refresh token bằng cookie HttpOnly.
- Quên mật khẩu và đặt lại mật khẩu.
- Đổi email, đổi mật khẩu, cập nhật hồ sơ.

### 7.2. Nhóm chức năng cộng tác

- Workspace.
- Workspace members.
- Workspace teams.
- Invite code tham gia workspace.

### 7.3. Nhóm chức năng quản lý công việc

- Projects.
- Goals.
- Task statuses.
- Task types.
- Tasks.
- Task comments.
- Task schedules.
- Activity logs.
- Notifications.

### 7.4. Nhóm chức năng làm việc sâu

- Pomodoro page cho từng task.
- Ghi chú HTML bằng editor TipTap.
- Upload ảnh vào ghi chú task qua AWS S3.

### 7.5. Nhóm chức năng quản trị hệ thống

- Quản trị user.
- Quản trị role.
- Quản trị permission.

## 8. Công nghệ chính được sử dụng

### 8.1. Backend

- Java 25.
- Spring Boot 4.0.4.
- Spring Security OAuth2 Resource Server.
- Spring Data JPA + MySQL.
- Liquibase.
- Redis cho blacklist token.
- WebSocket STOMP.
- AWS S3.
- Thymeleaf mail templates.

### 8.2. Frontend

- React 19.
- TypeScript.
- Vite.
- React Router.
- TanStack Query.
- Zustand.
- Framer Motion.
- shadcn/Radix UI style components.
- TipTap editor.

## 9. Điểm nổi bật của Chronelis so với bài toán quản lý việc đơn giản

1. Không chỉ có task mà còn có goal để liên kết công việc với mục tiêu.
2. Không chỉ có phân quyền hệ thống mà còn có vai trò theo workspace.
3. Hỗ trợ manager theo user hoặc theo team.
4. Có realtime event theo workspace, project, task và notification riêng theo user.
5. Có activity log giúp truy vết thay đổi.
6. Có pomodoro và notes gắn trực tiếp vào task.
7. Có admin module quản lý RBAC động bằng permission theo `apiPath + httpMethod`.

## 10. Phạm vi chưa thấy trong code hiện tại

Các phần sau không phải trọng tâm hiện tại hoặc chưa có dấu hiệu hoàn thiện end-to-end trong repository:

- Mobile app độc lập.
- Tích hợp chat realtime giữa người dùng.
- Báo cáo BI/analytics chuyên sâu.
- Tự động hóa workflow bằng AI agent nội bộ.
- Check item task hoàn chỉnh ở layer API dù enum vẫn còn dấu vết.

## 11. Kết luận tổng quan

Chronelis hiện không phải là ứng dụng to-do đơn giản mà là hệ thống cộng tác dự án theo thời gian thực, kết hợp quản trị người dùng, kiểm soát quyền truy cập, quản lý tiến độ và hỗ trợ làm việc tập trung. Đây là nền tảng đủ rõ bài toán, đủ chiều sâu kỹ thuật và đủ phạm vi nghiệp vụ để phát triển thành đề tài thực tập tốt nghiệp có tính phân tích, thiết kế và triển khai phần mềm.
