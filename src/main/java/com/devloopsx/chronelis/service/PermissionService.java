package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.dto.request.permission.CreateModuleRequest;
import com.devloopsx.chronelis.dto.request.permission.CreatePermissionRequest;
import com.devloopsx.chronelis.dto.request.permission.UpdatePermissionRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.permission.PermissionResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PermissionService {
  List<PermissionResponse> createModuleForPermissions(CreateModuleRequest createModuleRequest);

  void deleteModuleByName(String moduleName);

  List<String> getAllModules();

  PermissionResponse createPermission(CreatePermissionRequest createPermissionRequest);

  PermissionResponse getPermissionById(String permissionId);

  PaginationResponse getAllPermissionWithQuery(Specification<Permission> spec, Pageable pageable);

  PermissionResponse updatePermissionById(
      String permissionId, UpdatePermissionRequest updatePermissionRequest);

  void deletePermissionById(String permissionId);
}
