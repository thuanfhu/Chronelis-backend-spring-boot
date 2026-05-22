package com.devloopsx.chronelis.utils;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.PermissionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionUtils {
  PermissionRepository permissionRepository;

  public List<Permission> validatePermissionsExist(List<String> providedIds) {
    List<Permission> existingPermissions = this.permissionRepository.findAllById(providedIds);

    Set<String> existingIds =
        existingPermissions.stream().map(Permission::getPermissionId).collect(Collectors.toSet());

    Set<String> nonExistentIds =
        providedIds.stream().filter(id -> !existingIds.contains(id)).collect(Collectors.toSet());

    if (!nonExistentIds.isEmpty()) {
      String customErrorMessage =
          "Quyền hạn với ID: " + nonExistentIds + " không tồn tại trên hệ thống";
      throw new ApplicationException(ErrorCode.PERMISSION_NOT_FOUND, customErrorMessage);
    }

    return existingPermissions;
  }

  public void checkDuplicatePermissionIds(List<String> permissionIds) {
    Set<String> uniquePermissionIds = new HashSet<>(permissionIds);
    if (uniquePermissionIds.size() < permissionIds.size())
      throw new ApplicationException(ErrorCode.DUPLICATE_PERMISSION_IDS);
  }

  public Set<String> getPermissionIdsFromRole(Role role) {
    return role.getPermissions().stream()
        .map(Permission::getPermissionId)
        .collect(Collectors.toSet());
  }

  public void validatePermissionsBelongToModule(List<Permission> permissions, String targetModule) {
    List<Permission> conflictingPermissions =
        permissions.stream()
            .filter(
                permission ->
                    permission.getModule() != null && !permission.getModule().equals(targetModule))
            .toList();

    if (!conflictingPermissions.isEmpty()) {
      String errorMessage =
          "Quyền hạn sau đã thuộc về module khác: "
              + conflictingPermissions.stream()
                  .map(
                      permission ->
                          permission.getPermissionId()
                              + " (Module: "
                              + permission.getModule()
                              + ")")
                  .collect(Collectors.joining(", "));
      throw new ApplicationException(ErrorCode.PERMISSION_ALREADY_IN_ANOTHER_MODULE, errorMessage);
    }
  }

  public void validateUniquePermissionOnCreate(String name, String apiPath, String httpMethod) {
    if (name != null && this.permissionRepository.existsByName(name)) {
      throw new ApplicationException(ErrorCode.PERMISSION_NAME_EXISTED);
    }
    if (apiPath != null
        && httpMethod != null
        && this.permissionRepository.existsByApiPathAndHttpMethod(apiPath, httpMethod)) {
      throw new ApplicationException(ErrorCode.PERMISSION_PATH_AND_METHOD_EXISTED);
    }
  }

  public void validateUniquePermissionOnUpdate(
      String permissionId, String name, String apiPath, String httpMethod) {
    if (name != null
        && this.permissionRepository.existsByNameAndPermissionIdNot(name, permissionId)) {
      throw new ApplicationException(ErrorCode.PERMISSION_NAME_EXISTED);
    }
    if (apiPath != null
        && httpMethod != null
        && this.permissionRepository.existsByApiPathAndHttpMethodAndPermissionIdNot(
            apiPath, httpMethod, permissionId)) {
      throw new ApplicationException(ErrorCode.PERMISSION_PATH_AND_METHOD_EXISTED);
    }
  }
}
