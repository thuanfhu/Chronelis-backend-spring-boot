package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.auth.VerifyEmailRequest;
import com.devloopsx.chronelis.dto.request.user.*;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.user.UserResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {
  UserSecureResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest);

  UserSecureResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest);

  UserSecureResponse verifyEmailAndChangeNewEmail(VerifyEmailRequest verifyEmailRequest)
      throws ParseException, JOSEException;

  void updateUserEmail(UpdateUserEmailRequest updateUserEmailRequest);

  PaginationResponse getAllUserWithQuery(Specification<User> spec, Pageable pageable);

  UserSecureResponse getUserById(String userId);

  UserResponse updateUserForAdmin(
      String userId, UpdateUserForAdminRequest updateUserForAdminRequest);

  void deleteRoleFromUser(String userId, DeleteRoleFromUserRequest deleteRoleFromUserRequest);

  void deleteUserById(String userId);

  UserSecureResponse becomeStaff();
}
