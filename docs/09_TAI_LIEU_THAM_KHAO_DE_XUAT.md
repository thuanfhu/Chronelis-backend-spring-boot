# 09. Tài Liệu Tham Khảo Đề Xuất

## 1. Mục đích

File này tổng hợp các nhóm tài liệu nên khảo sát và trích dẫn khi viết đề cương hoặc báo cáo thực tập tốt nghiệp về Chronelis. Danh sách gồm hai phần:

1. Tài liệu nội bộ của chính hệ thống Chronelis.
2. Tài liệu chuẩn, sách, documentation và nhóm chủ đề nên tham khảo bên ngoài.

## 2. Tài liệu nội bộ nên trích dẫn trước

### 2.1. Tài liệu trong repository

- `Chronelis-backend-spring-boot/README.md`
- `Chronelis-backend-spring-boot/docs/API_DESCRIPTION.md`
- `Chronelis-backend-spring-boot/docs/01_TONG_QUAN_HE_THONG.md`
- `Chronelis-backend-spring-boot/docs/02_FLOW_NGHIEP_VU_VA_QUY_TRINH_SU_DUNG.md`
- `Chronelis-backend-spring-boot/docs/03_MO_HINH_DU_LIEU_ENTITY_QUAN_HE.md`
- `Chronelis-backend-spring-boot/docs/04_KIEN_TRUC_HE_THONG_VA_TICH_HOP_FE_BE.md`
- `Chronelis-frontend-reactjs/README.md`

### 2.2. Tài liệu quy định học thuật

- `Hướng dẫn viết đề cương nghiên cứu.pdf`
- `MauQuiDinhBaocaothuctapTN.pdf`

## 3. Nhóm tài liệu kỹ thuật chính thống nên khảo sát

### 3.1. Backend và bảo mật

- Spring Boot official documentation.
- Spring Security official documentation.
- Spring Data JPA official documentation.
- Liquibase official documentation.
- Redis official documentation.

### 3.2. Frontend và state management

- React official documentation.
- React Router official documentation.
- TanStack Query official documentation.
- Zustand official documentation.
- Framer Motion official documentation.

### 3.3. Realtime và giao tiếp hệ thống

- WebSocket protocol documentation.
- STOMP protocol overview.
- Spring WebSocket/STOMP documentation.

### 3.4. Rich text và file storage

- TipTap documentation.
- Azure Blob Storage official documentation.

## 4. Nhóm chủ đề học thuật nên tìm tài liệu liên quan

Khi viết phần “Tổng quan” hoặc “Khảo sát tài liệu”, nên tìm bài viết, bài báo hoặc tài liệu liên quan đến các chủ đề sau:

1. Hệ thống quản lý công việc cộng tác cho nhóm.
2. Mô hình Kanban trong quản lý tiến độ dự án.
3. Role-Based Access Control trong ứng dụng nhiều người dùng.
4. Đồng bộ dữ liệu thời gian thực bằng WebSocket trong ứng dụng web.
5. Thiết kế hệ thống quản lý task có goal, schedule và audit log.
6. Kỹ thuật Pomodoro trong phần mềm hỗ trợ năng suất cá nhân.

## 5. Từ khóa tìm kiếm gợi ý

### 5.1. Từ khóa tiếng Việt

- hệ thống quản lý công việc cộng tác
- quản lý tiến độ công việc thời gian thực
- phân quyền theo vai trò trong hệ thống web
- mô hình Kanban trong quản lý dự án phần mềm
- ứng dụng Pomodoro trong quản lý công việc

### 5.2. Từ khóa tiếng Anh

- collaborative task management system
- real-time project management web application
- role-based access control in collaborative systems
- WebSocket-based real-time collaboration
- Kanban task management architecture
- pomodoro productivity system design

## 6. Cách sử dụng tài liệu tham khảo trong đề cương

### 6.1. Phần mở đầu

Sử dụng tài liệu để làm rõ:

- tại sao bài toán quản lý công việc cộng tác là cần thiết
- vì sao cần realtime, role management và audit log

### 6.2. Phần tổng quan

Sử dụng tài liệu để:

- trình bày các hướng tiếp cận phổ biến
- phân tích điểm mạnh/yếu của các hướng đó
- chỉ ra khoảng trống mà Chronelis đang hướng tới giải quyết

### 6.3. Phần phương pháp

Sử dụng tài liệu kỹ thuật chính thống để giải thích vì sao chọn:

- React cho frontend
- Spring Boot cho backend
- JWT + refresh cookie cho auth
- STOMP/WebSocket cho realtime
- Azure Blob Storage cho lưu trữ file

## 7. Gợi ý cách ghi tài liệu tham khảo

Theo PDF hướng dẫn, khi ghi tài liệu tham khảo cần:

- chỉ liệt kê tài liệu thực sự đã dùng hoặc trích dẫn
- giữ format nhất quán
- không lạm dụng tài liệu không liên quan trực tiếp đến bài toán

Nếu dùng official docs, có thể nhóm theo:

- tên công nghệ
- tên tài liệu/doc site
- năm truy cập
- đường dẫn truy cập nếu giảng viên cho phép ghi URL

## 8. Danh mục ưu tiên đọc nhanh

Nếu thời gian gấp, nên ưu tiên theo thứ tự sau:

1. tài liệu nội bộ trong repo Chronelis
2. React, Spring Boot, Spring Security docs
3. WebSocket/STOMP docs
4. tài liệu về RBAC và task management systems

## 9. Kết luận

Để viết đề cương tốt, phần tham khảo không cần quá nhiều nhưng phải đúng trọng tâm. Với Chronelis, nhóm tài liệu quan trọng nhất là tài liệu kỹ thuật chính thống cho stack đang dùng và tài liệu liên quan đến hệ thống cộng tác công việc, realtime, phân quyền và quản lý tiến độ.
