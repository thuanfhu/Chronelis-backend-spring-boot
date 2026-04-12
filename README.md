# Chronelis Backend (Spring Boot)

Backend chính cho nền tảng cộng tác Chronelis: xác thực JWT + refresh cookie, RBAC bằng role/permission, quản lý workspace/project/task theo thời gian thực và lưu trữ file trên AWS S3.

## 1) Tech Stack

- Java 25
- Spring Boot 4.0.4
- Spring Security (OAuth2 Resource Server)
- Spring Data JPA + MySQL
- Liquibase
- Redis (token blacklist)
- WebSocket STOMP
- AWS SDK v2 S3
- Thymeleaf Mail Templates
- MapStruct + Lombok

## 2) Core Modules

- Auth: register/login/refresh/logout/forgot/reset/verify email
- Users + RBAC: users, roles, permissions, modules
- Collaboration: workspaces, members, invites, teams
- Delivery: projects, goals, task statuses/types/tasks/schedules/comments
- Realtime: notifications + activity logs + websocket events
- Storage: upload/delete/move file qua S3

## 3) Project Structure

- Source: `src/main/java/com/devloopsx/chronelis`
- Config: `src/main/resources/application.yaml`
- Liquibase changelog: `src/main/resources/db/changelog/db.changelog-master.xml`
- Mail templates: `src/main/resources/templates`
- API docs (chi tiết endpoint): `docs/API_DESCRIPTION.md`

## 4) Prerequisites

- JDK 25
- MySQL
- Redis
- AWS S3 credentials (nếu dùng upload file)
- SMTP credentials (nếu dùng email flow)

## 5) Environment Variables

Tạo file `.env` ở root backend dựa trên `.env.example`.

```env
FRONTEND_BASE_URL=http://localhost:5173
ACCOUNT_BASE_PASSWORD=Chronelis123@
ALLOWED_INIT=true

PROJECT_ASSISTANT_ENABLED=false
PROJECT_ASSISTANT_MAX_PREVIEW_ACTIONS=12
PROJECT_ASSISTANT_CONTEXT_GOAL_LIMIT=50
PROJECT_ASSISTANT_CONTEXT_TASK_LIMIT=200
PROJECT_ASSISTANT_CONTEXT_SCHEDULE_LIMIT=150
PROJECT_ASSISTANT_VALIDATION_RETRY_ATTEMPTS=2
PROJECT_ASSISTANT_GOOGLE_VERTEX_AI=false
PROJECT_ASSISTANT_GOOGLE_API_KEY=
PROJECT_ASSISTANT_GOOGLE_PROJECT_ID=
PROJECT_ASSISTANT_GOOGLE_LOCATION=
PROJECT_ASSISTANT_GOOGLE_CREDENTIALS_URI=
PROJECT_ASSISTANT_GOOGLE_MODEL=gemini-2.5-flash
PROJECT_ASSISTANT_GOOGLE_TEMPERATURE=0.15
PROJECT_ASSISTANT_GOOGLE_MAX_OUTPUT_TOKENS=4096

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

Ghi chú cho AI assistant:

- Tat ca cau hinh AI da duoc doc tu bien moi truong, khong can sua source de doi model hay key.
- Frontend khong giu secret AI. API key/model chi nam o backend env.
- Muon bat feature, dat `PROJECT_ASSISTANT_ENABLED=true` va dien bo `PROJECT_ASSISTANT_GOOGLE_*` phu hop.
- Neu dung Gemini API key thong thuong: can `PROJECT_ASSISTANT_GOOGLE_API_KEY`.
- Neu dung Vertex AI: bat `PROJECT_ASSISTANT_GOOGLE_VERTEX_AI=true` va cau hinh `PROJECT_ASSISTANT_GOOGLE_PROJECT_ID`, `PROJECT_ASSISTANT_GOOGLE_LOCATION`, `PROJECT_ASSISTANT_GOOGLE_CREDENTIALS_URI`.

## 6) Run Locally

### Windows

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Backend default: `http://localhost:8080`

## 7) API & Auth Notes

- Base path: `/api/v1`
- API wrapper: `{ success, message, data, meta }`
- Refresh flow dùng cookie `refresh_token` (HttpOnly)
- Public endpoints chỉ thuộc nhóm auth (register/login/verify/refresh/forgot/reset)
- Endpoint còn lại yêu cầu:
    1. JWT hợp lệ
    2. Permission mapping hợp lệ trong DB (`PermissionInterceptor`)

Chi tiết endpoint đầy đủ: `docs/API_DESCRIPTION.md`

## 8) Frontend Link Contracts

Email templates backend sinh link tới frontend theo `FRONTEND_BASE_URL`:

- `/auth/verify-active-account?token=...`
- `/auth/verify-change-email?token=...`
- `/auth/reset-password?token=...`

## 9) WebSocket

- Handshake: `/ws`
- Client publish prefix: `/server`
- Client subscribe prefix: `/client`
- Broker prefixes: `/public`, `/private`

Realtime channels chính:

- `/public/workspaces/{workspaceId}/events`
- `/public/workspaces/{workspaceId}/projects/{projectId}/events`
- `/public/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/events`
- `/user/{userId}/private/notifications`
- `/user/{userId}/private/notifications/unread-count`

## 10) Seed Data

`ALLOWED_INIT=true` sẽ chạy seed data khi khởi động (roles/permissions/accounts ban đầu).

Tắt seed:

```env
ALLOWED_INIT=false
```

## 11) Testing

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```
