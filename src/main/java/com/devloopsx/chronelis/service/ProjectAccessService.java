package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.projectaccess.UpdateProjectAccessRequest;
import com.devloopsx.chronelis.dto.request.projectaccess.UpsertProjectAccessRequest;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.dto.response.projectaccess.ProjectAccessResponse;
import java.util.List;

public interface ProjectAccessService {
  List<ProjectAccessResponse> listProjectAccess(Long projectId);

  ProjectAccessResponse upsertProjectAccess(Long projectId, UpsertProjectAccessRequest request);

  ProjectAccessResponse updateProjectAccess(
      Long projectId, Long accessId, UpdateProjectAccessRequest request);

  void revokeProjectAccess(Long projectId, Long accessId);

  EffectiveProjectAccessResponse getCurrentUserEffectiveAccess(Long projectId);
}
