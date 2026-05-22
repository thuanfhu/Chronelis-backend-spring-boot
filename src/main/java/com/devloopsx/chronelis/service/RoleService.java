package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.role.CreateRoleRequest;
import com.devloopsx.chronelis.dto.request.role.DeletePermissionFromRoleRequest;
import com.devloopsx.chronelis.dto.request.role.UpdateRoleRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.role.RoleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleService {
  RoleResponse createRole(CreateRoleRequest createRoleRequest);

  RoleResponse getRoleById(String roleId);

  PaginationResponse getAllRoleWithQuery(Specification<Role> spec, Pageable pageable);

  RoleResponse updateRoleById(String roleId, UpdateRoleRequest updateRoleRequest);

  void deletePermissionFromRole(
      String roleId, DeletePermissionFromRoleRequest deletePermissionFromRoleRequest);

  void deleteRoleById(String roleId);
}
