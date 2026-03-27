package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.auth.VerifyEmailRequest;
import com.devloopsx.chronelis.dto.request.user.*;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.user.UserResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.devloopsx.chronelis.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

	UserService userService;

	@PatchMapping("/update-profile")
	ApiResponse<UserSecureResponse> updateUserProfile(
			@RequestBody @Valid UpdateUserProfileRequest updateUserProfileRequest, HttpServletRequest servletRequest) {
		return ApiResponse.<UserSecureResponse>builder().message("Cập nhật thông tin người dùng thành công")
				.data(userService.updateUserProfile(updateUserProfileRequest)).meta(buildMetaInfo(servletRequest))
				.build();
	}

	@PutMapping("/update-password")
	ApiResponse<UserSecureResponse> updateUserPassword(
			@RequestBody @Valid UpdateUserPasswordRequest updateUserPasswordRequest,
			HttpServletRequest servletRequest) {
		return ApiResponse.<UserSecureResponse>builder().message("Cập nhật mật khẩu thành công")
				.data(userService.updateUserPassword(updateUserPasswordRequest)).meta(buildMetaInfo(servletRequest))
				.build();
	}

	@PutMapping("/update-email")
	ApiResponse<Void> updateUserEmail(@RequestBody @Valid UpdateUserEmailRequest updateUserEmailRequest,
			HttpServletRequest servletRequest) {
		userService.updateUserEmail(updateUserEmailRequest);
		return ApiResponse.<Void>builder().message("Vui lòng kiểm tra email mới để xác thực và hoàn tất cập nhật")
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@PostMapping("/verify-change-email")
	ApiResponse<UserSecureResponse> verifyEmailAndChangeNewEmail(
			@RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest)
			throws ParseException, JOSEException {
		return ApiResponse.<UserSecureResponse>builder()
				.message("Địa chỉ email đã được thay đổi thành công. Vui lòng đăng nhập lại")
				.data(userService.verifyEmailAndChangeNewEmail(verifyEmailRequest)).meta(buildMetaInfo(servletRequest))
				.build();
	}

	@GetMapping
	ApiResponse<PaginationResponse> getAllUserWithQuery(@Filter Specification<User> spec, Pageable pageable,
			HttpServletRequest servletRequest) {
		return ApiResponse.<PaginationResponse>builder()
				.message("Lấy danh sách người dùng thành công với bộ lọc truy vấn")
				.data(userService.getAllUserWithQuery(spec, pageable)).meta(buildMetaInfo(servletRequest)).build();
	}

	@GetMapping("/{userId}")
	ApiResponse<UserSecureResponse> getUserById(@PathVariable String userId, HttpServletRequest servletRequest) {
		return ApiResponse.<UserSecureResponse>builder().message("Lấy thông tin người dùng thành công")
				.data(userService.getUserById(userId)).meta(buildMetaInfo(servletRequest)).build();
	}

	@PatchMapping("/{userId}")
	ApiResponse<UserResponse> updateUserForAdmin(@PathVariable String userId,
			@RequestBody @Valid UpdateUserForAdminRequest updateUserForAdminRequest,
			HttpServletRequest servletRequest) {
		return ApiResponse.<UserResponse>builder().message("Cập nhật thông tin người dùng thành công")
				.data(userService.updateUserForAdmin(userId, updateUserForAdminRequest))
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@DeleteMapping("/{userId}")
	ApiResponse<Void> deleteUserById(@PathVariable String userId, HttpServletRequest servletRequest) {
		userService.deleteUserById(userId);
		return ApiResponse.<Void>builder().message("Xóa tài khoản người dùng thành công")
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@DeleteMapping("/{userId}/roles")
	ApiResponse<Void> deleteRoleFromUser(@PathVariable String userId,
			@RequestBody DeleteRoleFromUserRequest deleteRoleFromUserRequest, HttpServletRequest servletRequest) {
		userService.deleteRoleFromUser(userId, deleteRoleFromUserRequest);
		return ApiResponse.<Void>builder().message("Loại bỏ vai trò khỏi người dùng thành công")
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@PostMapping("/staff-requests")
	ApiResponse<UserSecureResponse> becomeStaff(HttpServletRequest servletRequest) {
		return ApiResponse.<UserSecureResponse>builder().message("Nâng cấp thành công sang vai trò STAFF")
				.data(userService.becomeStaff()).meta(buildMetaInfo(servletRequest)).build();
	}
}
