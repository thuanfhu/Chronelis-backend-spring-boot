# Chronelis Backend Spring Boot

Backend cho hệ thống Chronelis, xây dựng bằng Spring Boot. Hệ thống quản lý người dùng, xác thực JWT, phân quyền, lưu trữ tệp trên AWS S3, và hỗ trợ giao tiếp thời gian thực qua WebSocket.

## Tổng quan kỹ thuật

- Tên dự án Maven: `Chronelis`
- Artifact ID: `chronelis-backend-spring-boot`
- Phiên bản ứng dụng: `0.0.1-SNAPSHOT`
- Spring Boot: `4.0.4`
- Java: `25`
- Maven Wrapper: `3.9.14`

## Công nghệ đang sử dụng

- Spring Boot Starter Web
- Spring Boot Starter WebSocket (STOMP over WebSocket)
- Spring Security + OAuth2 Resource Server
- Spring Data JPA
- Spring Validation
- Spring Mail
- Thymeleaf
- MySQL Connector/J
- Liquibase
- Redis + Jedis
- Bucket4j
- MapStruct
- Lombok
- AWS SDK v2 S3
- dotenv-java

## Chức năng chính

- Xác thực và phân quyền theo JWT
- Đăng ký, đăng nhập, làm mới token, quên mật khẩu, xác thực email
- Quản lý người dùng, vai trò và quyền
- Tải lên, xoá và di chuyển tệp trên AWS S3
- Email HTML dùng Thymeleaf cho xác thực tài khoản và đặt lại mật khẩu
- WebSocket (STOMP) cho giao tiếp thời gian thực với xác thực JWT

## WebSocket

Dự án sử dụng STOMP over WebSocket cho giao tiếp thời gian thực:

- **Endpoint kết nối**: `/ws`
- **Xác thực**: JWT token gửi qua header `Authorization` trong STOMP CONNECT
- **Prefix server**: `/server` (client gửi message tới đây)
- **Prefix client**: `/client` (nhận message riêng cho user)
- **Broker**: `/public` (broadcast), `/private` (riêng tư)

Cấu trúc WebSocket:

- `WebSocketConfig`: Cấu hình STOMP broker và endpoint
- `WebSocketEventListener`: Xử lý sự kiện kết nối/ngắt kết nối
- `UserInterceptor`: Xác thực JWT cho kết nối WebSocket
- `WebSocketExceptionHandler`: Xử lý lỗi toàn cục cho WebSocket

## Yêu cầu hệ thống

1. Java Development Kit: JDK `25`
2. MySQL: qua biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
3. Redis: cho blacklist token
4. Git: clone hoặc đồng bộ mã nguồn
5. Maven: không bắt buộc nếu dùng Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Cấu trúc cấu hình quan trọng

- File cấu hình Spring: `src/resources/application.yaml`
- File biến môi trường mẫu: `src/resources/.env.example`
- File biến môi trường thực tế: `src/resources/.env`
- Template email Thymeleaf: `src/resources/templates/verify-email.html`, `src/resources/templates/forgot-password.html`
- Liquibase master changelog: `src/resources/db/changelog/db.changelog-master.xml`

Ứng dụng nạp biến môi trường bằng `dotenv-java` từ file `.env`.

## Biến môi trường cần cấu hình

Tạo hoặc cập nhật file `src/resources/.env` dựa trên `src/resources/.env.example`.

```env
FRONTEND_BASE_URL=http://localhost:5173
ACCOUNT_BASE_PASSWORD=Chronelis123@
ALLOWED_INIT=true

SERVER_PORT=8080

DB_URL=jdbc:mysql://localhost:3306/chronelis
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_password

JWT_ACCESS_SIGNER_KEY=your_access_signer_key
JWT_REFRESH_SIGNER_KEY=your_refresh_signer_key
JWT_ACCESS_TOKEN_DURATION=3600
JWT_REFRESH_TOKEN_DURATION=604800

AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_BUCKET_NAME=your_bucket_name
AWS_REGION=ap-southeast-1

REDIS_HOST=your_redis_host
REDIS_PORT=6379
REDIS_USERNAME=default
REDIS_PASSWORD=your_redis_password
REDIS_SSL=true
```

## Cách chạy dự án

### Windows

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Chạy file JAR sau khi build

```bash
java -jar target/chronelis-backend-spring-boot-0.0.1-SNAPSHOT.jar
```

## Kiểm thử

```bash
./mvnw test          # macOS / Linux
.\mvnw.cmd test      # Windows
```

## Truy cập hệ thống

- Backend mặc định: `http://localhost:8080`
- API base path: `/api/v1`
- WebSocket endpoint: `ws://localhost:8080/ws`
- Frontend mặc định: `http://localhost:5173`

Endpoint công khai:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/forgot-password`

## Template engine

Thymeleaf dùng để render email HTML, không dùng làm server-side view.

- `verify-email.html`: email xác thực tài khoản
- `forgot-password.html`: email đặt lại mật khẩu

## Color theme

Chronelis sử dụng bảng màu Deep Indigo / Teal:

- **Primary (Deep Indigo / Blue-Violet)**: `#4a3ab5` — biểu thị sự quy hoạch, rõ ràng, trí tuệ và tin cậy
- **Dark background**: `#1a1a2e` — nền tối tạo cảm giác chuyên nghiệp và tập trung
- **Accent (Teal)**: `#2bbcb3` — biểu thị sự linh hoạt, tập trung và cộng tác
- **Hover state**: `#3a2d96` — Dark Indigo cho trạng thái hover

## Chuẩn response API

### Thành công

```json
{
    "success": true,
    "message": "Đăng nhập thành công",
    "data": { ... },
    "meta": {
        "timestamp": "2026-03-27T09:30:00Z",
        "instance": "/api/v1/auth/login"
    }
}
```

### Lỗi

```json
{
    "success": false,
    "errors": [
        {
            "code": 1102,
            "message": "Định dạng email không hợp lệ",
            "field": "email",
            "resource": "registerUserRequest"
        }
    ],
    "meta": { ... }
}
```

## Seed dữ liệu mặc định

Khi `ALLOWED_INIT=true`, ứng dụng seed quyền, vai trò và tài khoản mặc định qua `DataInitializer`.

```env
ALLOWED_INIT=false  # tắt seed
```

## Ghi chú

- Dự án chưa cấu hình Swagger/OpenAPI
- Dự án sử dụng WebSocket broker cho realtime event; hiện không còn `@MessageMapping` controller kiểu template cũ.
