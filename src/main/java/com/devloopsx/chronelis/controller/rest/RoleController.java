package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.dto.request.role.CreateRoleRequest;
import com.devloopsx.chronelis.dto.request.role.DeletePermissionFromRoleRequest;
import com.devloopsx.chronelis.dto.request.role.UpdateRoleRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.role.RoleResponse;
import com.devloopsx.chronelis.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/roles")
public class RoleController {

  RoleService roleService;

  @PostMapping
  ApiResponse<RoleResponse> createRole(
      @RequestBody @Valid CreateRoleRequest createRoleRequest, HttpServletRequest servletRequest) {
    return ApiResponse.<RoleResponse>builder()
        .message("Tạo vai trò thành công")
        .data(roleService.createRole(createRoleRequest))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/{roleId}")
  ApiResponse<RoleResponse> getRoleById(
      @PathVariable String roleId, HttpServletRequest servletRequest) {
    return ApiResponse.<RoleResponse>builder()
        .message("Lấy thông tin vai trò thành công")
        .data(roleService.getRoleById(roleId))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping
  ApiResponse<PaginationResponse> getAllRoleWithQuery(
      @Filter Specification<Role> spec, Pageable pageable, HttpServletRequest servletRequest) {
    return ApiResponse.<PaginationResponse>builder()
        .message("Lấy danh sách vai trò thành công với bộ lọc truy vấn")
        .data(roleService.getAllRoleWithQuery(spec, pageable))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{roleId}")
  ApiResponse<RoleResponse> updateRoleById(
      @PathVariable String roleId,
      @RequestBody UpdateRoleRequest updateRoleRequest,
      HttpServletRequest servletRequest) {
    return ApiResponse.<RoleResponse>builder()
        .message("Cập nhật vai trò thành công")
        .data(roleService.updateRoleById(roleId, updateRoleRequest))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/{roleId}/permissions")
  ApiResponse<Void> deletePermissionFromRole(
      @PathVariable String roleId,
      @RequestBody DeletePermissionFromRoleRequest deletePermissionFromRoleRequest,
      HttpServletRequest servletRequest) {
    roleService.deletePermissionFromRole(roleId, deletePermissionFromRoleRequest);
    return ApiResponse.<Void>builder()
        .message("Xóa quyền khỏi vai trò thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/{roleId}")
  ApiResponse<Void> deleteRoleById(@PathVariable String roleId, HttpServletRequest servletRequest) {
    roleService.deleteRoleById(roleId);
    return ApiResponse.<Void>builder()
        .message("Xóa vai trò thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
