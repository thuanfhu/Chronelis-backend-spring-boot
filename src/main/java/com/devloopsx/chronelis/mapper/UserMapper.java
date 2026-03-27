package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.dto.request.auth.RegisterUserRequest;
import com.devloopsx.chronelis.dto.request.user.UpdateUserForAdminRequest;
import com.devloopsx.chronelis.dto.request.user.UpdateUserProfileRequest;
import com.devloopsx.chronelis.dto.response.user.UserResponse;
import com.devloopsx.chronelis.dto.response.user.UserSecureResponse;
import com.devloopsx.chronelis.domain.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "roles", ignore = true)
	@Mapping(target = "nickname", ignore = true)
	@Mapping(target = "avatarUrl", ignore = true)
	@Mapping(target = "biography", ignore = true)
	@Mapping(target = "city", ignore = true)
	@Mapping(target = "nationality", ignore = true)
	@Mapping(target = "refreshToken", ignore = true)
	@Mapping(target = "isVerified", ignore = true)
	User registerRequestToUser(RegisterUserRequest registerUserRequest);

	@Mapping(target = "rolesSecured", source = "roles")
	UserSecureResponse userToSecureResponse(User user);

	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "roles", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "phoneNumber", ignore = true)
	@Mapping(target = "refreshToken", ignore = true)
	@Mapping(target = "isVerified", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateUserProfileRequestToUser(@MappingTarget User user, UpdateUserProfileRequest updateUserProfileRequest);

	List<UserResponse> usersToUserResponseList(List<User> users);

	List<UserSecureResponse> usersToUserSecureResponseList(List<User> users);

	UserResponse userToResponse(User user);

	@Mapping(target = "roles", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "refreshToken", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateUserForAdminRequestToUser(@MappingTarget User user, UpdateUserForAdminRequest updateUserForAdminRequest);
}
