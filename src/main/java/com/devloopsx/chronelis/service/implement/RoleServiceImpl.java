package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.RoleType;
import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.role.CreateRoleRequest;
import com.devloopsx.chronelis.dto.request.role.DeletePermissionFromRoleRequest;
import com.devloopsx.chronelis.dto.request.role.UpdateRoleRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.role.RoleResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.RoleMapper;
import com.devloopsx.chronelis.repository.RoleRepository;
import com.devloopsx.chronelis.service.RoleService;
import com.devloopsx.chronelis.utils.PermissionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
	RoleRepository roleRepository;
	RoleMapper roleMapper;
	PermissionUtils permissionUtils;

	@Override
	public RoleResponse createRole(CreateRoleRequest createRoleRequest) {
		String roleName = createRoleRequest.getName().toUpperCase();
		if (this.roleRepository.existsByName(roleName))
			throw new ApplicationException(ErrorCode.ROLE_NAME_EXISTED);

		Role role = this.roleMapper.createRoleRequestToRole(createRoleRequest);
		role.setName(roleName);

		List<String> permissionIds = createRoleRequest.getPermissionIds();

		if (permissionIds != null && !permissionIds.isEmpty()) {
			this.permissionUtils.checkDuplicatePermissionIds(permissionIds);
			List<Permission> permissions = this.permissionUtils.validatePermissionsExist(permissionIds);
			role.setPermissions(permissions);
		}

		return this.roleMapper.roleToResponse(this.roleRepository.save(role));
	}

	@Override
	public RoleResponse getRoleById(String roleId) {
		return this.roleMapper.roleToResponse(this.roleRepository.findById(roleId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND)));
	}

	@Override
	public PaginationResponse getAllRoleWithQuery(Specification<Role> spec, Pageable pageable) {
		Page<Role> rolePage = this.roleRepository.findAll(spec, pageable);
		return PaginationResponse.builder().meta(PaginationMeta.builder().currentPage(pageable.getPageNumber() + 1) // base-index
																													// =
																													// 0
				.pageSize(pageable.getPageSize()).totalPages(rolePage.getTotalPages())
				.totalElements(rolePage.getTotalElements()).hasNext(rolePage.hasNext())
				.hasPrevious(rolePage.hasPrevious()).build())
				.content(this.roleMapper.rolesToRoleResponseList(rolePage.getContent())).build();
	}

	@Override
	public RoleResponse updateRoleById(String roleId, UpdateRoleRequest updateRoleRequest) {
		Role currentRole = this.roleRepository.findById(roleId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND));

		String roleName = updateRoleRequest.getName();
		if (roleName != null && !roleName.equals(currentRole.getName()) && this.roleRepository.existsByName(roleName))
			throw new ApplicationException(ErrorCode.ROLE_NAME_EXISTED);

		Boolean roleIsActive = updateRoleRequest.getActive();
		if (currentRole.getActive().equals(roleIsActive))
			throw new ApplicationException(ErrorCode.ROLE_SAME_IS_ACTIVE);

		this.roleMapper.updateRoleRequestToRole(currentRole, updateRoleRequest);

		List<String> permissionIds = updateRoleRequest.getPermissionIds();
		if (permissionIds != null && !permissionIds.isEmpty()) {
			Set<String> existingPermissionIdsInRole = this.permissionUtils.getPermissionIdsFromRole(currentRole);
			this.permissionUtils.checkDuplicatePermissionIds(permissionIds);
			this.permissionUtils.validatePermissionsExist(permissionIds);

			Set<String> duplicatePermissionIds = permissionIds.stream().filter(existingPermissionIdsInRole::contains)
					.collect(Collectors.toSet());

			if (!duplicatePermissionIds.isEmpty())
				throw new ApplicationException(ErrorCode.PERMISSION_ALREADY_EXISTS_IN_ROLE,
						"Vai trò " + currentRole.getName() + " đã có quyền hạn với ID: " + duplicatePermissionIds
								+ ". Vui lòng nhập lại");

			List<Permission> providedPermissions = this.permissionUtils.validatePermissionsExist(permissionIds);
			currentRole.getPermissions().addAll(providedPermissions);
		}

		return this.roleMapper.roleToResponse(this.roleRepository.save(currentRole));
	}

	@Override
	public void deletePermissionFromRole(String roleId,
			DeletePermissionFromRoleRequest deletePermissionFromRoleRequest) {
		Role currentRole = this.roleRepository.findById(roleId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND));

		List<String> permissionIds = deletePermissionFromRoleRequest.getPermissionIds();

		this.permissionUtils.checkDuplicatePermissionIds(permissionIds);
		this.permissionUtils.validatePermissionsExist(permissionIds);
		Set<String> existingPermissionIdsInRole = permissionUtils.getPermissionIdsFromRole(currentRole);

		Set<String> nonExistentInRole = permissionIds.stream().filter(id -> !existingPermissionIdsInRole.contains(id))
				.collect(Collectors.toSet());

		if (!nonExistentInRole.isEmpty())
			throw new ApplicationException(ErrorCode.PERMISSION_NOT_IN_ROLE,
					"Quyền hạn với ID: " + nonExistentInRole + " không có trong vai trò " + currentRole.getName());

		currentRole.getPermissions().removeIf(permission -> permissionIds.contains(permission.getPermissionId()));
		this.roleRepository.save(currentRole);
	}

	@Override
	public void deleteRoleById(String roleId) {
		Role currentRole = this.roleRepository.findById(roleId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND));

		// If system role, you can't delete
		List<String> protectedRoles = RoleType.getAllRoleNames();
		if (protectedRoles.contains(currentRole.getName()))
			throw new ApplicationException(ErrorCode.SYSTEM_ROLE_CANNOT_BE_DELETED);

		currentRole.getUsers().forEach(user -> user.getRoles().remove(currentRole));
		currentRole.getPermissions().clear(); // owner side (@JoinTable)
		currentRole.getUsers().clear();

		this.roleRepository.delete(currentRole);
	}
}
