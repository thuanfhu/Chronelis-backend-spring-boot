package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.workspace.AddWorkspaceMemberRequest;
import com.devloopsx.chronelis.dto.request.workspace.CreateWorkspaceRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceMemberRoleRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceMemberResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceResponse;
import com.devloopsx.chronelis.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
        WorkspaceService workspaceService;

        @PostMapping
        ApiResponse<WorkspaceResponse> createWorkspace(@RequestBody @Valid CreateWorkspaceRequest request,
                        HttpServletRequest servletRequest) {
                return ApiResponse.<WorkspaceResponse>builder()
                                .message("Tạo workspace thành công")
                                .data(workspaceService.createWorkspace(request))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @PatchMapping("/{workspaceId}")
        ApiResponse<WorkspaceResponse> updateWorkspace(@PathVariable Long workspaceId,
                        @RequestBody @Valid UpdateWorkspaceRequest request,
                        HttpServletRequest servletRequest) {
                return ApiResponse.<WorkspaceResponse>builder()
                                .message("Cập nhật workspace thành công")
                                .data(workspaceService.updateWorkspace(workspaceId, request))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @GetMapping("/{workspaceId}")
        ApiResponse<WorkspaceResponse> getWorkspace(@PathVariable Long workspaceId, HttpServletRequest servletRequest) {
                return ApiResponse.<WorkspaceResponse>builder()
                                .message("Lấy chi tiết workspace thành công")
                                .data(workspaceService.getWorkspace(workspaceId))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @GetMapping
        ApiResponse<PaginationResponse> listVisibleWorkspaces(Pageable pageable, HttpServletRequest servletRequest) {
                return ApiResponse.<PaginationResponse>builder()
                                .message("Lấy danh sách workspace thành công")
                                .data(workspaceService.listVisibleWorkspaces(pageable))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @PostMapping("/{workspaceId}/members")
        ApiResponse<WorkspaceMemberResponse> addMember(@PathVariable Long workspaceId,
                        @RequestBody @Valid AddWorkspaceMemberRequest request,
                        HttpServletRequest servletRequest) {
                return ApiResponse.<WorkspaceMemberResponse>builder()
                                .message("Thêm thành viên workspace thành công")
                                .data(workspaceService.addMember(workspaceId, request))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @GetMapping("/{workspaceId}/members")
        ApiResponse<List<WorkspaceMemberResponse>> listMembers(@PathVariable Long workspaceId,
                        HttpServletRequest servletRequest) {
                return ApiResponse.<List<WorkspaceMemberResponse>>builder()
                                .message("Lấy danh sách thành viên workspace thành công")
                                .data(workspaceService.listMembers(workspaceId))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @PatchMapping("/{workspaceId}/members/{userId}/role")
        ApiResponse<WorkspaceMemberResponse> updateMemberRole(@PathVariable Long workspaceId,
                        @PathVariable String userId,
                        @RequestBody @Valid UpdateWorkspaceMemberRoleRequest request,
                        HttpServletRequest servletRequest) {
                return ApiResponse.<WorkspaceMemberResponse>builder()
                                .message("Cập nhật vai trò thành viên thành công")
                                .data(workspaceService.updateMemberRole(workspaceId, userId, request))
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @DeleteMapping("/{workspaceId}/members/{userId}")
        ApiResponse<Void> removeMember(@PathVariable Long workspaceId, @PathVariable String userId,
                        HttpServletRequest servletRequest) {
                workspaceService.removeMember(workspaceId, userId);
                return ApiResponse.<Void>builder()
                                .message("Xóa thành viên khỏi workspace thành công")
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }

        @DeleteMapping("/{workspaceId}")
        ApiResponse<Void> deleteWorkspace(@PathVariable Long workspaceId, HttpServletRequest servletRequest) {
                workspaceService.deleteWorkspace(workspaceId);
                return ApiResponse.<Void>builder()
                                .message("Xóa workspace thành công")
                                .meta(buildMetaInfo(servletRequest))
                                .build();
        }
}
