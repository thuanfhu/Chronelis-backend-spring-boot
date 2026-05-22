# API DESCRIPTION

## Common Conventions

| Ký hiệu    | Ý nghĩa                                                  |
| ---------- | -------------------------------------------------------- |
| `[PUBLIC]` | Endpoint công khai, **không** cần `Authorization` header |
| `[AUTH]`   | Cần `Authorization: Bearer <access_token>`               |

**Base URL**: `/api/v1`

**Response wrapper chuẩn**:

```json
{
  "success": true,
  "message": "Mô tả kết quả",
  "data": {},
  "meta": {
    "timestamp": "2026-05-22T10:00:00Z",
    "instance": "/api/v1/..."
  }
}
```

**Response lỗi**:

```json
{
  "success": false,
  "errors": [
    {
      "code": 1002,
      "message": "Thông tin đăng nhập không hợp lệ"
    }
  ],
  "meta": {
    "timestamp": "2026-05-22T10:00:00Z",
    "instance": "/api/v1/..."
  }
}
```

**Pagination request params**:

```text
?page=1&size=10&sort=createdAt,desc
```

**Lưu ý**: Backend đang bật `one-indexed-parameters`, nên trang đầu tiên là `page=1`.

**SpringFilter DSL** dùng ở các endpoint có `@Filter`, ví dụ:

```text
?filter=email:'user@gmail.com' and isVerified:true
```

**Date-time formats**:

- Các DTO kế thừa `AuditResponse` như `UserSecureResponse`, `UserResponse`, `RoleResponse`, `PermissionResponse`, `SingleFileResponse` dùng format `yyyy-MM-dd HH:mm:ss a` theo múi giờ `GMT+7`.
- Các DTO collaboration như `WorkspaceResponse`, `ProjectResponse`, `TaskResponse`, `TaskScheduleResponse` dùng ISO-8601 mặc định, ví dụ `2026-05-22T19:30:00`.
- Các field `LocalDate` như `scheduledDate`, `fromDate`, `toDate` dùng format `yyyy-MM-dd`.

---

## Module 1: AUTH (9 APIs)

---

### API-001: Đăng ký tài khoản [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/register`

**Request body**:

```json
{
  "email": "user@gmail.com",
  "phoneNumber": "0912345678",
  "password": "Chronelis123@",
  "confirmPassword": "Chronelis123@",
  "firstName": "Nguyen",
  "lastName": "An"
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `email`           | ✓        | email hợp lệ, chỉ chấp nhận `gmail.com` hoặc `yopmail.com` |
| `phoneNumber`     | ✓        | số điện thoại Việt Nam hợp lệ theo regex backend |
| `password`        | ✓        | ít nhất 8 ký tự, có chữ thường, chữ hoa, số và ký tự đặc biệt trong tập `@$!%*?&` |
| `confirmPassword` | ✓        | cùng rule `password` và phải khớp `password` |
| `firstName`       | ✓        | 2-50 ký tự |
| `lastName`        | ✓        | 2-50 ký tự |

**Response**:

```json
{
  "success": true,
  "message": "Vui lòng kiểm tra email để xác thực tài khoản",
  "data": null
}
```

**Lỗi thường gặp**: `EMAIL_INVALID`, `EMAIL_PROVIDER_INVALID`, `PHONE_NUMBER_VN_INVALID`, `PASSWORD_INVALID_FORMAT`, `EMAIL_EXISTED`, `PHONE_NUMBER_EXISTED`.

---

### API-002: Xác thực email kích hoạt tài khoản [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/verify-active-account`

**Request body**:

```json
{
  "token": "<jwt-token-trong-email>"
}
```

| Field   | Bắt buộc | Validation |
| ------- | -------- | ---------- |
| `token` | ✓        | không được để trống |

**Response** (backend set `refresh_token` HttpOnly cookie):

```json
{
  "success": true,
  "message": "Email của bạn đã được xác thực thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userSecured": {
      "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
      "email": "user@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An",
      "nickname": null,
      "phoneNumber": "0912345678",
      "biography": null,
      "avatarUrl": null,
      "city": null,
      "nationality": null,
      "rolesSecured": [
        {
          "roleId": "USER_ROLE_ID",
          "name": "USER_ROLE",
          "description": "Vai trò mặc định của người dùng",
          "active": true,
          "createdAt": "2026-05-22 07:30:00 PM",
          "updatedAt": "2026-05-22 07:30:00 PM",
          "createdBy": "system",
          "updatedBy": "system"
        }
      ],
      "createdAt": "2026-05-22 07:30:00 PM",
      "updatedAt": "2026-05-22 07:30:00 PM",
      "createdBy": "system",
      "updatedBy": "system"
    }
  }
}
```

**Ghi chú**:

- `accessToken` nằm trong body response.
- `refresh_token` được backend set bằng cookie `HttpOnly`.
- `rolesSecured` chỉ chứa thông tin role bảo mật, không kèm danh sách permission.

---

### API-003: Gửi lại email xác thực [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/resend-verify`

**Request body**:

```json
{
  "email": "user@gmail.com"
}
```

| Field   | Bắt buộc | Validation |
| ------- | -------- | ---------- |
| `email` | ✓        | email hợp lệ, chỉ `gmail.com` hoặc `yopmail.com` |

**Response**:

```json
{
  "success": true,
  "message": "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư đến",
  "data": null
}
```

---

### API-004: Đăng nhập [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/login`

**Request body** (dùng `email` hoặc `phoneNumber`, không dùng cả hai):

```json
{
  "email": "user@gmail.com",
  "password": "Chronelis123@"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `email`       | tùy chọn | đúng format email và đúng domain hỗ trợ nếu truyền |
| `phoneNumber` | tùy chọn | đúng format số điện thoại Việt Nam nếu truyền |
| `password`    | ✓        | không được để trống, phải đúng regex password của backend |

**Response**: `AuthenticationResponse` cùng shape API-002; backend set `refresh_token` HttpOnly cookie.

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userSecured": {
      "...": "cùng cấu trúc API-002"
    }
  }
}
```

**Lỗi thường gặp**: `EMAIL_OR_PHONE_REQUIRED`, `ONLY_EMAIL_OR_PHONE`, `USER_NOT_FOUND`, `PASSWORD_MISMATCH`, `NOT_VERIFIED_ACCOUNT`.

---

### API-005: Đăng xuất [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/auth/logout`

**Request body**: không có.

**Response**:

```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null
}
```

**Ghi chú**:

- Backend xóa cookie `refresh_token`.
- Access token hiện tại được blacklist trong Redis.

---

### API-006: Lấy thông tin tài khoản hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/auth/account`

**Response**:

```json
{
  "success": true,
  "message": "Lấy thông tin người dùng đã xác thực thành công",
  "data": {
    "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
    "email": "user@gmail.com",
    "firstName": "Nguyen",
    "lastName": "An",
    "nickname": "nguyenan",
    "phoneNumber": "0912345678",
    "biography": "Project owner",
    "avatarUrl": "https://cdn.example.com/avatar.jpg",
    "city": "Ho Chi Minh",
    "nationality": "Vietnamese",
    "rolesSecured": [
      {
        "roleId": "USER_ROLE_ID",
        "name": "USER_ROLE",
        "description": "Vai trò mặc định của người dùng",
        "active": true,
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "system",
        "updatedBy": "system"
      }
    ],
    "createdAt": "2026-05-22 07:30:00 PM",
    "updatedAt": "2026-05-22 07:35:00 PM",
    "createdBy": "system",
    "updatedBy": "user@gmail.com"
  }
}
```

**Ghi chú**: `UserSecureResponse` hiện không có field `isVerified`; FE nên dùng đúng shape DTO hiện tại của backend.

---

### API-007: Lấy access token mới từ refresh token [PUBLIC]

- **Method**: `GET`
- **URL**: `/api/v1/auth/refresh`
- **Cookie bắt buộc**: `refresh_token=<refresh-token>`

**Response**:

```json
{
  "success": true,
  "message": "Lấy thành công refresh token và access token",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userSecured": {
      "...": "cùng cấu trúc API-006"
    }
  }
}
```

**Ghi chú**: backend có thể set lại cookie `refresh_token` mới nếu refresh token hợp lệ.

---

### API-008: Quên mật khẩu [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/forgot-password`

**Request body**:

```json
{
  "email": "user@gmail.com"
}
```

| Field   | Bắt buộc | Validation |
| ------- | -------- | ---------- |
| `email` | ✓        | email hợp lệ, đúng domain hỗ trợ |

**Response**:

```json
{
  "success": true,
  "message": "Vui lòng kiểm tra email để đặt lại mật khẩu",
  "data": null
}
```

---

### API-009: Đặt lại mật khẩu [PUBLIC]

- **Method**: `POST`
- **URL**: `/api/v1/auth/reset-password`

**Request body**:

```json
{
  "token": "<jwt-token-trong-email>",
  "newPassword": "NewPassword123@",
  "confirmPassword": "NewPassword123@"
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `token`           | ✓        | không được để trống |
| `newPassword`     | ✓        | cùng regex password mạnh như API-001 |
| `confirmPassword` | ✓        | cùng regex password mạnh, phải khớp `newPassword` |

**Response**:

```json
{
  "success": true,
  "message": "Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập lại",
  "data": null
}
```

---

## Module 2: USERS (10 APIs)

---

### API-010: Cập nhật hồ sơ cá nhân [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/users/update-profile`

**Request body**:

```json
{
  "firstName": "Nguyen",
  "lastName": "An",
  "nickname": "nguyenan",
  "avatarUrl": "https://cdn.example.com/avatar.jpg",
  "biography": "Quản lý sản phẩm",
  "city": "Ho Chi Minh",
  "nationality": "Vietnamese"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `firstName`   | tùy chọn | 2-50 ký tự |
| `lastName`    | tùy chọn | 2-50 ký tự |
| `nickname`    | tùy chọn | 2-50 ký tự |
| `avatarUrl`   | tùy chọn | backend không áp regex ở DTO |
| `biography`   | tùy chọn | backend không áp giới hạn ở DTO |
| `city`        | tùy chọn | backend không áp giới hạn ở DTO |
| `nationality` | tùy chọn | backend không áp giới hạn ở DTO |

**Response**: `UserSecureResponse` cùng cấu trúc API-006, `message = "Cập nhật thông tin người dùng thành công"`.

---

### API-011: Đổi mật khẩu [AUTH]

- **Method**: `PUT`
- **URL**: `/api/v1/users/update-password`

**Request body**:

```json
{
  "currentPassword": "Chronelis123@",
  "newPassword": "NewPassword123@",
  "confirmPassword": "NewPassword123@"
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `currentPassword` | ✓        | không được để trống |
| `newPassword`     | ✓        | không được để trống |
| `confirmPassword` | ✓        | không được để trống |

**Response**: `UserSecureResponse` cùng cấu trúc API-006, `message = "Cập nhật mật khẩu thành công"`.

**Ghi chú**: request DTO hiện chỉ validate `NotBlank`; FE nên áp dụng cùng rule password mạnh như API-001 để đồng nhất UX.

---

### API-012: Yêu cầu đổi email [AUTH]

- **Method**: `PUT`
- **URL**: `/api/v1/users/update-email`

**Request body**:

```json
{
  "newEmail": "newuser@gmail.com"
}
```

| Field      | Bắt buộc | Validation |
| ---------- | -------- | ---------- |
| `newEmail` | ✓        | email hợp lệ, đúng domain hỗ trợ |

**Response**:

```json
{
  "success": true,
  "message": "Vui lòng kiểm tra email mới để xác thực và hoàn tất cập nhật",
  "data": null
}
```

---

### API-013: Xác thực token đổi email [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/users/verify-change-email`

**Request body**:

```json
{
  "token": "<jwt-token-trong-email>"
}
```

**Response**: `UserSecureResponse` cùng cấu trúc API-006.

**Ghi chú**: `message` thực tế là `Địa chỉ email đã được thay đổi thành công. Vui lòng đăng nhập lại`.

---

### API-014: Lấy danh sách người dùng [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/users?page=1&size=20&filter=email:'user@gmail.com'`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách người dùng thành công với bộ lọc truy vấn",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 3,
      "totalElements": 42,
      "hasNext": true,
      "hasPrevious": false
    },
    "content": [
      {
        "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
        "email": "user@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An",
        "nickname": "nguyenan",
        "phoneNumber": "0912345678",
        "biography": "Project owner",
        "avatarUrl": "https://cdn.example.com/avatar.jpg",
        "city": "Ho Chi Minh",
        "nationality": "Vietnamese",
        "rolesSecured": [
          {
            "roleId": "USER_ROLE_ID",
            "name": "USER_ROLE",
            "description": "Vai trò mặc định của người dùng",
            "active": true,
            "createdAt": "2026-05-22 07:30:00 PM",
            "updatedAt": "2026-05-22 07:30:00 PM",
            "createdBy": "system",
            "updatedBy": "system"
          }
        ],
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:35:00 PM",
        "createdBy": "system",
        "updatedBy": "user@gmail.com"
      }
    ]
  }
}
```

**Ghi chú**: hỗ trợ `filter`, `page`, `size`, `sort` như phần Common Conventions.

---

### API-015: Lấy thông tin user theo ID [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/users/{userId}`

**Response**: `UserSecureResponse` cùng cấu trúc API-006, `message = "Lấy thông tin người dùng thành công"`.

---

### API-016: Cập nhật user bởi admin [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/users/{userId}`

**Request body**:

```json
{
  "firstName": "Nguyen",
  "lastName": "B",
  "email": "newadmin@gmail.com",
  "phoneNumber": "0987654321",
  "nickname": "adminb",
  "avatarUrl": "https://cdn.example.com/avatar.jpg",
  "biography": "Admin Chronelis",
  "city": "Ha Noi",
  "nationality": "Vietnamese",
  "isVerified": true,
  "roleIds": ["role-uuid-1"]
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `firstName`   | tùy chọn | 2-50 ký tự |
| `lastName`    | tùy chọn | 2-50 ký tự |
| `email`       | tùy chọn | email hợp lệ, đúng domain hỗ trợ |
| `phoneNumber` | tùy chọn | đúng format số điện thoại Việt Nam |
| `nickname`    | tùy chọn | 2-50 ký tự |
| `avatarUrl`   | tùy chọn | backend không áp regex ở DTO |
| `biography`   | tùy chọn | backend không áp giới hạn ở DTO |
| `city`        | tùy chọn | backend không áp giới hạn ở DTO |
| `nationality` | tùy chọn | backend không áp giới hạn ở DTO |
| `isVerified`  | tùy chọn | boolean |
| `roleIds`     | tùy chọn | mảng `roleId` cần tồn tại |

**Response**:

```json
{
  "success": true,
  "message": "Cập nhật thông tin người dùng thành công",
  "data": {
    "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
    "email": "newadmin@gmail.com",
    "firstName": "Nguyen",
    "lastName": "B",
    "nickname": "adminb",
    "phoneNumber": "0987654321",
    "biography": "Admin Chronelis",
    "avatarUrl": "https://cdn.example.com/avatar.jpg",
    "city": "Ha Noi",
    "nationality": "Vietnamese",
    "isVerified": true,
    "roles": [
      {
        "roleId": "ADMIN_ROLE_ID",
        "name": "ADMIN_ROLE",
        "description": "Vai trò quản trị hệ thống",
        "active": true,
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "system",
        "updatedBy": "system"
      }
    ],
    "createdAt": "2026-05-22 07:30:00 PM",
    "updatedAt": "2026-05-22 07:45:00 PM",
    "createdBy": "system",
    "updatedBy": "admin@gmail.com"
  }
}
```

---

### API-017: Xóa user [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/users/{userId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa tài khoản người dùng thành công",
  "data": null
}
```

---

### API-018: Xóa vai trò khỏi user [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/users/{userId}/roles`

**Request body**:

```json
{
  "roleIds": ["role-uuid-1", "role-uuid-2"]
}
```

| Field     | Bắt buộc | Validation |
| --------- | -------- | ---------- |
| `roleIds` | ✓        | mảng `roleId` cần gửi trong body |

**Response**:

```json
{
  "success": true,
  "message": "Loại bỏ vai trò khỏi người dùng thành công",
  "data": null
}
```

---

### API-019: Yêu cầu nâng cấp sang STAFF [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/users/staff-requests`

**Request body**: không có.

**Response**: `UserSecureResponse` cùng cấu trúc API-006.

**Ghi chú**: `message` thực tế là `Nâng cấp thành công sang vai trò STAFF` và `rolesSecured` sẽ chứa thêm role STAFF nếu thao tác thành công.

---

## Module 3: ROLES (6 APIs)

---

### API-020: Tạo vai trò [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/roles`

**Request body**:

```json
{
  "name": "PROJECT_ADMIN",
  "description": "Quản trị dự án",
  "active": true,
  "permissionIds": ["permission-uuid-1"]
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `name`          | ✓        | không được để trống |
| `description`   | tùy chọn | mô tả vai trò |
| `active`        | tùy chọn | boolean |
| `permissionIds` | tùy chọn | danh sách `permissionId` cần tồn tại |

**Response**:

```json
{
  "success": true,
  "message": "Tạo vai trò thành công",
  "data": {
    "roleId": "ROLE_PROJECT_ADMIN",
    "name": "PROJECT_ADMIN",
    "description": "Quản trị dự án",
    "active": true,
    "permissions": [
      {
        "permissionId": "PERMISSION_TASK_VIEW",
        "name": "Xem task",
        "apiPath": "/api/v1/tasks/{taskId}",
        "httpMethod": "GET",
        "module": "TASKS",
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "system",
        "updatedBy": "system"
      }
    ],
    "createdAt": "2026-05-22 07:30:00 PM",
    "updatedAt": "2026-05-22 07:30:00 PM",
    "createdBy": "admin@gmail.com",
    "updatedBy": "admin@gmail.com"
  }
}
```

---

### API-021: Lấy vai trò theo ID [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/roles/{roleId}`

**Response**: `RoleResponse` cùng cấu trúc API-020, `message = "Lấy thông tin vai trò thành công"`.

---

### API-022: Lấy danh sách vai trò [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/roles?page=1&size=20&filter=name:'PROJECT_ADMIN'`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách vai trò thành công với bộ lọc truy vấn",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 1,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "roleId": "ROLE_PROJECT_ADMIN",
        "name": "PROJECT_ADMIN",
        "description": "Quản trị dự án",
        "active": true,
        "permissions": [
          {
            "permissionId": "PERMISSION_TASK_VIEW",
            "name": "Xem task",
            "apiPath": "/api/v1/tasks/{taskId}",
            "httpMethod": "GET",
            "module": "TASKS",
            "createdAt": "2026-05-22 07:30:00 PM",
            "updatedAt": "2026-05-22 07:30:00 PM",
            "createdBy": "system",
            "updatedBy": "system"
          }
        ],
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "admin@gmail.com",
        "updatedBy": "admin@gmail.com"
      }
    ]
  }
}
```

**Ghi chú**: hỗ trợ `filter`, `page`, `size`, `sort` qua `SpringFilter DSL`.

---

### API-023: Cập nhật vai trò [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/roles/{roleId}`

**Request body**:

```json
{
  "name": "PROJECT_ADMIN_V2",
  "description": "Quản trị dự án nâng cao",
  "active": true,
  "permissionIds": ["permission-uuid-1", "permission-uuid-2"]
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `name`          | tùy chọn | tên vai trò mới |
| `description`   | tùy chọn | mô tả mới |
| `active`        | tùy chọn | boolean |
| `permissionIds` | tùy chọn | danh sách permission gán lại cho role |

**Response**: `RoleResponse` cùng cấu trúc API-020, `message = "Cập nhật vai trò thành công"`.

---

### API-024: Xóa permission khỏi vai trò [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/roles/{roleId}/permissions`

**Request body**:

```json
{
  "permissionIds": ["permission-uuid-1"]
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `permissionIds` | ✓        | danh sách permission cần gỡ khỏi role |

**Response**:

```json
{
  "success": true,
  "message": "Xóa quyền khỏi vai trò thành công",
  "data": null
}
```

---

### API-025: Xóa vai trò [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/roles/{roleId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa vai trò thành công",
  "data": null
}
```

---

## Module 4: PERMISSIONS (8 APIs)

---

### API-026: Tạo module permission [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/permissions/module`

**Request body**:

```json
{
  "moduleName": "TASKS",
  "permissionIds": ["permission-uuid-1", "permission-uuid-2"]
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `moduleName`    | ✓        | không được để trống |
| `permissionIds` | ✓        | danh sách không rỗng, không chứa ID trùng, mọi permission phải tồn tại |

**Response**:

```json
{
  "success": true,
  "message": "Tạo module thành công",
  "data": [
    {
      "permissionId": "PERMISSION_TASK_VIEW",
      "name": "Xem task",
      "apiPath": "/api/v1/tasks/{taskId}",
      "httpMethod": "GET",
      "module": "TASKS",
      "createdAt": "2026-05-22 07:30:00 PM",
      "updatedAt": "2026-05-22 07:35:00 PM",
      "createdBy": "system",
      "updatedBy": "admin@gmail.com"
    },
    {
      "permissionId": "PERMISSION_TASK_CREATE",
      "name": "Tạo task",
      "apiPath": "/api/v1/tasks",
      "httpMethod": "POST",
      "module": "TASKS",
      "createdAt": "2026-05-22 07:30:00 PM",
      "updatedAt": "2026-05-22 07:35:00 PM",
      "createdBy": "system",
      "updatedBy": "admin@gmail.com"
    }
  ]
}
```

**Ghi chú quan trọng**:

- Endpoint này **không tạo entity module riêng**, mà gán cùng một `moduleName` mới cho một nhóm permission có sẵn.
- Backend ép `moduleName` sang chữ hoa trước khi lưu.
- Nếu module đã tồn tại, request sẽ fail với `PERMISSION_MODULE_NAME_EXISTED`.
- Nếu permission đã thuộc module khác, request sẽ fail với `PERMISSION_ALREADY_IN_ANOTHER_MODULE`.

---

### API-027: Xóa module permission theo tên [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/permissions/module/{name}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa module thành công",
  "data": null
}
```

**Ghi chú**: backend chỉ gỡ `module` khỏi các permission trong module đó, **không xóa permission records**.

---

### API-028: Lấy danh sách tên module permission [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/permissions/modules`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách tên module thành công",
  "data": ["AUTH", "USERS", "ROLES", "PERMISSIONS", "WORKSPACES", "PROJECTS", "TASKS"]
}
```

---

### API-029: Tạo permission [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/permissions`

**Request body**:

```json
{
  "name": "Xem task",
  "apiPath": "/api/v1/tasks/{taskId}",
  "httpMethod": "GET",
  "module": "TASKS"
}
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `name`       | ✓        | không được để trống, phải unique |
| `apiPath`    | ✓        | không được để trống |
| `httpMethod` | ✓        | không được để trống, phải là HTTP method hợp lệ |
| `module`     | tùy chọn | nếu truyền thì phải là module đã tồn tại |

**Response**:

```json
{
  "success": true,
  "message": "Tạo quyền thành công",
  "data": {
    "permissionId": "PERMISSION_TASK_VIEW",
    "name": "Xem task",
    "apiPath": "/api/v1/tasks/{taskId}",
    "httpMethod": "GET",
    "module": "TASKS",
    "createdAt": "2026-05-22 07:30:00 PM",
    "updatedAt": "2026-05-22 07:30:00 PM",
    "createdBy": "admin@gmail.com",
    "updatedBy": "admin@gmail.com"
  }
}
```

**Lỗi thường gặp**: `PERMISSION_NAME_EXISTED`, `PERMISSION_PATH_AND_METHOD_EXISTED`, `PERMISSION_MODULE_NOT_FOUND`.

---

### API-030: Cập nhật permission [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/permissions/{permissionId}`

**Request body**:

```json
{
  "name": "Xem task chi tiết",
  "apiPath": "/api/v1/tasks/{taskId}",
  "httpMethod": "GET",
  "module": "TASKS"
}
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `name`       | tùy chọn | tên mới của permission |
| `apiPath`    | tùy chọn | path API mới |
| `httpMethod` | tùy chọn | HTTP method hợp lệ nếu truyền |
| `module`     | tùy chọn | nếu truyền phải là module đã tồn tại |

**Response**: `PermissionResponse` cùng cấu trúc API-029, `message = "Cập nhật quyền thành công"`.

**Ghi chú quan trọng**: theo service hiện tại, nếu `module` không có trong request hoặc là `null`, backend sẽ gỡ permission ra khỏi module hiện tại.

---

### API-031: Lấy permission theo ID [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/permissions/{permissionId}`

**Response**: `PermissionResponse` cùng cấu trúc API-029, `message = "Lấy thông tin quyền thành công"`.

---

### API-032: Lấy danh sách permission [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/permissions?page=1&size=20&filter=module:'TASKS'`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách quyền thành công với bộ lọc truy vấn",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "permissionId": "PERMISSION_TASK_VIEW",
        "name": "Xem task",
        "apiPath": "/api/v1/tasks/{taskId}",
        "httpMethod": "GET",
        "module": "TASKS",
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "admin@gmail.com",
        "updatedBy": "admin@gmail.com"
      },
      {
        "permissionId": "PERMISSION_TASK_CREATE",
        "name": "Tạo task",
        "apiPath": "/api/v1/tasks",
        "httpMethod": "POST",
        "module": "TASKS",
        "createdAt": "2026-05-22 07:30:00 PM",
        "updatedAt": "2026-05-22 07:30:00 PM",
        "createdBy": "admin@gmail.com",
        "updatedBy": "admin@gmail.com"
      }
    ]
  }
}
```

**Ghi chú**: hỗ trợ `filter`, `page`, `size`, `sort` qua `SpringFilter DSL`.

---

### API-033: Xóa permission [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/permissions/{permissionId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa quyền thành công",
  "data": null
}
```

---

## Module 5: STORAGE - Azure Blob Storage (6 APIs)

---

**Giới hạn upload hiện tại từ config**:

- MIME types được chấp nhận: `image/jpeg`, `image/png`, `image/jpg`, `audio/mpeg`.
- Kích thước tối đa mỗi file: `5 MB`.
- Kích thước tối thiểu mỗi file: `1 KB`.
- Nếu không truyền `folderName`, backend dùng thư mục mặc định từ cấu hình hệ thống.

### API-034: Upload một file [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/storage/azure-blob/upload/single`
- **Content-Type**: `multipart/form-data`

**Form data**:

```text
file: <binary>
folderName: "task-notes"
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `file`       | ✓        | không được rỗng; phải đúng MIME type và kích thước cho phép |
| `folderName` | tùy chọn | tên thư mục đích |

**Response**:

```json
{
  "success": true,
  "message": "Tai len tep don thanh cong",
  "data": {
    "fileName": "design.png",
    "fileUrl": "https://storage.example.com/task-notes/design-550e8400-e29b-41d4-a716-446655440000.png"
  }
}
```

**Lỗi thường gặp**: `FILE_NOT_BLANK`, `FILE_TYPE_NOT_ALLOWED`, `FILE_TOO_LARGE`, `FILE_TOO_SMALL`, `INVALID_FILE_NAME`, `FILE_UPLOAD_FAILED`.

---

### API-035: Upload nhiều file [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/storage/azure-blob/upload/multiple`
- **Content-Type**: `multipart/form-data`

**Form data**:

```text
files: <binary file 1>
files: <binary file 2>
folderName: "task-notes"
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `files`      | ✓        | danh sách file hợp lệ; mỗi file phải qua cùng rule như API-034 |
| `folderName` | tùy chọn | tên thư mục đích |

**Response**:

```json
{
  "success": true,
  "message": "Tai len nhieu tep thanh cong",
  "data": {
    "files": [
      {
        "fileName": "a.png",
        "fileUrl": "https://storage.example.com/task-notes/a-550e8400-e29b-41d4-a716-446655440000.png"
      },
      {
        "fileName": "b.mp3",
        "fileUrl": "https://storage.example.com/task-notes/b-550e8400-e29b-41d4-a716-446655440001.mp3"
      }
    ]
  }
}
```

---

### API-036: Xóa một file [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/storage/azure-blob/delete/single?filePath=task-notes/file.png`

**Query params**:

| Param      | Bắt buộc | Validation |
| ---------- | -------- | ---------- |
| `filePath` | ✓        | phải đúng pattern `folderName/fileName.ext` |

**Response**:

```json
{
  "success": true,
  "message": "Xoa tep thanh cong",
  "data": null
}
```

---

### API-037: Xóa nhiều file [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/storage/azure-blob/delete/multiple`

**Request body**:

```json
{
  "filePaths": ["task-notes/a.png", "task-notes/b.png"]
}
```

| Field       | Bắt buộc | Validation |
| ----------- | -------- | ---------- |
| `filePaths` | tùy chọn | danh sách đường dẫn file cần xóa |

**Response**:

```json
{
  "success": true,
  "message": "Xoa nhieu tep thanh cong",
  "data": null
}
```

**Ghi chú**: DTO hiện không gắn annotation validation cho `filePaths`, nhưng backend kỳ vọng danh sách đường dẫn hợp lệ.

---

### API-038: Di chuyển một file [AUTH]

- **Method**: `PUT`
- **URL**: `/api/v1/storage/azure-blob/move/single`
- **Content-Type**: `multipart/form-data` hoặc `application/x-www-form-urlencoded`

**Form data**:

```text
sourceKey: "task-notes/a.png"
destinationFolder: "archive"
```

| Field               | Bắt buộc | Validation |
| ------------------- | -------- | ---------- |
| `sourceKey`         | ✓        | không được để trống |
| `destinationFolder` | ✓        | không được để trống |

**Response**:

```json
{
  "success": true,
  "message": "Di chuyen tep thanh cong",
  "data": "Tep da duoc di chuyen tu: task-notes/a.png den thu muc: archive"
}
```

---

### API-039: Di chuyển nhiều file [AUTH]

- **Method**: `PUT`
- **URL**: `/api/v1/storage/azure-blob/move/multiple`

**Request body**:

```json
{
  "sourceKeys": ["task-notes/a.png", "task-notes/b.png"],
  "destinationFolder": "archive"
}
```

| Field               | Bắt buộc | Validation |
| ------------------- | -------- | ---------- |
| `sourceKeys`        | ✓        | danh sách không rỗng; mỗi phần tử không được để trống |
| `destinationFolder` | ✓        | không được để trống |

**Response**:

```json
{
  "success": true,
  "message": "Di chuyen nhieu tep thanh cong",
  "data": "Cac tep da duoc di chuyen toi thu muc: archive"
}
```

---

## Module 6: WORKSPACES (9 APIs)

---

### API-040: Tạo workspace [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspaces`

**Request body**:

```json
{
  "name": "Growth Team"
}
```

| Field  | Bắt buộc | Validation |
| ------ | -------- | ---------- |
| `name` | ✓        | không được để trống, tối đa 150 ký tự |

**Response**:

```json
{
  "success": true,
  "message": "Tạo workspace thành công",
  "data": {
    "id": 1,
    "name": "Growth Team",
    "owner": {
      "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "createdAt": "2026-05-22T19:30:00",
    "updatedAt": "2026-05-22T19:30:00"
  }
}
```

---

### API-041: Cập nhật workspace [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/workspaces/{workspaceId}`

**Request body**:

```json
{
  "name": "Growth Team Vietnam"
}
```

| Field  | Bắt buộc | Validation |
| ------ | -------- | ---------- |
| `name` | tùy chọn | tối đa 150 ký tự |

**Response**: `WorkspaceResponse` cùng cấu trúc API-040, `message = "Cập nhật workspace thành công"`.

---

### API-042: Lấy chi tiết workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspaces/{workspaceId}`

**Response**: `WorkspaceResponse` cùng cấu trúc API-040, `message = "Lấy chi tiết workspace thành công"`.

---

### API-043: Lấy danh sách workspace user nhìn thấy [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspaces?page=1&size=10&sort=createdAt,desc`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách workspace thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 10,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 1,
        "name": "Growth Team",
        "owner": {
          "userId": "9d9ed4fb-6a68-4d64-8a2f-9a7f2fbb2a20",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "createdAt": "2026-05-22T19:30:00",
        "updatedAt": "2026-05-22T19:30:00"
      }
    ]
  }
}
```

---

### API-044: Thêm thành viên workspace [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspaces/{workspaceId}/members`

**Request body**:

```json
{
  "userId": "user-uuid",
  "role": "MEMBER"
}
```

| Field    | Bắt buộc | Validation |
| -------- | -------- | ---------- |
| `userId` | ✓        | không được để trống |
| `role`   | ✓        | enum `WorkspaceMemberRoleType` |

**Response**:

```json
{
  "success": true,
  "message": "Thêm thành viên workspace thành công",
  "data": {
    "id": 10,
    "workspaceId": 1,
    "user": {
      "userId": "member-uuid",
      "email": "member@gmail.com",
      "firstName": "Tran",
      "lastName": "Binh"
    },
    "role": "MEMBER",
    "joinedAt": "2026-05-22T20:00:00"
  }
}
```

---

### API-045: Lấy danh sách thành viên workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspaces/{workspaceId}/members`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách thành viên workspace thành công",
  "data": [
    {
      "id": 1,
      "workspaceId": 1,
      "user": {
        "userId": "owner-uuid",
        "email": "owner@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
      },
      "role": "OWNER",
      "joinedAt": "2026-05-22T19:30:00"
    },
    {
      "id": 10,
      "workspaceId": 1,
      "user": {
        "userId": "member-uuid",
        "email": "member@gmail.com",
        "firstName": "Tran",
        "lastName": "Binh"
      },
      "role": "MEMBER",
      "joinedAt": "2026-05-22T20:00:00"
    }
  ]
}
```

---

### API-046: Cập nhật vai trò thành viên workspace [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/workspaces/{workspaceId}/members/{userId}/role`

**Request body**:

```json
{
  "role": "MEMBER"
}
```

| Field  | Bắt buộc | Validation |
| ------ | -------- | ---------- |
| `role` | ✓        | enum `WorkspaceMemberRoleType` |

**Response**: `WorkspaceMemberResponse` cùng cấu trúc API-044, `message = "Cập nhật vai trò thành viên thành công"`.

---

### API-047: Xóa thành viên khỏi workspace [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/workspaces/{workspaceId}/members/{userId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa thành viên khỏi workspace thành công",
  "data": null
}
```

---

### API-048: Xóa workspace [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/workspaces/{workspaceId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa workspace thành công",
  "data": null
}
```

---

## Module 7: WORKSPACE INVITES (5 APIs)

---

### API-049: Tạo lời mời workspace [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspace-invites`

**Request body**:

```json
{
  "workspaceId": 1,
  "roleToAssign": "MEMBER",
  "maxUses": 10,
  "expiresAt": "2026-06-01T00:00:00"
}
```

| Field          | Bắt buộc | Validation |
| -------------- | -------- | ---------- |
| `workspaceId`  | ✓        | không được null |
| `roleToAssign` | tùy chọn | enum `WorkspaceMemberRoleType` |
| `maxUses`      | tùy chọn | số lượt sử dụng tối đa |
| `expiresAt`    | tùy chọn | thời điểm hết hạn invite |

**Response**:

```json
{
  "success": true,
  "message": "Tạo lời mời thành công",
  "data": {
    "id": 101,
    "workspaceId": 1,
    "workspaceName": "Growth Team",
    "inviteCode": "ABCD-1234",
    "roleToAssign": "MEMBER",
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "maxUses": 10,
    "usedCount": 0,
    "expiresAt": "2026-06-01T00:00:00",
    "isActive": true,
    "createdAt": "2026-05-22T20:10:00"
  }
}
```

---

### API-050: Lấy danh sách lời mời đang hoạt động [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspace-invites/workspace/{workspaceId}`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách lời mời thành công",
  "data": [
    {
      "id": 101,
      "workspaceId": 1,
      "workspaceName": "Growth Team",
      "inviteCode": "ABCD-1234",
      "roleToAssign": "MEMBER",
      "createdBy": {
        "userId": "owner-uuid",
        "email": "owner@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
      },
      "maxUses": 10,
      "usedCount": 2,
      "expiresAt": "2026-06-01T00:00:00",
      "isActive": true,
      "createdAt": "2026-05-22T20:10:00"
    }
  ]
}
```

---

### API-051: Thu hồi lời mời [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/workspace-invites/{inviteId}/revoke`

**Response**:

```json
{
  "success": true,
  "message": "Thu hồi lời mời thành công",
  "data": null
}
```

---

### API-052: Kiểm tra mã mời [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspace-invites/validate/{inviteCode}`

**Response**: `WorkspaceInviteResponse` cùng cấu trúc API-049, `message = "Mã mời hợp lệ"`.

---

### API-053: Tham gia workspace bằng mã mời [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspace-invites/join`

**Request body**:

```json
{
  "inviteCode": "ABCD-1234"
}
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `inviteCode` | ✓        | không được để trống |

**Response**:

```json
{
  "success": true,
  "message": "Tham gia workspace thành công",
  "data": null
}
```

---

## Module 8: WORKSPACE TEAMS (8 APIs)

---

### API-054: Tạo team trong workspace [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspace-teams`

**Request body**:

```json
{
  "workspaceId": 1,
  "name": "Product Team",
  "description": "Nhóm sản phẩm"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `workspaceId` | ✓        | không được null |
| `name`        | ✓        | không được để trống, tối đa 150 ký tự |
| `description` | tùy chọn | tối đa 2000 ký tự |

**Response**:

```json
{
  "success": true,
  "message": "Tạo team thành công",
  "data": {
    "id": 20,
    "workspaceId": 1,
    "name": "Product Team",
    "description": "Nhóm sản phẩm",
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "memberCount": 0,
    "createdAt": "2026-05-22T20:15:00",
    "updatedAt": "2026-05-22T20:15:00"
  }
}
```

---

### API-055: Cập nhật team [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/workspace-teams/{teamId}`

**Request body**:

```json
{
  "name": "Product Team A",
  "description": "Nhóm sản phẩm khu vực A"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `name`        | tùy chọn | tối đa 150 ký tự |
| `description` | tùy chọn | tối đa 2000 ký tự |

**Response**: `WorkspaceTeamResponse` cùng cấu trúc API-054, `message = "Cập nhật team thành công"`.

---

### API-056: Lấy chi tiết team [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspace-teams/{teamId}`

**Response**: `WorkspaceTeamResponse` cùng cấu trúc API-054, `message = "Lấy chi tiết team thành công"`.

---

### API-057: Lấy danh sách team theo workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspace-teams/workspace/{workspaceId}`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách team theo workspace thành công",
  "data": [
    {
      "id": 20,
      "workspaceId": 1,
      "name": "Product Team",
      "description": "Nhóm sản phẩm",
      "createdBy": {
        "userId": "owner-uuid",
        "email": "owner@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
      },
      "memberCount": 3,
      "createdAt": "2026-05-22T20:15:00",
      "updatedAt": "2026-05-22T20:20:00"
    }
  ]
}
```

---

### API-058: Xóa team [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/workspace-teams/{teamId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa team thành công",
  "data": null
}
```

---

### API-059: Thêm thành viên vào team [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/workspace-teams/{teamId}/members`

**Request body**:

```json
{
  "userId": "user-uuid"
}
```

| Field    | Bắt buộc | Validation |
| -------- | -------- | ---------- |
| `userId` | ✓        | không được để trống |

**Response**:

```json
{
  "success": true,
  "message": "Thêm thành viên vào team thành công",
  "data": {
    "id": 301,
    "teamId": 20,
    "user": {
      "userId": "member-uuid",
      "email": "member@gmail.com",
      "firstName": "Tran",
      "lastName": "Binh"
    },
    "joinedAt": "2026-05-22T20:20:00"
  }
}
```

---

### API-060: Xóa thành viên khỏi team [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/workspace-teams/{teamId}/members/{userId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa thành viên khỏi team thành công",
  "data": null
}
```

---

### API-061: Lấy danh sách thành viên team [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/workspace-teams/{teamId}/members`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách thành viên team thành công",
  "data": [
    {
      "id": 301,
      "teamId": 20,
      "user": {
        "userId": "member-uuid",
        "email": "member@gmail.com",
        "firstName": "Tran",
        "lastName": "Binh"
      },
      "joinedAt": "2026-05-22T20:20:00"
    }
  ]
}
```

---

## Module 9: PROJECTS (7 APIs)

---

### API-062: Tạo project [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/projects`

**Request body**:

```json
{
  "workspaceId": 1,
  "name": "Chronelis Launch",
  "description": "Ra mắt sản phẩm",
  "visibility": "PUBLIC",
  "managerUserId": "user-uuid",
  "managerTeamId": null
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `workspaceId`   | ✓        | không được null |
| `name`          | ✓        | không được để trống, tối đa 150 ký tự |
| `description`   | tùy chọn | tối đa 2000 ký tự |
| `visibility`    | tùy chọn | enum `ProjectVisibilityType` |
| `managerUserId` | tùy chọn | user manager legacy/compatibility field |
| `managerTeamId` | tùy chọn | team manager legacy/compatibility field |

**Response**:

```json
{
  "success": true,
  "message": "Tạo project thành công",
  "data": {
    "id": 100,
    "workspaceId": 1,
    "name": "Chronelis Launch",
    "description": "Ra mắt sản phẩm",
    "status": "ACTIVE",
    "visibility": "PUBLIC",
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "managerUser": {
      "userId": "manager-uuid",
      "email": "manager@gmail.com",
      "firstName": "Linh",
      "lastName": "Pham"
    },
    "managerTeamId": null,
    "managerTeamName": null,
    "createdAt": "2026-05-22T20:30:00",
    "updatedAt": "2026-05-22T20:30:00"
  }
}
```

**Ghi chú**:

- `status` được backend trả trong response dù không bắt buộc phải truyền khi tạo.
- `managerUser` và `managerTeam*` là field tương thích dữ liệu; quyền truy cập thực tế nên đọc thêm ở module `PROJECT ACCESS`.

---

### API-063: Cập nhật project [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/projects/{projectId}`

**Request body**:

```json
{
  "name": "Chronelis Launch V2",
  "description": "Cập nhật phạm vi ra mắt",
  "status": "ACTIVE",
  "visibility": "PRIVATE",
  "managerUserId": "user-uuid",
  "managerTeamId": null
}
```

| Field           | Bắt buộc | Validation |
| --------------- | -------- | ---------- |
| `name`          | tùy chọn | tối đa 150 ký tự |
| `description`   | tùy chọn | tối đa 2000 ký tự |
| `status`        | tùy chọn | enum `ProjectStatusType` |
| `visibility`    | tùy chọn | enum `ProjectVisibilityType` |
| `managerUserId` | tùy chọn | user manager mới |
| `managerTeamId` | tùy chọn | team manager mới |

**Response**: `ProjectResponse` cùng cấu trúc API-062, `message = "Cập nhật project thành công"`.

---

### API-064: Cập nhật trạng thái project [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/projects/{projectId}/status`

**Request body**:

```json
{
  "status": "COMPLETED"
}
```

| Field    | Bắt buộc | Validation |
| -------- | -------- | ---------- |
| `status` | ✓        | enum `ProjectStatusType` |

**Response**: `ProjectResponse` cùng cấu trúc API-062, `message = "Cập nhật trạng thái project thành công"`.

---

### API-065: Lấy chi tiết project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/projects/{projectId}`

**Response**: `ProjectResponse` cùng cấu trúc API-062, `message = "Lấy chi tiết project thành công"`.

---

### API-066: Lấy danh sách project theo workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/projects/workspace/{workspaceId}?page=1&size=20`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách project theo workspace thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 100,
        "workspaceId": 1,
        "name": "Chronelis Launch",
        "description": "Ra mắt sản phẩm",
        "status": "ACTIVE",
        "visibility": "PUBLIC",
        "createdBy": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "managerUser": {
          "userId": "manager-uuid",
          "email": "manager@gmail.com",
          "firstName": "Linh",
          "lastName": "Pham"
        },
        "managerTeamId": null,
        "managerTeamName": null,
        "createdAt": "2026-05-22T20:30:00",
        "updatedAt": "2026-05-22T20:30:00"
      }
    ]
  }
}
```

---

### API-067: Xóa project [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/projects/{projectId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa project thành công",
  "data": null
}
```

**Ghi chú**: theo rule nghiệp vụ collaboration đã chốt, thao tác xóa project là quyền mức cao và không tương đương với mọi manager grant.

---

### API-068: Lấy analytics project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/projects/{projectId}/analytics`

**Response**:

```json
{
  "success": true,
  "message": "Lấy phân tích project thành công",
  "data": {
    "trend": [
      {
        "date": "2026-05-20",
        "created": 5,
        "completed": 1,
        "cumulative": 4
      },
      {
        "date": "2026-05-21",
        "created": 3,
        "completed": 4,
        "cumulative": 3
      }
    ],
    "completionRate": 72.5,
    "totalTasks": 40,
    "completedTasks": 29
  }
}
```

---

## Module 10: PROJECT ACCESS (5 APIs)

---

**Quy ước business quan trọng**:

- `ProjectAccess` lưu grant trực tiếp với các role: `MANAGER`, `CONTRIBUTOR`, `VIEWER`.
- `NO_ACCESS` chỉ xuất hiện ở `effectiveRole`, không phải role lưu trong DB.
- `subjectType` có thể là `USER` hoặc `TEAM`.
- Project `PUBLIC` và `PRIVATE` có hành vi quyền khác nhau; FE nên ưu tiên đọc API `access/me` để quyết định UI actions.

### API-069: Lấy danh sách quyền truy cập project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/projects/{projectId}/access`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách quyền truy cập project thành công",
  "data": [
    {
      "id": 900,
      "projectId": 100,
      "subjectType": "USER",
      "user": {
        "userId": "manager-uuid",
        "email": "manager@gmail.com",
        "firstName": "Linh",
        "lastName": "Pham"
      },
      "team": null,
      "role": "MANAGER",
      "grantedBy": {
        "userId": "owner-uuid",
        "email": "owner@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
      },
      "createdAt": "2026-05-22T20:35:00",
      "updatedAt": "2026-05-22T20:35:00"
    },
    {
      "id": 901,
      "projectId": 100,
      "subjectType": "TEAM",
      "user": null,
      "team": {
        "id": 20,
        "workspaceId": 1,
        "name": "Product Team",
        "description": "Nhóm sản phẩm",
        "createdBy": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "memberCount": 3,
        "createdAt": "2026-05-22T20:15:00",
        "updatedAt": "2026-05-22T20:20:00"
      },
      "role": "CONTRIBUTOR",
      "grantedBy": {
        "userId": "owner-uuid",
        "email": "owner@gmail.com",
        "firstName": "Nguyen",
        "lastName": "An"
      },
      "createdAt": "2026-05-22T20:40:00",
      "updatedAt": "2026-05-22T20:40:00"
    }
  ]
}
```

---

### API-070: Lấy quyền hiệu lực của user hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/projects/{projectId}/access/me`

**Response**:

```json
{
  "success": true,
  "message": "Lấy quyền hiệu lực project thành công",
  "data": {
    "projectId": 100,
    "workspaceId": 1,
    "visibility": "PRIVATE",
    "effectiveRole": "MANAGER",
    "workspaceOwner": false,
    "canViewProject": true,
    "canContribute": true,
    "canComment": true,
    "canManageProjectWork": true,
    "canManageProjectAccess": true,
    "canGrantManager": false,
    "canRevokeManager": false,
    "canManageManagerAccess": false,
    "canChangeVisibility": false,
    "canDeleteProject": false,
    "canAssignOthers": true,
    "canManageWorkspaceMembers": false,
    "canManageWorkspaceTeams": false,
    "canManageWorkspaceInvites": false,
    "canManageWorkspaceSettings": false
  }
}
```

**Ghi chú quan trọng**:

- `effectiveRole` có thể là `MANAGER`, `CONTRIBUTOR`, `VIEWER`, `NO_ACCESS`.
- UI nên dùng các cờ boolean `can*` làm nguồn quyết định chính cho actions/hide-disable button.
- Theo rule quyền đã chốt, workspace owner có authority cao hơn manager grant thông thường, đặc biệt cho thao tác đổi visibility, xóa project, và grant/revoke manager.

---

### API-071: Cấp hoặc cập nhật quyền project [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/projects/{projectId}/access`

**Request body**:

```json
{
  "subjectType": "USER",
  "userId": "user-uuid",
  "teamId": null,
  "role": "CONTRIBUTOR"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `subjectType` | ✓        | enum `ProjectAccessSubjectType` |
| `userId`      | tùy chọn | dùng khi `subjectType = USER` |
| `teamId`      | tùy chọn | dùng khi `subjectType = TEAM` |
| `role`        | ✓        | enum `ProjectAccessRoleType` |

**Response**: `ProjectAccessResponse` cùng cấu trúc API-069, `message = "Cập nhật quyền truy cập project thành công"`.

**Ghi chú**:

- Endpoint này là `upsert`: có thể tạo mới grant hoặc cập nhật grant hiện có cho cùng subject.
- FE nên gửi đúng cặp `subjectType` + `userId/teamId`; không nên gửi đồng thời cả `userId` và `teamId` có giá trị.

---

### API-072: Cập nhật vai trò trong quyền project [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/projects/{projectId}/access/{accessId}`

**Request body**:

```json
{
  "role": "VIEWER"
}
```

| Field  | Bắt buộc | Validation |
| ------ | -------- | ---------- |
| `role` | ✓        | enum `ProjectAccessRoleType` |

**Response**: `ProjectAccessResponse` cùng cấu trúc API-069, `message = "Cập nhật quyền truy cập project thành công"`.

---

### API-073: Thu hồi quyền project [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/projects/{projectId}/access/{accessId}`

**Response**:

```json
{
  "success": true,
  "message": "Thu hồi quyền truy cập project thành công",
  "data": null
}
```

---

## Module 11: GOALS (5 APIs)

---

### API-074: Tạo goal [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/goals`

**Request body**:

```json
{
  "projectId": 1,
  "title": "Ra mắt bản beta",
  "goalType": "SHORT_TERM",
  "status": "NOT_STARTED",
  "progressPercent": 0,
  "managerUserId": "user-uuid",
  "managerTeamId": null
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `projectId`       | ✓        | không được null |
| `title`           | ✓        | không được để trống, tối đa 200 ký tự |
| `goalType`        | ✓        | enum `GoalType` |
| `status`          | tùy chọn | enum `GoalStatusType` |
| `progressPercent` | tùy chọn | từ `0.00` đến `100.00` |
| `managerUserId`   | tùy chọn | user manager của goal |
| `managerTeamId`   | tùy chọn | team manager của goal |

**Response**:

```json
{
  "success": true,
  "message": "Tạo goal thành công",
  "data": {
    "id": 500,
    "projectId": 100,
    "title": "Ra mắt bản beta",
    "goalType": "SHORT_TERM",
    "status": "NOT_STARTED",
    "progressPercent": 0,
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "managerUser": {
      "userId": "manager-uuid",
      "email": "manager@gmail.com",
      "firstName": "Linh",
      "lastName": "Pham"
    },
    "managerTeamId": null,
    "managerTeamName": null,
    "createdAt": "2026-05-22T20:45:00",
    "updatedAt": "2026-05-22T20:45:00"
  }
}
```

---

### API-075: Cập nhật goal [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/goals/{goalId}`

**Request body**:

```json
{
  "title": "Ra mắt bản beta công khai",
  "goalType": "MEDIUM_TERM",
  "status": "IN_PROGRESS",
  "progressPercent": 45,
  "managerUserId": "user-uuid",
  "managerTeamId": null
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `title`           | tùy chọn | tối đa 200 ký tự |
| `goalType`        | tùy chọn | enum `GoalType` |
| `status`          | tùy chọn | enum `GoalStatusType` |
| `progressPercent` | tùy chọn | từ `0.00` đến `100.00` |
| `managerUserId`   | tùy chọn | user manager mới |
| `managerTeamId`   | tùy chọn | team manager mới |

**Response**: `GoalResponse` cùng cấu trúc API-074, `message = "Cập nhật goal thành công"`.

---

### API-076: Lấy chi tiết goal [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/goals/{goalId}`

**Response**: `GoalResponse` cùng cấu trúc API-074, `message = "Lấy chi tiết goal thành công"`.

---

### API-077: Lấy danh sách goal theo project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/goals/project/{projectId}?page=1&size=20`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách goal theo project thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 1,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 500,
        "projectId": 100,
        "title": "Ra mắt bản beta",
        "goalType": "SHORT_TERM",
        "status": "NOT_STARTED",
        "progressPercent": 0,
        "createdBy": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "managerUser": {
          "userId": "manager-uuid",
          "email": "manager@gmail.com",
          "firstName": "Linh",
          "lastName": "Pham"
        },
        "managerTeamId": null,
        "managerTeamName": null,
        "createdAt": "2026-05-22T20:45:00",
        "updatedAt": "2026-05-22T20:45:00"
      }
    ]
  }
}
```

---

### API-078: Xóa goal [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/goals/{goalId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa goal thành công",
  "data": null
}
```

---

## Module 12: TASK STATUSES (5 APIs)

---

### API-079: Tạo task status [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/task-statuses`

**Request body**:

```json
{
  "projectId": 1,
  "name": "Đang làm",
  "code": "IN_PROGRESS",
  "position": 2,
  "isClosed": false
}
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `projectId`  | ✓        | không được null |
| `name`       | ✓        | không được để trống, tối đa 100 ký tự |
| `code`       | ✓        | không được để trống, tối đa 50 ký tự |
| `position`   | tùy chọn | vị trí cột trên board |
| `isClosed`   | tùy chọn | boolean |

**Response**:

```json
{
  "success": true,
  "message": "Tạo cột Kanban thành công",
  "data": {
    "id": 1,
    "projectId": 100,
    "name": "Đang làm",
    "code": "IN_PROGRESS",
    "position": 2,
    "isClosed": false,
    "createdAt": "2026-05-22T21:00:00"
  }
}
```

---

### API-080: Lấy danh sách status theo project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-statuses/project/{projectId}`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách cột Kanban thành công",
  "data": [
    {
      "id": 1,
      "projectId": 100,
      "name": "Việc cần làm",
      "code": "TODO",
      "position": 1,
      "isClosed": false,
      "createdAt": "2026-05-22T21:00:00"
    },
    {
      "id": 2,
      "projectId": 100,
      "name": "Đang làm",
      "code": "IN_PROGRESS",
      "position": 2,
      "isClosed": false,
      "createdAt": "2026-05-22T21:00:00"
    }
  ]
}
```

---

### API-081: Cập nhật task status [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/task-statuses/{statusId}`

**Request body**:

```json
{
  "name": "Hoàn thành",
  "code": "DONE",
  "position": 4,
  "isClosed": true
}
```

| Field      | Bắt buộc | Validation |
| ---------- | -------- | ---------- |
| `name`     | tùy chọn | tối đa 100 ký tự |
| `code`     | tùy chọn | tối đa 50 ký tự |
| `position` | tùy chọn | vị trí mới |
| `isClosed` | tùy chọn | boolean |

**Response**: `TaskStatusResponse` cùng cấu trúc API-079, `message = "Cập nhật cột Kanban thành công"`.

---

### API-082: Sắp xếp status trong project [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/task-statuses/project/{projectId}/reorder`

**Request body**:

```json
{
  "statusIdsInOrder": [1, 2, 3, 4]
}
```

| Field              | Bắt buộc | Validation |
| ------------------ | -------- | ---------- |
| `statusIdsInOrder` | ✓        | danh sách không rỗng |

**Response**: danh sách `TaskStatusResponse`, `message = "Reorder cột Kanban thành công"`.

---

### API-083: Xóa task status [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/task-statuses/{statusId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa cột Kanban thành công",
  "data": null
}
```

---

## Module 13: TASK TYPES (5 APIs)

---

### API-084: Tạo task type [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/task-types`

**Request body**:

```json
{
  "workspaceId": 1,
  "projectId": 1,
  "goalId": 1,
  "name": "Bug",
  "description": "Lỗi cần xử lý",
  "color": "#EF4444",
  "icon": "bug"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `workspaceId` | ✓        | không được null |
| `projectId`   | ✓        | không được null |
| `goalId`      | tùy chọn | goal liên kết nếu có |
| `name`        | ✓        | không được để trống, tối đa 100 ký tự |
| `description` | tùy chọn | tối đa 2000 ký tự |
| `color`       | tùy chọn | phải đúng hex `#RRGGBB` |
| `icon`        | tùy chọn | tối đa 50 ký tự |

**Response**:

```json
{
  "success": true,
  "message": "Tạo task type thành công",
  "data": {
    "id": 11,
    "workspaceId": 1,
    "projectId": 100,
    "goalId": 500,
    "name": "Bug",
    "description": "Lỗi cần xử lý",
    "color": "#EF4444",
    "icon": "bug",
    "createdAt": "2026-05-22T21:05:00",
    "updatedAt": "2026-05-22T21:05:00"
  }
}
```

---

### API-085: Cập nhật task type [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/task-types/{taskTypeId}`

**Request body**:

```json
{
  "name": "Feature",
  "description": "Tính năng mới",
  "goalId": 1,
  "clearGoal": false,
  "color": "#3B82F6",
  "icon": "sparkles"
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `name`        | tùy chọn | tối đa 100 ký tự |
| `description` | tùy chọn | tối đa 2000 ký tự |
| `goalId`      | tùy chọn | goal liên kết mới |
| `clearGoal`   | tùy chọn | nếu `true` thì gỡ liên kết goal |
| `color`       | tùy chọn | phải đúng hex `#RRGGBB` |
| `icon`        | tùy chọn | tối đa 50 ký tự |

**Response**: `TaskTypeResponse` cùng cấu trúc API-084, `message = "Cập nhật task type thành công"`.

---

### API-086: Lấy chi tiết task type [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-types/{taskTypeId}`

**Response**: `TaskTypeResponse` cùng cấu trúc API-084, `message = "Lấy chi tiết task type thành công"`.

---

### API-087: Lấy danh sách task type theo project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-types/project/{projectId}`

**Response**: danh sách `TaskTypeResponse`, `message = "Lấy danh sách task type theo project thành công"`.

---

### API-088: Xóa task type [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/task-types/{taskTypeId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa task type thành công",
  "data": null
}
```

---

## Module 14: TASKS (14 APIs)

---

### API-089: Tạo task [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/tasks`

**Request body**:

```json
{
  "projectId": 1,
  "goalId": 1,
  "statusId": 1,
  "title": "Thiết kế onboarding",
  "description": "Tạo luồng bắt đầu nhanh cho dashboard",
  "notesHtml": "<p>Ghi chú</p>",
  "priority": "HIGH",
  "assigneeId": "user-uuid",
  "dueDate": "2026-05-30T18:00:00",
  "estimatedMinutes": 120,
  "boardPosition": 1,
  "taskTypeId": 1,
  "sourceView": "KANBAN"
}
```

| Field              | Bắt buộc | Validation |
| ------------------ | -------- | ---------- |
| `projectId`        | ✓        | không được null |
| `goalId`           | tùy chọn | goal liên kết |
| `statusId`         | ✓        | không được null |
| `title`            | ✓        | không được để trống, tối đa 200 ký tự |
| `description`      | tùy chọn | tối đa 5000 ký tự |
| `notesHtml`        | tùy chọn | tối đa 200000 ký tự |
| `priority`         | ✓        | enum `TaskPriorityType` |
| `assigneeId`       | tùy chọn | user phụ trách |
| `dueDate`          | tùy chọn | `LocalDateTime` |
| `estimatedMinutes` | tùy chọn | >= 0 |
| `boardPosition`    | tùy chọn | vị trí card trong cột |
| `taskTypeId`       | tùy chọn | task type liên kết |
| `sourceView`       | tùy chọn | enum `SourceViewType` |

**Response**:

```json
{
  "success": true,
  "message": "Tạo task thành công",
  "data": {
    "id": 700,
    "workspaceId": 1,
    "projectId": 100,
    "goalId": 500,
    "status": {
      "id": 2,
      "projectId": 100,
      "name": "Đang làm",
      "code": "IN_PROGRESS",
      "position": 2,
      "isClosed": false,
      "createdAt": "2026-05-22T21:00:00"
    },
    "title": "Thiết kế onboarding",
    "description": "Tạo luồng bắt đầu nhanh cho dashboard",
    "notesHtml": "<p>Ghi chú</p>",
    "priority": "HIGH",
    "taskType": {
      "id": 11,
      "workspaceId": 1,
      "projectId": 100,
      "goalId": 500,
      "name": "Bug",
      "description": "Lỗi cần xử lý",
      "color": "#EF4444",
      "icon": "bug",
      "createdAt": "2026-05-22T21:05:00",
      "updatedAt": "2026-05-22T21:05:00"
    },
    "sourceView": "KANBAN",
    "assignee": {
      "userId": "assignee-uuid",
      "email": "member@gmail.com",
      "firstName": "Tran",
      "lastName": "Binh"
    },
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "dueDate": "2026-05-30T18:00:00",
    "estimatedMinutes": 120,
    "boardPosition": 1,
    "blockerNote": null,
    "blocked": false,
    "blockedReason": null,
    "blockedByOpenCount": 0,
    "blockingTaskCount": 0,
    "isCompleted": false,
    "completedAt": null,
    "createdAt": "2026-05-22T21:10:00",
    "updatedAt": "2026-05-22T21:10:00"
  }
}
```

---

### API-090: Cập nhật task [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{taskId}`

**Request body**:

```json
{
  "title": "Thiết kế onboarding dashboard",
  "description": "Cập nhật nội dung",
  "notesHtml": "<p>Nội dung mới</p>",
  "goalId": 1,
  "clearGoal": false,
  "priority": "URGENT",
  "dueDate": "2026-05-31T18:00:00",
  "estimatedMinutes": 180,
  "taskTypeId": 1
}
```

| Field              | Bắt buộc | Validation |
| ------------------ | -------- | ---------- |
| `title`            | tùy chọn | tối đa 200 ký tự |
| `description`      | tùy chọn | tối đa 5000 ký tự |
| `notesHtml`        | tùy chọn | tối đa 200000 ký tự |
| `goalId`           | tùy chọn | goal mới |
| `clearGoal`        | tùy chọn | nếu `true` thì gỡ goal khỏi task |
| `priority`         | tùy chọn | enum `TaskPriorityType` |
| `dueDate`          | tùy chọn | `LocalDateTime` |
| `estimatedMinutes` | tùy chọn | >= 0 |
| `taskTypeId`       | tùy chọn | task type mới |

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Cập nhật task thành công"`.

---

### API-091: Lấy chi tiết task [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/{taskId}`

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Lấy chi tiết task thành công"`.

---

### API-092: Lấy công việc của user hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/my-work`

**Response**:

```json
{
  "success": true,
  "message": "Lấy trung tâm công việc cá nhân thành công",
  "data": {
    "assignedCount": 8,
    "blockedCount": 1,
    "overdueCount": 2,
    "dueTodayCount": 1,
    "highPriorityCount": 3,
    "upcomingScheduledCount": 2,
    "assignedTasks": [
      {
        "id": 700,
        "workspaceId": 1,
        "projectId": 100,
        "goalId": 500,
        "status": {
          "id": 2,
          "projectId": 100,
          "name": "Đang làm",
          "code": "IN_PROGRESS",
          "position": 2,
          "isClosed": false,
          "createdAt": "2026-05-22T21:00:00"
        },
        "title": "Thiết kế onboarding",
        "description": "Tạo luồng bắt đầu nhanh cho dashboard",
        "notesHtml": "<p>Ghi chú</p>",
        "priority": "HIGH",
        "taskType": null,
        "sourceView": "KANBAN",
        "assignee": {
          "userId": "assignee-uuid",
          "email": "member@gmail.com",
          "firstName": "Tran",
          "lastName": "Binh"
        },
        "createdBy": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "dueDate": "2026-05-30T18:00:00",
        "estimatedMinutes": 120,
        "boardPosition": 1,
        "blockerNote": null,
        "blocked": false,
        "blockedReason": null,
        "blockedByOpenCount": 0,
        "blockingTaskCount": 0,
        "isCompleted": false,
        "completedAt": null,
        "createdAt": "2026-05-22T21:10:00",
        "updatedAt": "2026-05-22T21:10:00"
      }
    ],
    "upcomingSchedules": [
      {
        "scheduleId": 801,
        "taskId": 700,
        "scheduledStart": "2026-05-23T09:00:00",
        "scheduledEnd": "2026-05-23T11:00:00",
        "task": {
          "id": 700,
          "workspaceId": 1,
          "projectId": 100,
          "goalId": 500,
          "status": {
            "id": 2,
            "projectId": 100,
            "name": "Đang làm",
            "code": "IN_PROGRESS",
            "position": 2,
            "isClosed": false,
            "createdAt": "2026-05-22T21:00:00"
          },
          "title": "Thiết kế onboarding",
          "description": "Tạo luồng bắt đầu nhanh cho dashboard",
          "notesHtml": "<p>Ghi chú</p>",
          "priority": "HIGH",
          "taskType": null,
          "sourceView": "KANBAN",
          "assignee": {
            "userId": "assignee-uuid",
            "email": "member@gmail.com",
            "firstName": "Tran",
            "lastName": "Binh"
          },
          "createdBy": {
            "userId": "owner-uuid",
            "email": "owner@gmail.com",
            "firstName": "Nguyen",
            "lastName": "An"
          },
          "dueDate": "2026-05-30T18:00:00",
          "estimatedMinutes": 120,
          "boardPosition": 1,
          "blockerNote": null,
          "blocked": false,
          "blockedReason": null,
          "blockedByOpenCount": 0,
          "blockingTaskCount": 0,
          "isCompleted": false,
          "completedAt": null,
          "createdAt": "2026-05-22T21:10:00",
          "updatedAt": "2026-05-22T21:10:00"
        }
      }
    ],
    "generatedAt": "2026-05-22T21:15:00"
  }
}
```

---

### API-093: Lấy analytics task của user hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/analytics`

**Response**:

```json
{
  "success": true,
  "message": "Lấy phân tích task thành công",
  "data": {
    "trend": [
      {
        "date": "2026-05-20",
        "created": 4,
        "completed": 1
      },
      {
        "date": "2026-05-21",
        "created": 3,
        "completed": 2
      }
    ],
    "estimatedByPriority": [
      {
        "priority": "HIGH",
        "totalMinutes": 360,
        "taskCount": 3
      },
      {
        "priority": "MEDIUM",
        "totalMinutes": 240,
        "taskCount": 4
      }
    ],
    "totalAssigned": 8,
    "totalCompleted": 5
  }
}
```

---

### API-094: Lấy dependency của task [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/{taskId}/dependencies`

**Response**:

```json
{
  "success": true,
  "message": "Lấy phụ thuộc task thành công",
  "data": {
    "taskId": 700,
    "blockerNote": "Cần hoàn tất thiết kế trước",
    "blocked": true,
    "blockedReason": "Có task phụ thuộc chưa hoàn thành",
    "blockedByOpenCount": 2,
    "blockingTaskCount": 1,
    "blockedByTasks": [
      {
        "id": 650,
        "projectId": 100,
        "goalId": 500,
        "title": "Thiết kế wireframe",
        "statusName": "Đang làm",
        "statusCode": "IN_PROGRESS",
        "priority": "HIGH",
        "dueDate": "2026-05-29T18:00:00",
        "completed": false
      }
    ],
    "blockingTasks": [
      {
        "id": 750,
        "projectId": 100,
        "goalId": 500,
        "title": "Tạo UI onboarding",
        "statusName": "Việc cần làm",
        "statusCode": "TODO",
        "priority": "MEDIUM",
        "dueDate": "2026-06-02T18:00:00",
        "completed": false
      }
    ]
  }
}
```

---

### API-095: Cập nhật dependency của task [AUTH]

- **Method**: `PUT`
- **URL**: `/api/v1/tasks/{taskId}/dependencies`

**Request body**:

```json
{
  "dependencyTaskIds": [2, 3],
  "blockerNote": "Cần hoàn tất thiết kế trước"
}
```

| Field               | Bắt buộc | Validation |
| ------------------- | -------- | ---------- |
| `dependencyTaskIds` | tùy chọn | danh sách task phụ thuộc |
| `blockerNote`       | tùy chọn | tối đa 1000 ký tự |

**Response**: `TaskDependencyDetailsResponse` cùng cấu trúc API-094, `message = "Cập nhật phụ thuộc task thành công"`.

---

### API-096: Lấy danh sách task theo project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/project/{projectId}?page=1&size=50`

**Response**: `PaginationResponse<TaskResponse>` dùng item cùng cấu trúc API-089, `message = "Lấy danh sách task theo project thành công"`.

---

### API-097: Lấy danh sách task theo goal [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/tasks/goal/{goalId}?page=1&size=50`

**Response**: `PaginationResponse<TaskResponse>` dùng item cùng cấu trúc API-089, `message = "Lấy danh sách task theo goal thành công"`.

---

### API-098: Di chuyển task sang status khác [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{taskId}/move`

**Request body**:

```json
{
  "statusId": 2,
  "targetPosition": 1
}
```

| Field            | Bắt buộc | Validation |
| ---------------- | -------- | ---------- |
| `statusId`       | ✓        | không được null |
| `targetPosition` | tùy chọn | vị trí mong muốn trong status mới |

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Di chuyển task thành công"`.

---

### API-099: Sắp xếp lại task trong status [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{taskId}/reorder`

**Request body**:

```json
{
  "targetPosition": 3
}
```

| Field            | Bắt buộc | Validation |
| ---------------- | -------- | ---------- |
| `targetPosition` | ✓        | không được null |

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Reorder task thành công"`.

---

### API-100: Gán hoặc bỏ gán người phụ trách task [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{taskId}/assignee`

**Request body**:

```json
{
  "assigneeId": "user-uuid"
}
```

| Field        | Bắt buộc | Validation |
| ------------ | -------- | ---------- |
| `assigneeId` | tùy chọn | truyền `null` hoặc bỏ field để bỏ gán tùy cách FE gửi |

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Cập nhật người phụ trách task thành công"`.

---

### API-101: Cập nhật trạng thái hoàn thành task [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/tasks/{taskId}/completion`

**Request body**:

```json
{
  "isCompleted": true
}
```

| Field         | Bắt buộc | Validation |
| ------------- | -------- | ---------- |
| `isCompleted` | ✓        | boolean |

**Response**: `TaskResponse` cùng cấu trúc API-089, `message = "Cập nhật trạng thái hoàn thành task thành công"`.

---

### API-102: Xóa task [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/tasks/{taskId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa task thành công",
  "data": null
}
```

---

## Module 15: TASK SCHEDULES (6 APIs)

---

### API-103: Tạo lịch cho task [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/task-schedules`

**Request body**:

```json
{
  "taskId": 1,
  "scheduledStart": "2026-05-23T09:00:00",
  "scheduledEnd": "2026-05-23T11:00:00"
}
```

| Field            | Bắt buộc | Validation |
| ---------------- | -------- | ---------- |
| `taskId`         | ✓        | không được null |
| `scheduledStart` | ✓        | `LocalDateTime` |
| `scheduledEnd`   | ✓        | `LocalDateTime` |

**Response**:

```json
{
  "success": true,
  "message": "Tạo lịch task thành công",
  "data": {
    "id": 801,
    "taskId": 700,
    "scheduledStart": "2026-05-23T09:00:00",
    "scheduledEnd": "2026-05-23T11:00:00",
    "scheduledDate": "2026-05-23",
    "createdBy": {
      "userId": "owner-uuid",
      "email": "owner@gmail.com",
      "firstName": "Nguyen",
      "lastName": "An"
    },
    "createdAt": "2026-05-22T21:20:00",
    "updatedAt": "2026-05-22T21:20:00"
  }
}
```

---

### API-104: Cập nhật lịch task [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/task-schedules/{scheduleId}`

**Request body**:

```json
{
  "scheduledStart": "2026-05-23T10:00:00",
  "scheduledEnd": "2026-05-23T12:00:00"
}
```

| Field            | Bắt buộc | Validation |
| ---------------- | -------- | ---------- |
| `scheduledStart` | ✓        | `LocalDateTime` |
| `scheduledEnd`   | ✓        | `LocalDateTime` |

**Response**: `TaskScheduleResponse` cùng cấu trúc API-103, `message = "Cập nhật lịch task thành công"`.

---

### API-105: Xóa lịch task [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/task-schedules/{scheduleId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa lịch task thành công",
  "data": null
}
```

---

### API-106: Lấy lịch theo task [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-schedules/task/{taskId}`

**Response**: danh sách `TaskScheduleResponse`, `message = "Lấy danh sách lịch theo task thành công"`.

---

### API-107: Lấy calendar theo project [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-schedules/calendar/project/{projectId}?fromDate=2026-05-01&toDate=2026-05-31&page=1&size=100`

**Query params**:

| Param      | Bắt buộc | Ghi chú                |
| ---------- | -------- | ---------------------- |
| `fromDate` | ✓        | ISO date `yyyy-MM-dd`  |
| `toDate`   | ✓        | ISO date `yyyy-MM-dd`  |
| `page`     |          | one-indexed pagination |
| `size`     |          | số phần tử mỗi trang   |

**Response**:

```json
{
  "success": true,
  "message": "Lấy dữ liệu lịch theo project thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 100,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 801,
        "taskId": 700,
        "scheduledStart": "2026-05-23T09:00:00",
        "scheduledEnd": "2026-05-23T11:00:00",
        "scheduledDate": "2026-05-23",
        "createdBy": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "createdAt": "2026-05-22T21:20:00",
        "updatedAt": "2026-05-22T21:20:00"
      }
    ]
  }
}
```

---

### API-108: Lấy calendar theo workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-schedules/calendar/workspace/{workspaceId}?fromDate=2026-05-01&toDate=2026-05-31&page=1&size=100`

**Response**: `PaginationResponse<TaskScheduleResponse>` dùng item cùng cấu trúc API-103, `message = "Lấy dữ liệu lịch theo workspace thành công"`.

---

## Module 16: TASK COMMENTS (4 APIs)

---

### API-109: Tạo comment task [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/task-comments`

**Request body**:

```json
{
  "taskId": 1,
  "parentCommentId": null,
  "content": "Mình đã cập nhật phần thiết kế."
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `taskId`          | ✓        | không được null |
| `parentCommentId` | tùy chọn | `null` nếu là comment gốc |
| `content`         | ✓        | không được để trống |

**Response**:

```json
{
  "success": true,
  "message": "Thêm bình luận thành công",
  "data": {
    "id": 901,
    "taskId": 700,
    "parentCommentId": null,
    "user": {
      "userId": "member-uuid",
      "email": "member@gmail.com",
      "firstName": "Tran",
      "lastName": "Binh"
    },
    "content": "Mình đã cập nhật phần thiết kế.",
    "createdAt": "2026-05-22T21:30:00",
    "updatedAt": "2026-05-22T21:30:00"
  }
}
```

**Ghi chú**: theo rule collaboration hiện tại, `MANAGER` và `CONTRIBUTOR` có thể tạo comment; `VIEWER` chỉ xem comment.

---

### API-110: Cập nhật comment task [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/task-comments/{commentId}`

**Request body**:

```json
{
  "content": "Nội dung comment đã chỉnh sửa."
}
```

| Field     | Bắt buộc | Validation |
| --------- | -------- | ---------- |
| `content` | ✓        | không được để trống |

**Response**: `TaskCommentResponse` cùng cấu trúc API-109, `message = "Cập nhật bình luận thành công"`.

---

### API-111: Xóa comment task [AUTH]

- **Method**: `DELETE`
- **URL**: `/api/v1/task-comments/{commentId}`

**Response**:

```json
{
  "success": true,
  "message": "Xóa bình luận thành công",
  "data": null
}
```

---

### API-112: Lấy comment theo task [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/task-comments/task/{taskId}`

**Response**: danh sách `TaskCommentResponse`, `message = "Lấy danh sách bình luận theo task thành công"`.

---

## Module 17: NOTIFICATIONS (4 APIs)

---

### API-113: Lấy danh sách thông báo của user hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/notifications?page=1&size=20`

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách thông báo thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 1001,
        "type": "TASK_ASSIGNED",
        "title": "Bạn được giao task mới",
        "message": "Task Thiết kế onboarding đã được giao cho bạn",
        "referenceType": "TASK",
        "referenceId": 700,
        "isRead": false,
        "createdAt": "2026-05-22T21:35:00"
      }
    ]
  }
}
```

---

### API-114: Lấy số lượng thông báo chưa đọc [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/notifications/unread-count`

**Response**:

```json
{
  "success": true,
  "message": "Lấy số lượng thông báo chưa đọc thành công",
  "data": {
    "unreadCount": 3
  }
}
```

---

### API-115: Đánh dấu một thông báo đã đọc [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/notifications/{notificationId}/read`

**Response**:

```json
{
  "success": true,
  "message": "Đánh dấu thông báo đã đọc thành công",
  "data": null
}
```

---

### API-116: Đánh dấu tất cả thông báo đã đọc [AUTH]

- **Method**: `PATCH`
- **URL**: `/api/v1/notifications/read-all`

**Response**:

```json
{
  "success": true,
  "message": "Đánh dấu tất cả thông báo đã đọc thành công",
  "data": null
}
```

---

## Module 18: ACTIVITY LOGS (1 API)

---

### API-117: Lấy activity log theo workspace [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/activity-logs/workspace/{workspaceId}?actorId={userId}&actionType=TASK_CREATED&targetType=TASK&fromDateTime=2026-05-01T00:00:00&toDateTime=2026-05-31T23:59:59&page=1&size=20`

**Query params**:

| Param          | Bắt buộc | Ghi chú                   |
| -------------- | -------- | ------------------------- |
| `actorId`      |          | lọc theo user thực hiện   |
| `actionType`   |          | enum `ActivityActionType` |
| `targetType`   |          | enum `ActivityTargetType` |
| `fromDateTime` |          | ISO date-time             |
| `toDateTime`   |          | ISO date-time             |
| `page`, `size` |          | one-indexed pagination    |

**Response**:

```json
{
  "success": true,
  "message": "Lấy danh sách activity log thành công",
  "data": {
    "meta": {
      "currentPage": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalElements": 2,
      "hasNext": false,
      "hasPrevious": false
    },
    "content": [
      {
        "id": 1101,
        "workspaceId": 1,
        "actor": {
          "userId": "owner-uuid",
          "email": "owner@gmail.com",
          "firstName": "Nguyen",
          "lastName": "An"
        },
        "actionType": "TASK_CREATED",
        "targetType": "TASK",
        "targetId": 700,
        "description": "Nguyen An đã tạo task Thiết kế onboarding",
        "createdAt": "2026-05-22T21:10:00"
      }
    ]
  }
}
```

---

## Module 19: POMODORO (2 APIs)

---

### API-118: Lưu phiên Pomodoro cho task [AUTH]

- **Method**: `POST`
- **URL**: `/api/v1/pomodoro/tasks/{taskId}`

**Request body**:

```json
{
  "durationMinutes": 25,
  "startedAt": "2026-05-22T09:00:00",
  "endedAt": "2026-05-22T09:25:00"
}
```

| Field             | Bắt buộc | Validation |
| ----------------- | -------- | ---------- |
| `durationMinutes` | ✓        | từ `1` đến `240` |
| `startedAt`       | tùy chọn | `LocalDateTime` |
| `endedAt`         | tùy chọn | `LocalDateTime` |

**Response**:

```json
{
  "success": true,
  "message": "Lưu phiên Pomodoro thành công",
  "data": {
    "id": 1201,
    "taskId": 700,
    "user": {
      "userId": "member-uuid",
      "email": "member@gmail.com",
      "firstName": "Tran",
      "lastName": "Binh"
    },
    "durationMinutes": 25,
    "startedAt": "2026-05-22T09:00:00",
    "endedAt": "2026-05-22T09:25:00",
    "createdAt": "2026-05-22T09:25:05"
  }
}
```

---

### API-119: Lấy lịch sử Pomodoro theo task của user hiện tại [AUTH]

- **Method**: `GET`
- **URL**: `/api/v1/pomodoro/tasks/{taskId}`

**Response**:

```json
{
  "success": true,
  "message": "Lấy lịch sử Pomodoro thành công",
  "data": [
    {
      "id": 1201,
      "taskId": 700,
      "user": {
        "userId": "member-uuid",
        "email": "member@gmail.com",
        "firstName": "Tran",
        "lastName": "Binh"
      },
      "durationMinutes": 25,
      "startedAt": "2026-05-22T09:00:00",
      "endedAt": "2026-05-22T09:25:00",
      "createdAt": "2026-05-22T09:25:05"
    }
  ]
}
```

**Ghi chú**: endpoint chỉ trả lịch sử Pomodoro của **user hiện tại** trên task tương ứng.

---

## Phụ lục A: Endpoint Summary

| API | Method | URL                                                       | Auth   | Module            |
| --- | ------ | --------------------------------------------------------- | ------ | ----------------- |
| 001 | POST   | `/api/v1/auth/register`                                   | PUBLIC | AUTH              |
| 002 | POST   | `/api/v1/auth/verify-active-account`                      | PUBLIC | AUTH              |
| 003 | POST   | `/api/v1/auth/resend-verify`                              | PUBLIC | AUTH              |
| 004 | POST   | `/api/v1/auth/login`                                      | PUBLIC | AUTH              |
| 005 | POST   | `/api/v1/auth/logout`                                     | AUTH   | AUTH              |
| 006 | GET    | `/api/v1/auth/account`                                    | AUTH   | AUTH              |
| 007 | GET    | `/api/v1/auth/refresh`                                    | PUBLIC | AUTH              |
| 008 | POST   | `/api/v1/auth/forgot-password`                            | PUBLIC | AUTH              |
| 009 | POST   | `/api/v1/auth/reset-password`                             | PUBLIC | AUTH              |
| 010 | PATCH  | `/api/v1/users/update-profile`                            | AUTH   | USERS             |
| 011 | PUT    | `/api/v1/users/update-password`                           | AUTH   | USERS             |
| 012 | PUT    | `/api/v1/users/update-email`                              | AUTH   | USERS             |
| 013 | POST   | `/api/v1/users/verify-change-email`                       | AUTH   | USERS             |
| 014 | GET    | `/api/v1/users`                                           | AUTH   | USERS             |
| 015 | GET    | `/api/v1/users/{userId}`                                  | AUTH   | USERS             |
| 016 | PATCH  | `/api/v1/users/{userId}`                                  | AUTH   | USERS             |
| 017 | DELETE | `/api/v1/users/{userId}`                                  | AUTH   | USERS             |
| 018 | DELETE | `/api/v1/users/{userId}/roles`                            | AUTH   | USERS             |
| 019 | POST   | `/api/v1/users/staff-requests`                            | AUTH   | USERS             |
| 020 | POST   | `/api/v1/roles`                                           | AUTH   | ROLES             |
| 021 | GET    | `/api/v1/roles/{roleId}`                                  | AUTH   | ROLES             |
| 022 | GET    | `/api/v1/roles`                                           | AUTH   | ROLES             |
| 023 | PATCH  | `/api/v1/roles/{roleId}`                                  | AUTH   | ROLES             |
| 024 | DELETE | `/api/v1/roles/{roleId}/permissions`                      | AUTH   | ROLES             |
| 025 | DELETE | `/api/v1/roles/{roleId}`                                  | AUTH   | ROLES             |
| 026 | POST   | `/api/v1/permissions/module`                              | AUTH   | PERMISSIONS       |
| 027 | DELETE | `/api/v1/permissions/module/{name}`                       | AUTH   | PERMISSIONS       |
| 028 | GET    | `/api/v1/permissions/modules`                             | AUTH   | PERMISSIONS       |
| 029 | POST   | `/api/v1/permissions`                                     | AUTH   | PERMISSIONS       |
| 030 | PATCH  | `/api/v1/permissions/{permissionId}`                      | AUTH   | PERMISSIONS       |
| 031 | GET    | `/api/v1/permissions/{permissionId}`                      | AUTH   | PERMISSIONS       |
| 032 | GET    | `/api/v1/permissions`                                     | AUTH   | PERMISSIONS       |
| 033 | DELETE | `/api/v1/permissions/{permissionId}`                      | AUTH   | PERMISSIONS       |
| 034 | POST   | `/api/v1/storage/azure-blob/upload/single`                | AUTH   | STORAGE           |
| 035 | POST   | `/api/v1/storage/azure-blob/upload/multiple`              | AUTH   | STORAGE           |
| 036 | DELETE | `/api/v1/storage/azure-blob/delete/single`                | AUTH   | STORAGE           |
| 037 | DELETE | `/api/v1/storage/azure-blob/delete/multiple`              | AUTH   | STORAGE           |
| 038 | PUT    | `/api/v1/storage/azure-blob/move/single`                  | AUTH   | STORAGE           |
| 039 | PUT    | `/api/v1/storage/azure-blob/move/multiple`                | AUTH   | STORAGE           |
| 040 | POST   | `/api/v1/workspaces`                                      | AUTH   | WORKSPACES        |
| 041 | PATCH  | `/api/v1/workspaces/{workspaceId}`                        | AUTH   | WORKSPACES        |
| 042 | GET    | `/api/v1/workspaces/{workspaceId}`                        | AUTH   | WORKSPACES        |
| 043 | GET    | `/api/v1/workspaces`                                      | AUTH   | WORKSPACES        |
| 044 | POST   | `/api/v1/workspaces/{workspaceId}/members`                | AUTH   | WORKSPACES        |
| 045 | GET    | `/api/v1/workspaces/{workspaceId}/members`                | AUTH   | WORKSPACES        |
| 046 | PATCH  | `/api/v1/workspaces/{workspaceId}/members/{userId}/role`  | AUTH   | WORKSPACES        |
| 047 | DELETE | `/api/v1/workspaces/{workspaceId}/members/{userId}`       | AUTH   | WORKSPACES        |
| 048 | DELETE | `/api/v1/workspaces/{workspaceId}`                        | AUTH   | WORKSPACES        |
| 049 | POST   | `/api/v1/workspace-invites`                               | AUTH   | WORKSPACE_INVITES |
| 050 | GET    | `/api/v1/workspace-invites/workspace/{workspaceId}`       | AUTH   | WORKSPACE_INVITES |
| 051 | PATCH  | `/api/v1/workspace-invites/{inviteId}/revoke`             | AUTH   | WORKSPACE_INVITES |
| 052 | GET    | `/api/v1/workspace-invites/validate/{inviteCode}`         | AUTH   | WORKSPACE_INVITES |
| 053 | POST   | `/api/v1/workspace-invites/join`                          | AUTH   | WORKSPACE_INVITES |
| 054 | POST   | `/api/v1/workspace-teams`                                 | AUTH   | WORKSPACE_TEAMS   |
| 055 | PATCH  | `/api/v1/workspace-teams/{teamId}`                        | AUTH   | WORKSPACE_TEAMS   |
| 056 | GET    | `/api/v1/workspace-teams/{teamId}`                        | AUTH   | WORKSPACE_TEAMS   |
| 057 | GET    | `/api/v1/workspace-teams/workspace/{workspaceId}`         | AUTH   | WORKSPACE_TEAMS   |
| 058 | DELETE | `/api/v1/workspace-teams/{teamId}`                        | AUTH   | WORKSPACE_TEAMS   |
| 059 | POST   | `/api/v1/workspace-teams/{teamId}/members`                | AUTH   | WORKSPACE_TEAMS   |
| 060 | DELETE | `/api/v1/workspace-teams/{teamId}/members/{userId}`       | AUTH   | WORKSPACE_TEAMS   |
| 061 | GET    | `/api/v1/workspace-teams/{teamId}/members`                | AUTH   | WORKSPACE_TEAMS   |
| 062 | POST   | `/api/v1/projects`                                        | AUTH   | PROJECTS          |
| 063 | PATCH  | `/api/v1/projects/{projectId}`                            | AUTH   | PROJECTS          |
| 064 | PATCH  | `/api/v1/projects/{projectId}/status`                     | AUTH   | PROJECTS          |
| 065 | GET    | `/api/v1/projects/{projectId}`                            | AUTH   | PROJECTS          |
| 066 | GET    | `/api/v1/projects/workspace/{workspaceId}`                | AUTH   | PROJECTS          |
| 067 | DELETE | `/api/v1/projects/{projectId}`                            | AUTH   | PROJECTS          |
| 068 | GET    | `/api/v1/projects/{projectId}/analytics`                  | AUTH   | PROJECTS          |
| 069 | GET    | `/api/v1/projects/{projectId}/access`                     | AUTH   | PROJECT_ACCESS    |
| 070 | GET    | `/api/v1/projects/{projectId}/access/me`                  | AUTH   | PROJECT_ACCESS    |
| 071 | POST   | `/api/v1/projects/{projectId}/access`                     | AUTH   | PROJECT_ACCESS    |
| 072 | PATCH  | `/api/v1/projects/{projectId}/access/{accessId}`          | AUTH   | PROJECT_ACCESS    |
| 073 | DELETE | `/api/v1/projects/{projectId}/access/{accessId}`          | AUTH   | PROJECT_ACCESS    |
| 074 | POST   | `/api/v1/goals`                                           | AUTH   | GOALS             |
| 075 | PATCH  | `/api/v1/goals/{goalId}`                                  | AUTH   | GOALS             |
| 076 | GET    | `/api/v1/goals/{goalId}`                                  | AUTH   | GOALS             |
| 077 | GET    | `/api/v1/goals/project/{projectId}`                       | AUTH   | GOALS             |
| 078 | DELETE | `/api/v1/goals/{goalId}`                                  | AUTH   | GOALS             |
| 079 | POST   | `/api/v1/task-statuses`                                   | AUTH   | TASK_STATUSES     |
| 080 | GET    | `/api/v1/task-statuses/project/{projectId}`               | AUTH   | TASK_STATUSES     |
| 081 | PATCH  | `/api/v1/task-statuses/{statusId}`                        | AUTH   | TASK_STATUSES     |
| 082 | PATCH  | `/api/v1/task-statuses/project/{projectId}/reorder`       | AUTH   | TASK_STATUSES     |
| 083 | DELETE | `/api/v1/task-statuses/{statusId}`                        | AUTH   | TASK_STATUSES     |
| 084 | POST   | `/api/v1/task-types`                                      | AUTH   | TASK_TYPES        |
| 085 | PATCH  | `/api/v1/task-types/{taskTypeId}`                         | AUTH   | TASK_TYPES        |
| 086 | GET    | `/api/v1/task-types/{taskTypeId}`                         | AUTH   | TASK_TYPES        |
| 087 | GET    | `/api/v1/task-types/project/{projectId}`                  | AUTH   | TASK_TYPES        |
| 088 | DELETE | `/api/v1/task-types/{taskTypeId}`                         | AUTH   | TASK_TYPES        |
| 089 | POST   | `/api/v1/tasks`                                           | AUTH   | TASKS             |
| 090 | PATCH  | `/api/v1/tasks/{taskId}`                                  | AUTH   | TASKS             |
| 091 | GET    | `/api/v1/tasks/{taskId}`                                  | AUTH   | TASKS             |
| 092 | GET    | `/api/v1/tasks/my-work`                                   | AUTH   | TASKS             |
| 093 | GET    | `/api/v1/tasks/analytics`                                 | AUTH   | TASKS             |
| 094 | GET    | `/api/v1/tasks/{taskId}/dependencies`                     | AUTH   | TASKS             |
| 095 | PUT    | `/api/v1/tasks/{taskId}/dependencies`                     | AUTH   | TASKS             |
| 096 | GET    | `/api/v1/tasks/project/{projectId}`                       | AUTH   | TASKS             |
| 097 | GET    | `/api/v1/tasks/goal/{goalId}`                             | AUTH   | TASKS             |
| 098 | PATCH  | `/api/v1/tasks/{taskId}/move`                             | AUTH   | TASKS             |
| 099 | PATCH  | `/api/v1/tasks/{taskId}/reorder`                          | AUTH   | TASKS             |
| 100 | PATCH  | `/api/v1/tasks/{taskId}/assignee`                         | AUTH   | TASKS             |
| 101 | PATCH  | `/api/v1/tasks/{taskId}/completion`                       | AUTH   | TASKS             |
| 102 | DELETE | `/api/v1/tasks/{taskId}`                                  | AUTH   | TASKS             |
| 103 | POST   | `/api/v1/task-schedules`                                  | AUTH   | TASK_SCHEDULES    |
| 104 | PATCH  | `/api/v1/task-schedules/{scheduleId}`                     | AUTH   | TASK_SCHEDULES    |
| 105 | DELETE | `/api/v1/task-schedules/{scheduleId}`                     | AUTH   | TASK_SCHEDULES    |
| 106 | GET    | `/api/v1/task-schedules/task/{taskId}`                    | AUTH   | TASK_SCHEDULES    |
| 107 | GET    | `/api/v1/task-schedules/calendar/project/{projectId}`     | AUTH   | TASK_SCHEDULES    |
| 108 | GET    | `/api/v1/task-schedules/calendar/workspace/{workspaceId}` | AUTH   | TASK_SCHEDULES    |
| 109 | POST   | `/api/v1/task-comments`                                   | AUTH   | TASK_COMMENTS     |
| 110 | PATCH  | `/api/v1/task-comments/{commentId}`                       | AUTH   | TASK_COMMENTS     |
| 111 | DELETE | `/api/v1/task-comments/{commentId}`                       | AUTH   | TASK_COMMENTS     |
| 112 | GET    | `/api/v1/task-comments/task/{taskId}`                     | AUTH   | TASK_COMMENTS     |
| 113 | GET    | `/api/v1/notifications`                                   | AUTH   | NOTIFICATIONS     |
| 114 | GET    | `/api/v1/notifications/unread-count`                      | AUTH   | NOTIFICATIONS     |
| 115 | PATCH  | `/api/v1/notifications/{notificationId}/read`             | AUTH   | NOTIFICATIONS     |
| 116 | PATCH  | `/api/v1/notifications/read-all`                          | AUTH   | NOTIFICATIONS     |
| 117 | GET    | `/api/v1/activity-logs/workspace/{workspaceId}`           | AUTH   | ACTIVITY_LOGS     |
| 118 | POST   | `/api/v1/pomodoro/tasks/{taskId}`                         | AUTH   | POMODORO          |
| 119 | GET    | `/api/v1/pomodoro/tasks/{taskId}`                         | AUTH   | POMODORO          |

---

## Phụ lục B: DTO Quick Reference

### Core response DTOs

- `UserSummaryResponse`: `userId`, `email`, `firstName`, `lastName`.
- `UserSecureResponse`: thông tin hồ sơ user hiện tại, `rolesSecured`.
- `UserResponse`: thông tin user cho admin, `roles`.
- `WorkspaceResponse`: `id`, `name`, `owner`, `createdAt`, `updatedAt`.
- `WorkspaceMemberResponse`: `id`, `workspaceId`, `user`, `role`, `joinedAt`.
- `WorkspaceInviteResponse`: `id`, `workspaceId`, `workspaceName`, `inviteCode`, `roleToAssign`, `createdBy`, `maxUses`, `usedCount`, `expiresAt`, `isActive`, `createdAt`.
- `WorkspaceTeamResponse`: `id`, `workspaceId`, `name`, `description`, `createdBy`, `memberCount`, timestamps.
- `WorkspaceTeamMemberResponse`: `id`, `teamId`, `user`, `joinedAt`.
- `ProjectResponse`: `id`, `workspaceId`, `name`, `description`, `status`, `visibility`, `createdBy`, `managerUser`, `managerTeamId`, `managerTeamName`, timestamps.
- `ProjectAnalyticsResponse`: `trend`, `completionRate`, `totalTasks`, `completedTasks`.
- `ProjectAccessResponse`: `id`, `projectId`, `subjectType`, `user`, `team`, `role`, `grantedBy`, timestamps.
- `EffectiveProjectAccessResponse`: `projectId`, `workspaceId`, `visibility`, `effectiveRole`, và các cờ `can*` để FE quyết định quyền thao tác.
- `GoalResponse`: `id`, `projectId`, `title`, `goalType`, `status`, `progressPercent`, manager fields, timestamps.
- `TaskStatusResponse`: `id`, `projectId`, `name`, `code`, `position`, `isClosed`, `createdAt`.
- `TaskTypeResponse`: `id`, `workspaceId`, `projectId`, `goalId`, `name`, `description`, `color`, `icon`, timestamps.
- `TaskResponse`: `id`, `workspaceId`, `projectId`, `goalId`, `status`, `title`, `description`, `notesHtml`, `blockerNote`, `priority`, `taskType`, `sourceView`, `assignee`, `createdBy`, `dueDate`, `estimatedMinutes`, `boardPosition`, `isCompleted`, dependency summary, timestamps.
- `MyWorkResponse`: các bộ đếm dashboard cá nhân, `assignedTasks`, `upcomingSchedules`, `generatedAt`.
- `TaskDependencyDetailsResponse`: `taskId`, trạng thái blocked, note blocker, các task đang chặn và bị chặn.
- `TaskScheduleResponse`: `id`, `taskId`, `scheduledStart`, `scheduledEnd`, `scheduledDate`, `createdBy`, timestamps.
- `TaskCommentResponse`: `id`, `taskId`, `parentCommentId`, `user`, `content`, timestamps.
- `NotificationResponse`: `id`, `type`, `title`, `message`, `referenceType`, `referenceId`, `isRead`, `createdAt`.
- `NotificationUnreadCountResponse`: `unreadCount`.
- `ActivityLogResponse`: `id`, `workspaceId`, `actor`, `actionType`, `targetType`, `targetId`, `description`, `createdAt`.
- `PomodoroSessionResponse`: `id`, `taskId`, `user`, `durationMinutes`, `startedAt`, `endedAt`, `createdAt`.

### Pagination DTO

```json
{
  "meta": {
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 3,
    "totalElements": 42,
    "hasNext": true,
    "hasPrevious": false
  },
  "content": []
}
```

---

## Phụ lục C: Common Error Codes

| ErrorCode key              | Code | Message rút gọn                             |
| -------------------------- | ---- | ------------------------------------------- |
| `UNAUTHENTICATED`          | 1002 | Thông tin đăng nhập không hợp lệ            |
| `UNAUTHORIZED`             | 1003 | Không có quyền truy cập tính năng           |
| `TOKEN_EXPIRED`            | 1005 | Token truy cập đã hết hạn                   |
| `INVALID_TOKEN`            | 1006 | Token truy cập không hợp lệ                 |
| `TOKEN_REVOKED`            | 1007 | Token đã bị thu hồi                         |
| `NOT_FOUND_ROUTE`          | 1008 | Không tìm thấy tài nguyên                   |
| `MISSING_TOKEN`            | 1010 | Thiếu token truy cập                        |
| `INVALID_HTTP_METHOD`      | 1015 | Phương thức HTTP không hợp lệ               |
| `INVALID_REQUEST_DATA`     | 1016 | Dữ liệu yêu cầu không hợp lệ                |
| `RESOURCE_NOT_FOUND`       | 1017 | Không tìm thấy tài nguyên                   |
| `TOO_MANY_REQUESTS`        | 1022 | Gửi quá nhiều yêu cầu                       |
| `EMAIL_INVALID`            | 1102 | Định dạng email không hợp lệ                |
| `EMAIL_PROVIDER_INVALID`   | 1103 | Chỉ hỗ trợ Gmail/Yopmail                    |
| `PASSWORD_INVALID_FORMAT`  | 1105 | Mật khẩu không đúng định dạng               |
| `PHONE_NUMBER_VN_INVALID`  | 1111 | Số điện thoại Việt Nam không hợp lệ         |
| `EMAIL_EXISTED`            | 1112 | Email đã tồn tại                            |
| `NOT_VERIFIED_ACCOUNT`     | 1116 | Tài khoản chưa xác thực                     |
| `PHONE_NUMBER_EXISTED`     | 1117 | Số điện thoại đã tồn tại                    |
| `EMAIL_OR_PHONE_REQUIRED`  | 1121 | Cần email hoặc số điện thoại để đăng nhập   |
| `ONLY_EMAIL_OR_PHONE`      | 1122 | Chỉ dùng một trong email hoặc số điện thoại |
| `USER_NOT_FOUND`           | 1201 | Không tìm thấy người dùng                   |
| `ROLE_NOT_FOUND`           | 1304 | Không tìm thấy vai trò                      |
| `PERMISSION_NOT_FOUND`     | 1407 | Không tìm thấy quyền                        |
| `FILE_NOT_FOUND`           | 1501 | Không tìm thấy tệp                          |
| `FILE_TYPE_NOT_ALLOWED`    | 1503 | Loại tệp không được cho phép                |
| `FILE_TOO_LARGE`           | 1504 | Kích thước tệp vượt quá giới hạn            |
| `EMPTY_SOURCE_LIST`        | 1517 | Danh sách tệp nguồn không được để trống     |
| `DESTINATION_FOLDER_EMPTY` | 1518 | Tên thư mục đích không được để trống        |

Nguồn chuẩn: `src/main/java/com/devloopsx/chronelis/exception/ErrorCode.java`.

---

## Phụ lục D: Enum Values

- `WorkspaceMemberRoleType`: `OWNER`, `MEMBER`
- `ProjectStatusType`: `ACTIVE`, `COMPLETED`, `ARCHIVED`
- `ProjectVisibilityType`: `PUBLIC`, `PRIVATE`
- `ProjectAccessSubjectType`: `USER`, `TEAM`
- `ProjectAccessRoleType`: `VIEWER`, `CONTRIBUTOR`, `MANAGER`
- `EffectiveProjectAccessRoleType`: `NO_ACCESS`, `VIEWER`, `CONTRIBUTOR`, `MANAGER`
- `GoalType`: `SHORT_TERM`, `MEDIUM_TERM`, `LONG_TERM`
- `GoalStatusType`: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `ON_HOLD`
- `TaskPriorityType`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- `SourceViewType`: `KANBAN`, `TODO`, `CALENDAR`
- `NotificationType`: `TASK_ASSIGNED`, `TASK_COMMENTED`, `TASK_RESCHEDULED`, `TASK_STATUS_CHANGED`, `GOAL_UPDATED`, `WORKSPACE_MEMBER_ADDED`, `WORKSPACE_MEMBER_REMOVED`, `TASK_CREATED`, `TASK_UPDATED`, `WORKSPACE_INVITE_USED`
- `ReferenceType`: `TASK`, `GOAL`, `PROJECT`, `WORKSPACE`, `COMMENT`, `SCHEDULE`, `TASK_TYPE`, `TEAM`, `INVITE`, `CHECK_ITEM`
- `ActivityActionType`: `WORKSPACE_CREATED`, `WORKSPACE_UPDATED`, `WORKSPACE_DELETED`, `MEMBER_ADDED`, `MEMBER_REMOVED`, `MEMBER_ROLE_UPDATED`, `PROJECT_CREATED`, `PROJECT_UPDATED`, `PROJECT_DELETED`, `GOAL_CREATED`, `GOAL_UPDATED`, `GOAL_DELETED`, `TASK_CREATED`, `TASK_UPDATED`, `TASK_ASSIGNED`, `TASK_UNASSIGNED`, `TASK_RESCHEDULED`, `TASK_DEPENDENCY_UPDATED`, `TASK_MOVED_STATUS`, `TASK_REORDERED`, `TASK_DELETED`, `COMMENT_ADDED`, `COMMENT_UPDATED`, `COMMENT_DELETED`, `TASK_TYPE_CREATED`, `TASK_TYPE_UPDATED`, `TASK_TYPE_DELETED`, `TEAM_CREATED`, `TEAM_UPDATED`, `TEAM_DELETED`, `TEAM_MEMBER_ADDED`, `TEAM_MEMBER_REMOVED`, `INVITE_CREATED`, `INVITE_REVOKED`, `INVITE_USED`, `CHECK_ITEM_CREATED`, `CHECK_ITEM_UPDATED`, `CHECK_ITEM_DELETED`
- `ActivityTargetType`: `TASK`, `GOAL`, `PROJECT`, `WORKSPACE`, `COMMENT`, `SCHEDULE`, `MEMBER`, `STATUS`, `TASK_TYPE`, `TEAM`, `INVITE`, `CHECK_ITEM`
- `HttpMethodType`: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `HEAD`, `OPTIONS`

---

## Phụ lục E: WebSocket Runtime Notes

- STOMP endpoint: `/ws`
- SockJS fallback: bật trong `WebSocketConfig`.
- Broker destinations: `/public`, `/private`
- Application destination prefix: `/app`
- User destination prefix: `/private`
- Frontend nên gửi access token trong STOMP connect header nếu màn hình cần nhận event private.

Nguồn chuẩn: `src/main/java/com/devloopsx/chronelis/configuration/WebSocketConfig.java`.
