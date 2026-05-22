package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.auth.*;
import com.devloopsx.chronelis.dto.response.auth.AuthenticationResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
  void register(RegisterUserRequest registerUserRequest);

  AuthenticationResponse verifyEmailAndActivateAccount(VerifyEmailRequest verifyEmailRequest)
      throws ParseException, JOSEException;

  ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest);

  ResponseEntity<Void> logout();

  UserSecureResponse getMyInfo();

  ResponseEntity<AuthenticationResponse> getNewToken(String refreshToken)
      throws ParseException, JOSEException;

  void resendVerifyEmail(ResendVerifyEmailRequest resendVerifyEmailRequest);

  void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

  void resetPassword(ResetPasswordRequest resetPasswordRequest)
      throws ParseException, JOSEException;
}
