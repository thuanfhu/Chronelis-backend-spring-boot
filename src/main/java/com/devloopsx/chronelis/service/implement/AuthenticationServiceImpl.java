package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.RoleType;
import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.auth.*;
import com.devloopsx.chronelis.dto.response.auth.AuthenticationResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.UserMapper;
import com.devloopsx.chronelis.repository.RoleRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.service.EmailService;
import com.devloopsx.chronelis.utils.PhoneNumberUtils;
import com.devloopsx.chronelis.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.devloopsx.chronelis.service.AuthenticationService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
	UserRepository userRepository;
	RoleRepository roleRepository;
	EmailService emailService;
	UserMapper userMapper;
	PasswordEncoder passwordEncoder;
	SecurityUtils securityUtils;
	PhoneNumberUtils phoneNumberUtils;

	@Override
	public void register(RegisterUserRequest registerUserRequest) {
		if (this.userRepository.existsByEmail(registerUserRequest.getEmail()))
			throw new ApplicationException(ErrorCode.EMAIL_EXISTED);

		String formattedPhoneNumber = this.phoneNumberUtils
				.formatPhoneNumberToE164(registerUserRequest.getPhoneNumber(), "VN");
		if (this.userRepository.existsByPhoneNumber(formattedPhoneNumber))
			throw new ApplicationException(ErrorCode.PHONE_NUMBER_EXISTED);

		User user = this.userMapper.registerRequestToUser(registerUserRequest);
		user.setPassword(this.passwordEncoder.encode(registerUserRequest.getPassword()));
		user.setPhoneNumber(formattedPhoneNumber);

		// Set default CUSTOMER role
		List<Role> roles = new ArrayList<>();
		roles.add(this.roleRepository.findByName(RoleType.CUSTOMER_ROLE.getName())
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NAME_NOT_FOUND)));
		user.setRoles(roles);

		this.userRepository.save(user);
		this.emailService.sendVerifyActiveAccountEmail(user);
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		User currentUser = this.userRepository.findByEmail(forgotPasswordRequest.getEmail())
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		if (!currentUser.getIsVerified())
			throw new ApplicationException(ErrorCode.NOT_VERIFIED_ACCOUNT);
		this.emailService.sendForgotPasswordEmail(currentUser);
	}

	@Override
	public void resetPassword(ResetPasswordRequest resetPasswordRequest) throws ParseException, JOSEException {
		// Verify token and get current user by email
		SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(resetPasswordRequest.getToken());
		String userId = verifiedToken.getJWTClaimsSet().getSubject();
		User currentUser = this.userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

		// New password same current password
		if (passwordEncoder.matches(resetPasswordRequest.getNewPassword(), currentUser.getPassword()))
			throw new ApplicationException(ErrorCode.PASSWORD_SAME_AS_CURRENT);

		// Update new password, logout user and notification login again
		currentUser.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
		this.userRepository.save(currentUser);
	}

	@Override
	public AuthenticationResponse verifyEmailAndActivateAccount(VerifyEmailRequest verifyEmailRequest)
			throws ParseException, JOSEException {
		SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(verifyEmailRequest.getToken());

		// Get information user and update status isVerified = true
		String userId = verifiedToken.getJWTClaimsSet().getSubject();

		User currentUser = this.userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		if (currentUser.getIsVerified())
			throw new ApplicationException(ErrorCode.NOT_VERIFIED_ACCOUNT_TWICE);

		currentUser.setIsVerified(true);
		userRepository.save(currentUser);

		return AuthenticationResponse.builder().accessToken(this.securityUtils.generateAccessToken(currentUser))
				.userSecured(userMapper.userToSecureResponse(currentUser)).build();
	}

	@Override
	public void resendVerifyEmail(ResendVerifyEmailRequest resendVerifyEmailRequest) {
		User currentUser = this.userRepository.findByEmail(resendVerifyEmailRequest.getEmail())
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		if (currentUser.getIsVerified())
			throw new ApplicationException(ErrorCode.NOT_VERIFIED_ACCOUNT_TWICE);
		this.emailService.sendVerifyActiveAccountEmail(currentUser);
	}

	@Override
	public ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest) {
		User currentUser;

		String emailLoginForm = loginRequest.getEmail();
		String phoneNumberLoginForm = loginRequest.getPhoneNumber();
		String passwordLoginForm = loginRequest.getPassword();

		if (emailLoginForm != null && !emailLoginForm.isEmpty()) {
			currentUser = this.userRepository.findByEmail(emailLoginForm)
					.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		} else {
			String formattedPhoneNumber = this.phoneNumberUtils.formatPhoneNumberToE164(phoneNumberLoginForm, "VN");
			currentUser = this.userRepository.findByPhoneNumber(formattedPhoneNumber)
					.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		}

		// Not verified account
		if (!currentUser.getIsVerified())
			throw new ApplicationException(ErrorCode.NOT_VERIFIED_ACCOUNT);

		// Compare form request password with database password
		boolean isPasswordMatch = passwordEncoder.matches(passwordLoginForm, currentUser.getPassword());
		if (!isPasswordMatch)
			throw new ApplicationException(ErrorCode.UNAUTHENTICATED);

		return this.securityUtils.createAuthResponse(currentUser);
	}

	@Override
	public UserSecureResponse getMyInfo() {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return this.userMapper.userToSecureResponse(
				userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND)));
	}

	@Override
	public ResponseEntity<Void> logout() {
		return this.securityUtils.logout();
	}

	@Override
	public ResponseEntity<AuthenticationResponse> getNewToken(String refreshToken)
			throws ParseException, JOSEException {
		// Validate refresh token (not expired, valid, same in database)
		SignedJWT verifiedRefreshToken = this.securityUtils.verifyRefreshToken(refreshToken);

		String userId = verifiedRefreshToken.getJWTClaimsSet().getSubject();
		User currentUser = this.userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

		return this.securityUtils.createAuthResponse(currentUser);
	}
}
