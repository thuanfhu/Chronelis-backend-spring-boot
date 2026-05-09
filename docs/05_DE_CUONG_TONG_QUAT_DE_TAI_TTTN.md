# 05. Đề Cương Tổng Quát Đề Tài TTTN

## 1. Mục đích của file này

File này là bản nháp nội dung để dùng khi viết đề cương thực tập tốt nghiệp cho Chronelis, bám theo yêu cầu trong:

- `Hướng dẫn viết đề cương nghiên cứu.pdf`
- `MauQuiDinhBaocaothuctapTN.pdf`
- bài tập thầy giao về đề cương tổng quát đề tài TTTN

## 2. Tên đề tài đề xuất

**Phân tích, thiết kế và xây dựng hệ thống cộng tác và quản lý tiến độ công việc thời gian thực Chronelis**

## 3. Mở đầu

### 3.1. Đặt vấn đề

Trong môi trường học tập, thực tập và làm việc nhóm hiện nay, công việc thường bị phân tán giữa nhiều công cụ khác nhau như bảng việc, lịch, ghi chú cá nhân, email và tin nhắn. Điều này làm giảm khả năng theo dõi tiến độ tổng thể, gây khó khăn khi phân quyền, truy vết lịch sử thay đổi và phối hợp giữa các thành viên trong cùng một dự án.

Chronelis được xây dựng để giải quyết bài toán đó bằng cách gom toàn bộ không gian cộng tác vào một nền tảng duy nhất theo cấu trúc workspace, project, goal và task. Hệ thống đồng thời hỗ trợ thông báo thời gian thực, lịch công việc, ghi chú rich text, Pomodoro và quản trị quyền truy cập theo nhiều cấp.

### 3.2. Tính cần thiết của đề tài

Đề tài có tính cần thiết vì:

1. Các nhóm phát triển phần mềm và nhóm thực tập cần công cụ quản lý công việc có phân quyền rõ ràng.
2. Nhiều hệ thống quản lý việc chỉ dừng ở CRUD task, chưa gắn chặt mục tiêu, lịch, notes và realtime.
3. Nhu cầu giám sát tiến độ theo project và goal ngày càng quan trọng trong môi trường làm việc nhóm.
4. Việc nghiên cứu và hiện thực một hệ thống như Chronelis giúp sinh viên vận dụng đồng thời kiến thức về frontend, backend, bảo mật, realtime, dữ liệu và quy trình phần mềm.

### 3.3. Khoảng trống cần tập trung giải quyết

Qua phân tích code hiện tại, các khoảng trống của những mô hình quản lý công việc đơn giản mà Chronelis đang hướng tới giải quyết gồm:

- thiếu cấu trúc phân tầng giữa workspace, project, goal và task
- thiếu đồng bộ realtime giữa nhiều người dùng
- thiếu cơ chế audit log và notification rõ ràng
- thiếu khả năng gán manager theo người hoặc theo team
- thiếu tích hợp lịch, ghi chú và phiên tập trung ngay trong luồng task

### 3.4. Câu hỏi nghiên cứu

1. Làm thế nào để thiết kế một hệ thống cộng tác công việc có thể quản lý nhiều cấp: workspace, project, goal và task?
2. Làm thế nào để kết hợp phân quyền hệ thống với phân quyền nội bộ workspace mà vẫn đảm bảo tính linh hoạt?
3. Làm thế nào để đồng bộ thay đổi công việc gần thời gian thực giữa nhiều thành viên?
4. Làm thế nào để bổ sung lịch, ghi chú và Pomodoro vào luồng quản lý task mà không làm rời rạc trải nghiệm người dùng?

### 3.5. Mục tiêu nghiên cứu cụ thể

1. Phân tích bài toán cộng tác và quản lý tiến độ công việc cho nhóm.
2. Mô hình hóa dữ liệu và các thực thể nghiệp vụ chính của Chronelis.
3. Thiết kế backend API có hỗ trợ bảo mật, phân quyền và realtime.
4. Thiết kế frontend phản ánh đúng workflow của người dùng theo từng vai trò.
5. Mô tả đầy đủ các API, luồng nghiệp vụ, dữ liệu và ràng buộc để phục vụ triển khai, kiểm thử và báo cáo.

### 3.6. Đối tượng và phạm vi nghiên cứu

#### Đối tượng nghiên cứu

- Hệ thống Chronelis ở cả frontend và backend.
- Các module xác thực, cộng tác, quản lý task, notification, activity log và admin.

#### Phạm vi nghiên cứu

- Frontend React của Chronelis.
- Backend Spring Boot của Chronelis.
- API, entity, workflow và kiến trúc realtime hiện có trong code.

#### Ngoài phạm vi

- Mobile app độc lập.
- Báo cáo BI nâng cao.
- Tích hợp chat giữa người dùng.
- AI automation workflow.

## 4. Chương 1. Tổng quan

### 4.1. Chủ đề nghiên cứu

Chủ đề của đề tài là xây dựng hệ thống cộng tác công việc thời gian thực cho nhóm làm việc theo mô hình dự án, trong đó công việc được quản lý theo cấu trúc workspace, project, goal và task.

### 4.2. Tổng quan hệ thống đang nghiên cứu

Chronelis đã có các nhóm chức năng chính:

- Auth và quản lý tài khoản.
- Workspaces, members, teams, invites.
- Projects, goals, tasks, statuses, types.
- Calendar, comments, notes, Pomodoro.
- Notifications và activity logs.
- Admin module quản trị RBAC.

### 4.3. Những điểm cần tập trung phân tích

1. Cách tổ chức dữ liệu nhiều lớp và quan hệ giữa các entity.
2. Cơ chế kiểm soát quyền truy cập ở cả hệ thống và workspace.
3. Cơ chế realtime cập nhật thay đổi.
4. Luồng đồng bộ giữa trạng thái task, goal progress và activity/notification.
5. Mapping giữa frontend routes và backend APIs.

### 4.4. Hướng giải quyết trong đề tài

Đề tài sẽ giải quyết bằng cách:

- phân tích mã nguồn và nghiệp vụ hiện tại
- hệ thống hóa các module chức năng và luồng sử dụng
- mô tả mô hình dữ liệu và ràng buộc nghiệp vụ
- mô tả kiến trúc FE/BE và giao thức realtime
- xây dựng bộ tài liệu API và tài liệu kỹ thuật đầy đủ để làm nền cho báo cáo

## 5. Chương 2. Phương pháp nghiên cứu

## 5.1. Thiết kế nghiên cứu

Đây là nghiên cứu theo hướng ứng dụng phần mềm và phân tích hệ thống, tập trung vào việc đọc hiểu, mô hình hóa và đối chiếu hệ thống Chronelis đã được xây dựng.

Thiết kế nghiên cứu gồm 4 bước:

1. Khảo sát yêu cầu và xác định bài toán.
2. Phân tích kiến trúc, dữ liệu, API và luồng nghiệp vụ từ code.
3. Đối chiếu frontend và backend để tìm mapping chức năng thực tế.
4. Chuẩn hóa kết quả dưới dạng đề cương, tài liệu API và tài liệu kỹ thuật.

## 5.2. Thời gian và địa điểm nghiên cứu

- Thời gian thực hiện theo yêu cầu bài tập: từ `26/03/2026` đến `15/04/2026`.
- Địa điểm nghiên cứu: môi trường làm việc với mã nguồn Chronelis tại nơi thực tập và máy trạm cá nhân phục vụ phân tích, build và đối chiếu hệ thống.

## 5.3. Đối tượng nghiên cứu

- Source code frontend.
- Source code backend.
- Hợp đồng API giữa FE và BE.
- Các workflow nghiệp vụ cốt lõi của hệ thống.

## 5.4. Cỡ mẫu nghiên cứu

Do đây là đề tài ứng dụng phần mềm, cỡ mẫu không được hiểu theo nghĩa thống kê người trả lời khảo sát. Cỡ mẫu trong nghiên cứu này được hiểu là tập hợp module và use case được đưa vào phạm vi phân tích, bao gồm:

- 17 REST controllers backend
- các feature modules chính của frontend
- toàn bộ luồng auth, workspace, project, goal, task, notification, admin

## 5.5. Các biến và tiêu chí quan sát

Để phân tích hệ thống, có thể xem các biến quan sát chính là:

- vai trò người dùng: `ADMIN`, `USER`, `OWNER`, `ADMIN`, `MEMBER`
- trạng thái project, goal, task
- assignee và manager theo user/team
- nguồn view tạo task: `KANBAN`, `TODO`, `CALENDAR`
- các sự kiện realtime và activity log phát sinh sau thao tác

## 5.6. Phương pháp và công cụ thu thập dữ liệu

- Đọc controller, service, DTO, entity, mapper, router, page component.
- Build frontend để kiểm tra tính nhất quán kiểu dữ liệu.
- Đối chiếu route frontend với API backend.
- Đọc tài liệu PDF hướng dẫn đề cương và báo cáo để chuẩn hóa cấu trúc nội dung.

## 5.7. Quy trình nghiên cứu

1. Đọc yêu cầu học thuật và xác định cấu trúc đề cương cần viết.
2. Quét toàn bộ codebase Chronelis FE + BE.
3. Tóm tắt bài toán, nghiệp vụ, kiến trúc và dữ liệu.
4. Soạn tài liệu kỹ thuật và đề cương theo chương mục yêu cầu.
5. Rà soát độ khớp giữa tài liệu và code.

## 5.8. Phương pháp phân tích dữ liệu

- Phân tích định tính theo use case.
- Đối chiếu contract API giữa request DTO, response DTO và frontend integration.
- Phân tích quan hệ entity và ràng buộc nghiệp vụ từ service layer.
- Tổng hợp kết quả thành bảng, sơ đồ và mô tả có cấu trúc.

## 5.9. Đạo đức trong nghiên cứu

- Không công bố public các khóa bí mật, token hay thông tin tài khoản thật.
- Không sử dụng trái phép dữ liệu người dùng.
- Mọi kết luận trong đề cương phải bám vào code và tài liệu thực tế, tránh phỏng đoán không có căn cứ.

## 6. Chương 3. Dự kiến kết quả

| Mục tiêu            | Kết quả dự kiến                                                                        |
| ------------------- | -------------------------------------------------------------------------------------- |
| Phân tích bài toán  | Mô tả đầy đủ mục tiêu hệ thống, actor, phạm vi và nhu cầu thực tế                      |
| Phân tích nghiệp vụ | Tài liệu flow end-to-end cho auth, workspace, project, goal, task, notification, admin |
| Phân tích dữ liệu   | Tài liệu entity, quan hệ, enum, ràng buộc dữ liệu                                      |
| Phân tích kiến trúc | Tài liệu FE/BE, websocket, auth, permission, storage, email                            |
| Chuẩn hóa API       | Tài liệu `API_DESCRIPTION.md` theo style Postman                                       |
| Phục vụ đề cương    | Bản nháp đề cương tổng quát, kế hoạch thực hiện, tài liệu tham khảo đề xuất            |

## 7. Chương 4. Kế hoạch thực hiện

Chi tiết timeline, nhân lực, phương tiện, khó khăn và sản phẩm kỳ vọng được trình bày tại file `06_KE_HOACH_THUC_HIEN_VA_SAN_PHAM_KY_VONG.md`.

## 8. Gợi ý cách ráp thành đề cương nộp thầy

1. Dùng phần `Mở đầu` trong file này làm phần mở bài.
2. Dùng `01`, `02`, `03`, `04` để kéo sang phần Tổng quan và Phương pháp.
3. Dùng `06` cho phần kế hoạch, nhân lực, tiến độ.
4. Dùng `09` để viết mục Tài liệu tham khảo.

## 9. Ghi chú hình thức trình bày theo PDF

- Font Times New Roman.
- Cỡ chữ 13 hoặc 14.
- Giãn dòng 1.5.
- Trình bày sạch, rõ, đánh số chương mục chuẩn.
- Đánh số trang theo đúng quy định của file PDF hướng dẫn.

## 10. Kết luận

Với phạm vi hiện có, Chronelis là đề tài đủ tốt để viết đề cương thực tập tốt nghiệp vì vừa có chiều sâu nghiệp vụ, vừa có đủ thành phần kỹ thuật hiện đại như realtime, RBAC, REST API, quản lý tiến độ và tích hợp frontend/backend rõ ràng.
