# 00. Mục Lục Tài Liệu Chronelis

## 1. Mục tiêu bộ tài liệu

Bộ tài liệu này được soạn lại dựa trên code thực tế của hai repository:

- `Chronelis-backend-spring-boot`
- `Chronelis-frontend-reactjs`

Mục tiêu là phục vụ đồng thời hai nhu cầu:

1. Mô tả chính xác hệ thống Chronelis đang làm gì ở thời điểm hiện tại.
2. Hỗ trợ viết đề cương thực tập tốt nghiệp theo yêu cầu trong hai file PDF và nội dung thầy giao:
    - phải xác định rõ vấn đề nghiên cứu
    - mô tả nội dung, chương mục, thời gian thực hiện
    - nêu sản phẩm kỳ vọng đạt được
    - bám theo hướng dẫn về đề cương và báo cáo thực tập tốt nghiệp

## 2. Nguồn đối chiếu dùng để soạn tài liệu

### 2.1. Nguồn nội bộ từ repository

- Code backend: controller, service, repository, dto, mapper, entity, config.
- Code frontend: router, pages, feature modules, API wrapper, websocket hooks.
- Tài liệu cũ: `API_DESCRIPTION.md`.

### 2.2. Nguồn yêu cầu học thuật

- `docs/Hướng dẫn viết đề cương nghiên cứu.pdf`
- `docs/MauQuiDinhBaocaothuctapTN.pdf`
- Nội dung thầy giao trong ảnh bài tập:
    - viết đề cương thực tập tốt nghiệp
    - nêu nội dung, thời gian thực hiện, chương mục cần làm, sản phẩm kỳ vọng
    - tập trung xác định vấn đề, viết đề cương chi tiết, khảo sát tài liệu
    - thời gian thực hiện từ `26/03/2026` đến `15/04/2026`

## 3. Danh sách tài liệu

1. `00_MUC_LUC_TAI_LIEU.md`: mục lục và cách dùng bộ tài liệu.
2. `01_TONG_QUAN_HE_THONG.md`: xác định đề tài, bài toán, phạm vi và ý nghĩa của Chronelis.
3. `02_FLOW_NGHIEP_VU_VA_QUY_TRINH_SU_DUNG.md`: các luồng nghiệp vụ end-to-end bám sát code.
4. `03_MO_HINH_DU_LIEU_ENTITY_QUAN_HE.md`: mô hình dữ liệu, entity, quan hệ và enum nghiệp vụ.
5. `04_KIEN_TRUC_HE_THONG_VA_TICH_HOP_FE_BE.md`: kiến trúc frontend, backend, realtime và tích hợp.
6. `05_DE_CUONG_TONG_QUAT_DE_TAI_TTTN.md`: bản nháp đề cương tổng quát bám yêu cầu thầy.
7. `06_KE_HOACH_THUC_HIEN_VA_SAN_PHAM_KY_VONG.md`: tiến độ, nhân lực, phương tiện, kết quả kỳ vọng.
8. `07_FRONTEND_SU_DUNG_API_NHU_THE_NAO.md`: mapping màn hình frontend với API backend.
9. `08_GHI_CHU_DOI_CHIEU_CODE_VA_RUI_RO.md`: các lưu ý, ràng buộc, mismatch và risk log.
10. `09_TAI_LIEU_THAM_KHAO_DE_XUAT.md`: nhóm tài liệu nên khảo sát và trích dẫn khi viết đề cương/báo cáo.
11. `API_DESCRIPTION.md`: mô tả API theo style Postman, bám DTO và controller hiện tại.
12. `../../Chronelis-frontend-reactjs/docs/FRONTEND_GUIDE.md`: tài liệu onboarding frontend theo route, page, component, state, API và realtime.

## 4. Tài liệu nào tương ứng với yêu cầu của thầy

### 4.1. Phần xác định vấn đề và lý do chọn đề tài

- Đọc `01_TONG_QUAN_HE_THONG.md`
- Đọc `05_DE_CUONG_TONG_QUAT_DE_TAI_TTTN.md`

### 4.2. Phần tổng quan, khảo sát hệ thống và nghiên cứu liên quan

- Đọc `01_TONG_QUAN_HE_THONG.md`
- Đọc `02_FLOW_NGHIEP_VU_VA_QUY_TRINH_SU_DUNG.md`
- Đọc `04_KIEN_TRUC_HE_THONG_VA_TICH_HOP_FE_BE.md`
- Đọc `09_TAI_LIEU_THAM_KHAO_DE_XUAT.md`

### 4.3. Phần phương pháp nghiên cứu và cách triển khai

- Đọc `04_KIEN_TRUC_HE_THONG_VA_TICH_HOP_FE_BE.md`
- Đọc `07_FRONTEND_SU_DUNG_API_NHU_THE_NAO.md`
- Đọc `API_DESCRIPTION.md`

### 4.4. Phần dự kiến kết quả và sản phẩm

- Đọc `05_DE_CUONG_TONG_QUAT_DE_TAI_TTTN.md`
- Đọc `06_KE_HOACH_THUC_HIEN_VA_SAN_PHAM_KY_VONG.md`

### 4.5. Phần phụ lục kỹ thuật khi viết báo cáo thực tập tốt nghiệp

- Đọc `03_MO_HINH_DU_LIEU_ENTITY_QUAN_HE.md`
- Đọc `07_FRONTEND_SU_DUNG_API_NHU_THE_NAO.md`
- Đọc `API_DESCRIPTION.md`
- Đọc `../../Chronelis-frontend-reactjs/docs/FRONTEND_GUIDE.md` nếu cần onboarding chi tiết cho frontend.

## 5. Thứ tự đọc khuyến nghị

1. Đọc `01` để hiểu Chronelis là hệ thống gì.
2. Đọc `02` để nắm quy trình người dùng và logic kinh doanh.
3. Đọc `03` và `04` để hiểu data model, kiến trúc, realtime và tích hợp FE/BE.
4. Đọc `05` và `06` để ráp thành đề cương nộp cho thầy.
5. Đọc `07`, `08`, `API_DESCRIPTION` và frontend guide khi cần chi tiết kỹ thuật.

## 6. Ghi chú phạm vi

- Tài liệu chỉ mô tả các chức năng đã xuất hiện trong code hiện tại.
- Những điểm còn bất nhất hoặc cần xác minh thêm được gom ở `08_GHI_CHU_DOI_CHIEU_CODE_VA_RUI_RO.md`.
- Tài liệu ưu tiên tính chính xác kỹ thuật để có thể dùng trực tiếp khi viết đề cương và báo cáo.
