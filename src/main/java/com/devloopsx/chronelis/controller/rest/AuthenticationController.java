package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.auth.*;
import com.devloopsx.chronelis.dto.response.auth.AuthenticationResponse;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.devloopsx.chronelis.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.text.ParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

  AuthenticationService authenticationService;

  @PostMapping("/register")
  ApiResponse<Void> register(
      @RequestBody @Valid RegisterUserRequest registerUserRequest,
      HttpServletRequest servletRequest) {
    authenticationService.register(registerUserRequest);
    return ApiResponse.<Void>builder()
        .message("Vui lòng kiểm tra email để xác thực tài khoản")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/verify-active-account")
  ApiResponse<AuthenticationResponse> verifyEmailAndActivateAccount(
      @RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest)
      throws ParseException, JOSEException {
    return ApiResponse.<AuthenticationResponse>builder()
        .message("Email của bạn đã được xác thực thành công")
        .data(authenticationService.verifyEmailAndActivateAccount(verifyEmailRequest))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/resend-verify")
  ApiResponse<Void> resendVerifyEmail(
      @RequestBody @Valid ResendVerifyEmailRequest resendVerifyEmailRequest,
      HttpServletRequest servletRequest) {
    authenticationService.resendVerifyEmail(resendVerifyEmailRequest);
    return ApiResponse.<Void>builder()
        .message("Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư đến")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/login")
  ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest) {
    ResponseEntity<AuthenticationResponse> responseEntity =
        authenticationService.login(loginRequest);
    ApiResponse<AuthenticationResponse> apiResponse =
        ApiResponse.<AuthenticationResponse>builder()
            .message("Đăng nhập thành công")
            .data(responseEntity.getBody())
            .meta(buildMetaInfo(servletRequest))
            .build();

    return ResponseEntity.status(responseEntity.getStatusCode())
        .headers(responseEntity.getHeaders())
        .body(apiResponse);
  }

  @PostMapping("/logout")
  ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest) {
    ResponseEntity<Void> responseEntity = authenticationService.logout();
    ApiResponse<Void> apiResponse =
        ApiResponse.<Void>builder()
            .message("Đăng xuất thành công")
            .meta(buildMetaInfo(servletRequest))
            .build();

    return ResponseEntity.status(responseEntity.getStatusCode())
        .headers(responseEntity.getHeaders())
        .body(apiResponse);
  }

  @GetMapping("/account")
  ApiResponse<UserSecureResponse> getMyInfo(HttpServletRequest servletRequest) {
    return ApiResponse.<UserSecureResponse>builder()
        .message("Lấy thông tin người dùng đã xác thực thành công")
        .data(authenticationService.getMyInfo())
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/refresh")
  ResponseEntity<ApiResponse<AuthenticationResponse>> getNewToken(
      @CookieValue(name = "refresh_token") String refreshToken, HttpServletRequest servletRequest)
      throws ParseException, JOSEException {
    ResponseEntity<AuthenticationResponse> responseEntity =
        authenticationService.getNewToken(refreshToken);
    ApiResponse<AuthenticationResponse> apiResponse =
        ApiResponse.<AuthenticationResponse>builder()
            .message("Lấy thành công refresh token và access token")
            .data(responseEntity.getBody())
            .meta(buildMetaInfo(servletRequest))
            .build();

    return ResponseEntity.status(responseEntity.getStatusCode())
        .headers(responseEntity.getHeaders())
        .body(apiResponse);
  }

  @PostMapping("/forgot-password")
  ApiResponse<Void> forgotPassword(
      @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest,
      HttpServletRequest servletRequest) {
    authenticationService.forgotPassword(forgotPasswordRequest);
    return ApiResponse.<Void>builder()
        .message("Vui lòng kiểm tra email để đặt lại mật khẩu")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/reset-password")
  ApiResponse<Void> resetPassword(
      @RequestBody @Valid ResetPasswordRequest resetPasswordRequest,
      HttpServletRequest servletRequest)
      throws ParseException, JOSEException {
    authenticationService.resetPassword(resetPasswordRequest);
    return ApiResponse.<Void>builder()
        .message("Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập lại")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
