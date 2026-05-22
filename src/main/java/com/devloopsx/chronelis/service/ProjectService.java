package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.project.CreateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectStatusRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectAnalyticsResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
  ProjectResponse createProject(CreateProjectRequest request);

  ProjectResponse updateProject(Long projectId, UpdateProjectRequest request);

  ProjectResponse updateProjectStatus(Long projectId, UpdateProjectStatusRequest request);

  ProjectResponse getProject(Long projectId);

  PaginationResponse listProjectsByWorkspace(Long workspaceId, Pageable pageable);

  void deleteProject(Long projectId);

  ProjectAnalyticsResponse getProjectAnalytics(Long projectId);
}
