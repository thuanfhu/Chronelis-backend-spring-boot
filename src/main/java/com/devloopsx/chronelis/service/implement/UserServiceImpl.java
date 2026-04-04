package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.RoleType;
import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.auth.VerifyEmailRequest;
import com.devloopsx.chronelis.dto.request.user.*;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.user.UserResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.UserMapper;
import com.devloopsx.chronelis.repository.ActivityLogRepository;
import com.devloopsx.chronelis.repository.GoalRepository;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.RoleRepository;
import com.devloopsx.chronelis.repository.TaskCommentRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.repository.TaskScheduleRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceInviteRepository;
import com.devloopsx.chronelis.repository.WorkspaceRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamRepository;
import com.devloopsx.chronelis.service.EmailService;
import com.devloopsx.chronelis.service.UserService;
import com.devloopsx.chronelis.utils.PhoneNumberUtils;
import com.devloopsx.chronelis.utils.RoleUtils;
import com.devloopsx.chronelis.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
	UserRepository userRepository;
	WorkspaceRepository workspaceRepository;
	ProjectRepository projectRepository;
	GoalRepository goalRepository;
	TaskRepository taskRepository;
	TaskScheduleRepository taskScheduleRepository;
	TaskCommentRepository taskCommentRepository;
	WorkspaceTeamRepository workspaceTeamRepository;
	WorkspaceInviteRepository workspaceInviteRepository;
	ActivityLogRepository activityLogRepository;
	RoleRepository roleRepository;
	EmailService emailService;
	UserMapper userMapper;
	PasswordEncoder passwordEncoder;
	SecurityUtils securityUtils;
	PhoneNumberUtils phoneNumberUtils;
	RoleUtils roleUtils;

	@Override
	public UserSecureResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest) {
		User userAuthenticated = securityUtils.getAuthenticatedUser();
		userMapper.updateUserProfileRequestToUser(userAuthenticated, updateUserProfileRequest);
		return userMapper.userToSecureResponse(userRepository.save(userAuthenticated));
	}

	@Override
	public UserSecureResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
		User userAuthenticated = this.securityUtils.getAuthenticatedUser();

		if (!passwordEncoder.matches(updateUserPasswordRequest.getCurrentPassword(), userAuthenticated.getPassword()))
			throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
		if (!updateUserPasswordRequest.getNewPassword().equals(updateUserPasswordRequest.getConfirmPassword()))
			throw new ApplicationException(ErrorCode.PASSWORD_AND_CONFIRM_MISMATCH);
		if (passwordEncoder.matches(updateUserPasswordRequest.getNewPassword(), userAuthenticated.getPassword()))
			throw new ApplicationException(ErrorCode.PASSWORD_SAME_AS_CURRENT);

		String hashPassword = this.passwordEncoder.encode(updateUserPasswordRequest.getNewPassword());
		userAuthenticated.setPassword(hashPassword);
		UserSecureResponse response = this.userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));

		// Logout user after password changed successfully
		this.securityUtils.logout();

		return response;
	}

	@Override
	public void updateUserEmail(UpdateUserEmailRequest updateUserEmailRequest) {
		User userAuthenticated = securityUtils.getAuthenticatedUser();
		String newEmail = updateUserEmailRequest.getNewEmail();

		securityUtils.enforceProtectedEmailPolicy(userAuthenticated.getEmail()); // can't change system email
		if (userAuthenticated.getEmail().equals(newEmail))
			throw new ApplicationException(ErrorCode.NEW_EMAIL_SAME_BEFORE);
		if (userRepository.existsByEmail(newEmail))
			throw new ApplicationException(ErrorCode.EMAIL_EXISTED);

		// Set temporary new email to current user and send verify token to this email
		userAuthenticated.setEmail(newEmail);
		emailService.sendVerifyChangeEmail(userAuthenticated);
	}

	@Override
	public UserSecureResponse verifyEmailAndChangeNewEmail(VerifyEmailRequest verifyEmailRequest)
			throws ParseException, JOSEException {
		SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(verifyEmailRequest.getToken());
		User userAuthenticated = this.securityUtils.getAuthenticatedUser();

		String updateEmail = verifiedToken.getJWTClaimsSet().getStringClaim("email");
		userAuthenticated.setEmail(updateEmail);

		UserSecureResponse response = userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));

		// Logout user after email changed successfully
		this.securityUtils.logout();

		return response;
	}

	@Override
	public PaginationResponse getAllUserWithQuery(Specification<User> spec, Pageable pageable) {
		Page<User> userPage = userRepository.findAll(spec, pageable);
		User userAuthenticated = this.securityUtils.getAuthenticatedUser();

		return PaginationResponse.builder().meta(PaginationMeta.builder().currentPage(pageable.getPageNumber() + 1) // base-index
																													// //
																													// 0
				.pageSize(pageable.getPageSize()).totalPages(userPage.getTotalPages())
				.totalElements(userPage.getTotalElements()).hasNext(userPage.hasNext())
				.hasPrevious(userPage.hasPrevious()).build())
				.content(this.securityUtils.isAdmin(userAuthenticated)
						? this.userMapper.usersToUserResponseList(userPage.getContent())
						: this.userMapper.usersToUserSecureResponseList(userPage.getContent()))
				.build();
	}

	@Override
	public UserSecureResponse getUserById(String userId) {
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		return this.userMapper.userToSecureResponse(user);
	}

	@Override
	public UserResponse updateUserForAdmin(String userId, UpdateUserForAdminRequest updateUserForAdminRequest) {
		User currentUser = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		securityUtils.enforceProtectedEmailPolicy(currentUser.getEmail()); // can't change system email

		// Check phone number and email is valid
		String currentEmail = updateUserForAdminRequest.getEmail();
		if (currentEmail != null && !currentEmail.isEmpty()) {
			if (userRepository.existsByEmail(currentEmail))
				throw new ApplicationException(ErrorCode.EMAIL_EXISTED);
		}

		String currentPhoneNumber = updateUserForAdminRequest.getPhoneNumber();
		if (currentPhoneNumber != null && !currentPhoneNumber.isEmpty()) {
			String formattedPhoneNumber = phoneNumberUtils.formatPhoneNumberToE164(currentPhoneNumber, "VN");
			if (userRepository.existsByPhoneNumber(formattedPhoneNumber))
				throw new ApplicationException(ErrorCode.PHONE_NUMBER_EXISTED);
		}

		Boolean isVerifiedAccount = updateUserForAdminRequest.getIsVerified();
		if (currentUser.getIsVerified().equals(isVerifiedAccount))
			throw new ApplicationException(ErrorCode.USER_SAME_IS_VERIFY);

		userMapper.updateUserForAdminRequestToUser(currentUser, updateUserForAdminRequest);

		// Get roleIds and map List<Role> to User entity
		List<String> roleIds = updateUserForAdminRequest.getRoleIds();

		if (roleIds != null && !roleIds.isEmpty()) {
			Set<String> uniqueRoleIds = new HashSet<>(roleIds);
			if (uniqueRoleIds.size() < roleIds.size())
				throw new ApplicationException(ErrorCode.DUPLICATE_ROLE_IDS);

			List<Role> newRoles = roleUtils.validateRolesExist(roleIds);
			roleUtils.validateUserDoesNotAlreadyHaveRoles(currentUser, newRoles);

			currentUser.getRoles().addAll(newRoles);
		}

		return userMapper.userToResponse(userRepository.save(currentUser));
	}

	@Override
	@Transactional
	public void deleteUserById(String userId) {
		User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		securityUtils.enforceProtectedEmailPolicy(targetUser.getEmail());

		User replacementUser = userRepository.findById(securityUtils.getAuthenticatedUser().getUserId())
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

		if (targetUser.getUserId().equals(replacementUser.getUserId())) {
			throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
					"Không thể tự xóa tài khoản đang đăng nhập");
		}

		reassignUserForeignKeys(targetUser.getUserId(), replacementUser);

		targetUser.getRoles().clear();
		userRepository.delete(targetUser);
	}

	@Override
	public void deleteRoleFromUser(String userId, DeleteRoleFromUserRequest deleteRoleFromUserRequest) {
		User currentUser = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

		List<String> roleIds = deleteRoleFromUserRequest.getRoleIds();

		roleUtils.checkDuplicateRoleIds(roleIds);
		roleUtils.validateRolesExist(roleIds);

		Set<String> existingRoleIdsInUser = roleUtils.getRoleIdsFromUser(currentUser);
		Set<String> nonExistentInUser = roleIds.stream().filter(id -> !existingRoleIdsInUser.contains(id))
				.collect(Collectors.toSet());

		if (!nonExistentInUser.isEmpty()) {
			throw new ApplicationException(ErrorCode.ROLE_NOT_IN_USER,
					"Vai trò với ID: " + nonExistentInUser + " không có trong người dùng " + currentUser.getEmail());
		}

		currentUser.getRoles().removeIf(role -> roleIds.contains(role.getRoleId()));
		userRepository.save(currentUser);
	}

	@Override
	@Transactional
	public UserSecureResponse becomeStaff() {
		User currentUser = securityUtils.getAuthenticatedUser();

		boolean hasUserRole = currentUser.getRoles().stream()
				.anyMatch(role -> RoleType.USER_ROLE.getName().equals(role.getName()));

		if (hasUserRole) {
			throw new ApplicationException(ErrorCode.USER_ALREADY_HAS_STAFF_ROLE);
		}

		Role staffRole = roleRepository.findByName(RoleType.USER_ROLE.getName())
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NAME_NOT_FOUND));

		currentUser.getRoles().add(staffRole);
		User savedUser = userRepository.save(currentUser);
		return userMapper.userToSecureResponse(savedUser);
	}

	private void reassignUserForeignKeys(String sourceUserId, User replacementUser) {
		taskRepository.clearAssigneeReferences(sourceUserId);
		activityLogRepository.reassignActor(sourceUserId, replacementUser);
		workspaceInviteRepository.reassignCreatedBy(sourceUserId, replacementUser);
		workspaceTeamRepository.reassignCreatedBy(sourceUserId, replacementUser);
		taskCommentRepository.reassignCommentAuthor(sourceUserId, replacementUser);
		taskScheduleRepository.reassignCreatedBy(sourceUserId, replacementUser);
		taskRepository.reassignCreatedBy(sourceUserId, replacementUser);
		goalRepository.reassignCreatedBy(sourceUserId, replacementUser);
		projectRepository.reassignCreatedBy(sourceUserId, replacementUser);
		workspaceRepository.reassignOwner(sourceUserId, replacementUser);
	}
}
