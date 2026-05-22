package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.project.CreateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectRequest;
import com.devloopsx.chronelis.dto.request.project.UpdateProjectStatusRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectAnalyticsResponse;
import com.devloopsx.chronelis.dto.response.project.ProjectResponse;
import com.devloopsx.chronelis.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/projects")
public class ProjectController {
  ProjectService projectService;

  @PostMapping
  ApiResponse<ProjectResponse> createProject(
      @RequestBody @Valid CreateProjectRequest request, HttpServletRequest servletRequest) {
    return ApiResponse.<ProjectResponse>builder()
        .message("Tạo project thành công")
        .data(projectService.createProject(request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{projectId}")
  ApiResponse<ProjectResponse> updateProject(
      @PathVariable Long projectId,
      @RequestBody @Valid UpdateProjectRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.<ProjectResponse>builder()
        .message("Cập nhật project thành công")
        .data(projectService.updateProject(projectId, request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{projectId}/status")
  ApiResponse<ProjectResponse> updateProjectStatus(
      @PathVariable Long projectId,
      @RequestBody @Valid UpdateProjectStatusRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.<ProjectResponse>builder()
        .message("Cập nhật trạng thái project thành công")
        .data(projectService.updateProjectStatus(projectId, request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/{projectId}")
  ApiResponse<ProjectResponse> getProject(
      @PathVariable Long projectId, HttpServletRequest servletRequest) {
    return ApiResponse.<ProjectResponse>builder()
        .message("Lấy chi tiết project thành công")
        .data(projectService.getProject(projectId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/workspace/{workspaceId}")
  ApiResponse<PaginationResponse> listProjectsByWorkspace(
      @PathVariable Long workspaceId, Pageable pageable, HttpServletRequest servletRequest) {
    return ApiResponse.<PaginationResponse>builder()
        .message("Lấy danh sách project theo workspace thành công")
        .data(projectService.listProjectsByWorkspace(workspaceId, pageable))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/{projectId}")
  ApiResponse<Void> deleteProject(@PathVariable Long projectId, HttpServletRequest servletRequest) {
    projectService.deleteProject(projectId);
    return ApiResponse.<Void>builder()
        .message("Xóa project thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/{projectId}/analytics")
  ApiResponse<ProjectAnalyticsResponse> getProjectAnalytics(
      @PathVariable Long projectId, HttpServletRequest servletRequest) {
    return ApiResponse.<ProjectAnalyticsResponse>builder()
        .message("Lấy phân tích project thành công")
        .data(projectService.getProjectAnalytics(projectId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
