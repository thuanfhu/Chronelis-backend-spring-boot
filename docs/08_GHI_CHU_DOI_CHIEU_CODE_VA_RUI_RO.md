# 08. Ghi Chú Đối Chiếu Code Và Rủi Ro

## 1. Mục đích

File này tổng hợp những điểm đã xác minh từ code và những chỗ cần đặc biệt chú ý khi viết tài liệu hoặc triển khai tiếp. Mục tiêu là tránh mô tả sai hệ thống hoặc bỏ qua các ràng buộc quan trọng.

## 2. Các điểm đã xác minh từ code

### 2.1. Permission là permission động theo database

Backend không authorize theo role cứng cho hầu hết API. Sau khi qua JWT, request còn đi qua `PermissionInterceptor` để kiểm tra `apiPath + httpMethod` so với dữ liệu permission trong DB.

Ý nghĩa:

- nếu seed permission thiếu, endpoint có thể trả lỗi dù controller và service đã đúng
- tài liệu phải mô tả đây là cơ chế phân quyền động

### 2.2. Workspace role khác system role

Có hai lớp quyền:

- system role: `ADMIN`, `USER`
- workspace role: `OWNER`, `MEMBER`
- project access role: `MANAGER`, `CONTRIBUTOR`, `VIEWER`

Nếu không phân biệt rõ, tài liệu rất dễ mô tả sai.

### 2.3. Project và goal manager có thể là user hoặc team

Đây là rule quan trọng trong code. Frontend cũng đã hiển thị manager user/team ở workspace detail page.

### 2.4. Tạo project tự sinh cột Kanban mặc định

Ngay sau khi tạo project, backend sinh sẵn:

- `To do`
- `In Progress`
- `Done`

### 2.5. Task completion đã có cơ chế khôi phục cột mở

`Task.lastOpenStatus` đang được dùng để đưa task quay lại cột mở khi đánh dấu chưa hoàn thành. Vì vậy không nên dùng nhận định cũ rằng task sẽ luôn kẹt ở cột done sau khi uncheck.

### 2.6. Notes của task không có bảng riêng

`notesHtml` nằm trực tiếp trong bảng `tasks`. Đây là điểm phải ghi đúng khi mô tả mô hình dữ liệu.

## 3. Rủi ro và mismatch kỹ thuật cần chú ý

### 3.1. `UNAUTHORIZED_ACCESS` đang map về HTTP 401

Trong `ErrorCode`, `UNAUTHORIZED_ACCESS` map sang `HttpStatus.UNAUTHORIZED`. Về semantics, nhiều trường hợp này gần với `403 Forbidden` hơn.

Tác động:

- frontend có thể xử lý nhầm như lỗi hết phiên
- tài liệu nên ghi rõ hiện trạng thay vì ghi theo kỳ vọng lý tưởng

### 3.2. Endpoint `POST /users/staff-requests` có dấu hiệu bất nhất

Controller và message mô tả là nâng cấp thành STAFF, nhưng `UserServiceImpl.becomeStaff()` hiện kiểm tra và gán `RoleType.USER_ROLE`. Đây là điểm cần xem lại nếu muốn tài liệu 100% hành vi-endpoint theo ý nghĩa nghiệp vụ.

Khuyến nghị:

- nêu endpoint này là endpoint đặc biệt đang cần rà soát thêm
- tránh mô tả quá mạnh rằng hệ thống đã có `STAFF` role hoàn chỉnh nếu chưa xác nhận seed/enum tương ứng

### 3.3. `CHECK_ITEM` vẫn còn trong enum nhưng không còn API chính thức

`ActivityTargetType` và `ReferenceType` vẫn có `CHECK_ITEM`, nhưng hiện không có controller CRUD check item trong codebase hiện tại.

Tác động:

- tài liệu nghiệp vụ không nên đưa check item thành module đang hoạt động
- có thể ghi đây là dấu vết của feature cũ hoặc hướng mở rộng trong tương lai

### 3.4. Team và manager update bị giới hạn mạnh cho owner

Không chỉ tạo invite, thêm member hay tạo team, mà cả việc set/chỉnh `managerUserId` hoặc `managerTeamId` cho project/goal cũng yêu cầu owner workspace.

Điều này cần được nhấn mạnh trong tài liệu vì workspace không còn role `ADMIN`; phân quyền chi tiết nằm ở project access grant.

### 3.5. Xóa dữ liệu là hard delete

Các thao tác như xóa task, xóa team, xóa project/workspace hiện không có mô hình soft delete rõ ràng. Một số service còn cleanup thủ công comment/schedule/team member để phòng trường hợp DB không cascade.

Tác động:

- cần cẩn trọng khi test và seed dữ liệu
- nếu viết phần hạn chế hệ thống, đây là một điểm đáng nêu

### 3.6. Frontend bundle vẫn có cảnh báo chunk lớn

Build frontend hiện thành công nhưng Vite có cảnh báo chunk JS lớn hơn ngưỡng mặc định. Đây không phải lỗi chạy ứng dụng, nhưng là rủi ro hiệu năng cần theo dõi nếu hệ thống tiếp tục mở rộng.

### 3.7. Calendar visibility phụ thuộc đúng permission seed

Để thành viên thường thấy lịch đúng, hai API calendar theo project và workspace đều cần mapping permission chính xác trong DB. Đây là chỗ dễ phát sinh lỗi “có task nhưng lịch trống”.

## 4. Ghi chú khi viết tài liệu học thuật

1. Nên mô tả theo code hiện tại, không mô tả theo ý định thiết kế chưa thành hiện thực.
2. Nên phân biệt rõ “đang có trong code” và “hướng phát triển tiếp theo”.
3. Với các điểm còn nghi ngờ, nên ghi theo dạng “cần rà soát thêm” thay vì khẳng định tuyệt đối.

## 5. Những điểm mạnh có thể đưa vào phần đánh giá hệ thống

1. Phân tầng dữ liệu rõ: workspace -> project -> goal -> task.
2. Có phân quyền hai lớp và realtime event.
3. Có activity log và notification, phù hợp sản phẩm cộng tác nhóm.
4. Có task notes HTML và Pomodoro, giúp đề tài có chiều sâu hơn CRUD thông thường.
5. Frontend và backend nhìn chung đã align tương đối tốt ở route, DTO và workflow.

## 6. Kết luận

Chronelis có nền tảng kỹ thuật tốt để viết đề tài thực tập tốt nghiệp, nhưng khi viết tài liệu cần cẩn thận với các điểm nhạy như semantics 401/403, feature dấu vết `CHECK_ITEM`, endpoint `staff-requests` và các permission phụ thuộc seed dữ liệu. File này nên được dùng như risk log nội bộ mỗi khi chỉnh sửa thêm bộ docs.
