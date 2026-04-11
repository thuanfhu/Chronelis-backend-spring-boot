# 06. Kế Hoạch Thực Hiện Và Sản Phẩm Kỳ Vọng

## 1. Căn cứ lập kế hoạch

Theo yêu cầu thầy giao, giai đoạn đề cương tổng quát đề tài TTTN được thực hiện trong khoảng:

- bắt đầu: `26/03/2026`
- kết thúc: `15/04/2026`

Trọng tâm của giai đoạn này là:

- xác định vấn đề
- viết đề cương chi tiết
- khảo sát tài liệu
- mô tả nội dung, chương mục và sản phẩm kỳ vọng

## 2. Nhân lực dự kiến

| Vai trò                                   | Số lượng | Trách nhiệm                                      |
| ----------------------------------------- | -------- | ------------------------------------------------ |
| Sinh viên thực hiện                       | 1        | khảo sát code, viết tài liệu, tổng hợp đề cương  |
| Giảng viên hướng dẫn                      | 1        | góp ý định hướng học thuật, chuẩn hóa đề cương   |
| Mentor kỹ thuật tại nơi thực tập (nếu có) | 1        | giải thích bối cảnh hệ thống, xác nhận nghiệp vụ |

## 3. Phương tiện và công cụ thực hiện

- Máy tính cá nhân phục vụ đọc code, build và viết tài liệu.
- VS Code và GitHub Copilot để phân tích codebase.
- Môi trường backend Spring Boot.
- Môi trường frontend React/Vite.
- Tài liệu PDF hướng dẫn của nhà trường/thầy.

## 4. Kinh phí dự kiến

Giai đoạn đề cương chủ yếu sử dụng hạ tầng và công cụ sẵn có, nên kinh phí trực tiếp gần như không đáng kể. Nếu cần liệt kê trong báo cáo, có thể ghi:

- thiết bị cá nhân: đã có sẵn
- phần mềm lập trình: dùng môi trường sẵn có
- tài liệu tham khảo: chủ yếu là tài liệu số
- chi phí in ấn/nộp bản cứng: phát sinh ở giai đoạn nộp chính thức

## 5. Kế hoạch thời gian chi tiết

| Giai đoạn | Thời gian     | Nội dung thực hiện                                                           | Kết quả đầu ra                           |
| --------- | ------------- | ---------------------------------------------------------------------------- | ---------------------------------------- |
| 1         | 26/03 - 29/03 | tiếp nhận yêu cầu, đọc bài tập, xác định phạm vi hệ thống                    | xác định đề tài và mục tiêu sơ bộ        |
| 2         | 30/03 - 03/04 | khảo sát backend: controller, service, entity, DTO, auth, permission         | bản đồ API, dữ liệu và nghiệp vụ backend |
| 3         | 04/04 - 07/04 | khảo sát frontend: router, pages, feature modules, API integration, realtime | mapping FE-BE và các màn hình chính      |
| 4         | 08/04 - 10/04 | tổng hợp tổng quan hệ thống, flow nghiệp vụ, kiến trúc và mô hình dữ liệu    | bộ tài liệu kỹ thuật nền                 |
| 5         | 11/04 - 13/04 | viết đề cương tổng quát, kế hoạch, tài liệu tham khảo, cập nhật API docs     | bộ docs phục vụ nộp đề cương             |
| 6         | 14/04 - 15/04 | rà soát hình thức trình bày, chỉnh sửa theo góp ý, hoàn thiện bản nộp        | bản đề cương hoàn chỉnh                  |

## 6. Biểu diễn tiến độ dạng Gantt rút gọn

| Công việc                   | 26-29/03 | 30/03-03/04 | 04-07/04 | 08-10/04 | 11-13/04 | 14-15/04 |
| --------------------------- | -------- | ----------- | -------- | -------- | -------- | -------- |
| Xác định bài toán           | X        |             |          |          |          |          |
| Khảo sát backend            |          | X           |          |          |          |          |
| Khảo sát frontend           |          |             | X        |          |          |          |
| Chuẩn hóa tài liệu hệ thống |          |             |          | X        |          |          |
| Viết đề cương và API docs   |          |             |          |          | X        |          |
| Rà soát và hoàn thiện       |          |             |          |          |          | X        |

## 7. Sản phẩm kỳ vọng đạt được

### 7.1. Sản phẩm học thuật

1. Bản đề cương tổng quát đề tài TTTN có cấu trúc rõ ràng.
2. Phần mô tả bài toán, mục tiêu, phương pháp nghiên cứu và kết quả dự kiến.
3. Phần kế hoạch thực hiện và sản phẩm đầu ra.
4. Danh mục tài liệu khảo sát và tham khảo.

### 7.2. Sản phẩm kỹ thuật

1. Tài liệu tổng quan hệ thống Chronelis.
2. Tài liệu flow nghiệp vụ và quy trình sử dụng.
3. Tài liệu mô hình dữ liệu, entity và quan hệ.
4. Tài liệu kiến trúc FE/BE và cơ chế realtime.
5. Tài liệu tích hợp frontend với backend.
6. Tài liệu `API_DESCRIPTION.md` đủ dùng cho frontend và cho phần phụ lục kỹ thuật của báo cáo.

### 7.3. Sản phẩm phần mềm dự kiến trình bày

Nếu cần mô tả sản phẩm kỳ vọng ở góc độ phần mềm, có thể nêu:

- backend Spring Boot chạy được
- frontend React build được
- quản lý workspace, project, goal, task hoạt động thống nhất
- notification và activity log có khả năng minh họa cho luồng realtime và truy vết

## 8. Khó khăn dự kiến và cách xử lý

| Khó khăn                                              | Ảnh hưởng                           | Cách xử lý                                                   |
| ----------------------------------------------------- | ----------------------------------- | ------------------------------------------------------------ |
| Hệ thống nhiều module, khó nhìn tổng thể ngay từ đầu  | dễ bỏ sót nghiệp vụ                 | đọc theo lớp controller -> service -> DTO -> frontend route  |
| Permission động phụ thuộc seed dữ liệu                | có thể gây hiểu nhầm khi test quyền | đối chiếu thêm `PermissionInterceptor` và ghi chú ở risk log |
| Frontend và backend phát triển độc lập theo thời gian | dễ lệch contract                    | dùng DTO và route thực tế làm nguồn chính thức               |
| Tài liệu cũ có thể chưa cập nhật                      | dễ ghi sai khi viết đề cương        | bám theo code hiện tại và cập nhật lại toàn bộ docs          |

## 9. Khuyến nghị khi nộp cho thầy

1. Dùng file `05` làm lõi nội dung đề cương.
2. Trích bảng timeline trong file này sang phần kế hoạch thực hiện.
3. Dùng `01`, `02`, `03`, `04` làm phụ lục hỗ trợ nếu thầy hỏi sâu về hệ thống.
4. Dùng `API_DESCRIPTION.md` như tài liệu kỹ thuật kèm theo, không cần nhét toàn bộ API vào thân bài đề cương.

## 10. Kết luận

Trong khung thời gian từ `26/03/2026` đến `15/04/2026`, sản phẩm phù hợp nhất là một bộ đề cương và tài liệu kỹ thuật có thể chứng minh rõ ba điều: bài toán là gì, hệ thống Chronelis đã được xây ra sao, và hướng phát triển/nội dung thực tập tốt nghiệp sẽ đi theo chương mục nào.
