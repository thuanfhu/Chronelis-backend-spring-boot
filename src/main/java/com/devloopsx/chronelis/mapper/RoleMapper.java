package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.role.CreateRoleRequest;
import com.devloopsx.chronelis.dto.request.role.UpdateRoleRequest;
import com.devloopsx.chronelis.dto.response.role.RoleResponse;
import com.devloopsx.chronelis.dto.response.role.RoleSecureResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
	@Mapping(target = "permissions", ignore = true)
	@Mapping(target = "roleId", ignore = true)
	@Mapping(target = "users", ignore = true)
	Role createRoleRequestToRole(CreateRoleRequest createRoleRequest);

	@Mapping(target = "permissions", ignore = true)
	@Mapping(target = "roleId", ignore = true)
	@Mapping(target = "users", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateRoleRequestToRole(@MappingTarget Role role, UpdateRoleRequest updateRoleRequest);

	RoleSecureResponse roleToSecureResponse(Role role);

	RoleResponse roleToResponse(Role role);

	List<RoleResponse> rolesToRoleResponseList(List<Role> roles);
}
