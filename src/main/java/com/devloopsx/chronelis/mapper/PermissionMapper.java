package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.dto.request.permission.CreatePermissionRequest;
import com.devloopsx.chronelis.dto.request.permission.UpdatePermissionRequest;
import com.devloopsx.chronelis.dto.response.permission.PermissionResponse;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
  PermissionResponse permissionToResponse(Permission permission);

  @Mapping(target = "permissionId", ignore = true)
  @Mapping(target = "roles", ignore = true)
  Permission createPermissionRequestToPermission(CreatePermissionRequest createPermissionRequest);

  @Mapping(target = "module", ignore = true)
  @Mapping(target = "permissionId", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "version", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updatePermissionRequestToPermission(
      @MappingTarget Permission permission, UpdatePermissionRequest updatePermissionRequest);

  List<PermissionResponse> permissionsToPermissionResponseList(List<Permission> permissions);
}
