package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.team.AddTeamMemberRequest;
import com.devloopsx.chronelis.dto.request.team.CreateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.request.team.UpdateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamMemberResponse;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamResponse;
import com.devloopsx.chronelis.service.WorkspaceTeamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/workspace-teams")
public class WorkspaceTeamController {
  WorkspaceTeamService workspaceTeamService;

  @PostMapping
  ApiResponse<WorkspaceTeamResponse> createTeam(
      @RequestBody @Valid CreateWorkspaceTeamRequest request, HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceTeamResponse>builder()
        .message("Tạo team thành công")
        .data(workspaceTeamService.createTeam(request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{teamId}")
  ApiResponse<WorkspaceTeamResponse> updateTeam(
      @PathVariable Long teamId,
      @RequestBody @Valid UpdateWorkspaceTeamRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceTeamResponse>builder()
        .message("Cập nhật team thành công")
        .data(workspaceTeamService.updateTeam(teamId, request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/{teamId}")
  ApiResponse<WorkspaceTeamResponse> getTeam(
      @PathVariable Long teamId, HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceTeamResponse>builder()
        .message("Lấy chi tiết team thành công")
        .data(workspaceTeamService.getTeam(teamId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/workspace/{workspaceId}")
  ApiResponse<List<WorkspaceTeamResponse>> listByWorkspace(
      @PathVariable Long workspaceId, HttpServletRequest servletRequest) {
    return ApiResponse.<List<WorkspaceTeamResponse>>builder()
        .message("Lấy danh sách team theo workspace thành công")
        .data(workspaceTeamService.listByWorkspace(workspaceId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/{teamId}")
  ApiResponse<Void> deleteTeam(@PathVariable Long teamId, HttpServletRequest servletRequest) {
    workspaceTeamService.deleteTeam(teamId);
    return ApiResponse.<Void>builder()
        .message("Xóa team thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/{teamId}/members")
  ApiResponse<WorkspaceTeamMemberResponse> addMember(
      @PathVariable Long teamId,
      @RequestBody @Valid AddTeamMemberRequest request,
      HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceTeamMemberResponse>builder()
        .message("Thêm thành viên vào team thành công")
        .data(workspaceTeamService.addMember(teamId, request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/{teamId}/members/{userId}")
  ApiResponse<Void> removeMember(
      @PathVariable Long teamId, @PathVariable String userId, HttpServletRequest servletRequest) {
    workspaceTeamService.removeMember(teamId, userId);
    return ApiResponse.<Void>builder()
        .message("Xóa thành viên khỏi team thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/{teamId}/members")
  ApiResponse<List<WorkspaceTeamMemberResponse>> listMembers(
      @PathVariable Long teamId, HttpServletRequest servletRequest) {
    return ApiResponse.<List<WorkspaceTeamMemberResponse>>builder()
        .message("Lấy danh sách thành viên team thành công")
        .data(workspaceTeamService.listMembers(teamId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
