package com.devloopsx.chronelis.utils;

import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleUtils {
  RoleRepository roleRepository;

  public List<Role> validateRolesExist(List<String> roleIds) {
    return roleIds.stream()
        .map(
            roleId ->
                roleRepository
                    .findById(roleId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND)))
        .collect(Collectors.toList());
  }

  public void validateUserDoesNotAlreadyHaveRoles(User user, List<Role> newRoles) {
    Set<String> existingRoleIds = getRoleIdsFromUser(user);
    for (Role role : newRoles) {
      if (existingRoleIds.contains(role.getRoleId())) {
        throw new ApplicationException(ErrorCode.DUPLICATE_ROLE_IDS);
      }
    }
  }

  public void checkDuplicateRoleIds(List<String> roleIds) {
    Set<String> uniqueIds = new HashSet<>(roleIds);
    if (uniqueIds.size() < roleIds.size()) {
      throw new ApplicationException(ErrorCode.DUPLICATE_ROLE_IDS);
    }
  }

  public Set<String> getRoleIdsFromUser(User user) {
    return user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toSet());
  }
}
