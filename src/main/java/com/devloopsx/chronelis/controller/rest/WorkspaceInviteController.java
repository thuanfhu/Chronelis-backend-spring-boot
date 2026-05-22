package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.invite.CreateWorkspaceInviteRequest;
import com.devloopsx.chronelis.dto.request.invite.JoinByInviteRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.invite.WorkspaceInviteResponse;
import com.devloopsx.chronelis.service.WorkspaceInviteService;
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
@RequestMapping("/api/v1/workspace-invites")
public class WorkspaceInviteController {
  WorkspaceInviteService workspaceInviteService;

  @PostMapping
  ApiResponse<WorkspaceInviteResponse> createInvite(
      @RequestBody @Valid CreateWorkspaceInviteRequest request, HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceInviteResponse>builder()
        .message("Tạo lời mời thành công")
        .data(workspaceInviteService.createInvite(request))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/workspace/{workspaceId}")
  ApiResponse<List<WorkspaceInviteResponse>> listActiveInvites(
      @PathVariable Long workspaceId, HttpServletRequest servletRequest) {
    return ApiResponse.<List<WorkspaceInviteResponse>>builder()
        .message("Lấy danh sách lời mời thành công")
        .data(workspaceInviteService.listActiveInvites(workspaceId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{inviteId}/revoke")
  ApiResponse<Void> revokeInvite(@PathVariable Long inviteId, HttpServletRequest servletRequest) {
    workspaceInviteService.revokeInvite(inviteId);
    return ApiResponse.<Void>builder()
        .message("Thu hồi lời mời thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/validate/{inviteCode}")
  ApiResponse<WorkspaceInviteResponse> validateInviteCode(
      @PathVariable String inviteCode, HttpServletRequest servletRequest) {
    return ApiResponse.<WorkspaceInviteResponse>builder()
        .message("Mã mời hợp lệ")
        .data(workspaceInviteService.validateInviteCode(inviteCode))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/join")
  ApiResponse<Void> joinByInvite(
      @RequestBody @Valid JoinByInviteRequest request, HttpServletRequest servletRequest) {
    workspaceInviteService.joinByInvite(request);
    return ApiResponse.<Void>builder()
        .message("Tham gia workspace thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
