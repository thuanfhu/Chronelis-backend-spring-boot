package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import java.util.Set;

public interface ProjectPermissionService {
  EffectiveProjectAccessResponse resolveCurrentUserAccess(Long projectId);

  EffectiveProjectAccessResponse resolveCurrentUserAccess(Project project);

  EffectiveProjectAccessRoleType resolveCurrentUserRole(Project project);

  EffectiveProjectAccessRoleType resolveUserRole(Project project, String userId);

  Set<String> findAuthorizedUserIds(Project project);
}
