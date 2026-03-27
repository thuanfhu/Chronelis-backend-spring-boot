package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.dto.request.permission.CreateModuleRequest;
import com.devloopsx.chronelis.dto.request.permission.CreatePermissionRequest;
import com.devloopsx.chronelis.dto.request.permission.UpdatePermissionRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.permission.PermissionResponse;
import com.devloopsx.chronelis.service.PermissionService;
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

import java.util.List;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/permissions")
public class PermissionController {

	PermissionService permissionService;

	@PostMapping("/module")
	ApiResponse<List<PermissionResponse>> createModuleForPermissions(
			@RequestBody @Valid CreateModuleRequest createModuleRequest, HttpServletRequest servletRequest) {
		return ApiResponse.<List<PermissionResponse>>builder().message("Tạo module thành công")
				.data(permissionService.createModuleForPermissions(createModuleRequest))
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@DeleteMapping("/module/{name}")
	ApiResponse<Void> deleteModuleByName(@PathVariable("name") String moduleName, HttpServletRequest servletRequest) {
		permissionService.deleteModuleByName(moduleName);
		return ApiResponse.<Void>builder().message("Xóa module thành công").meta(buildMetaInfo(servletRequest)).build();
	}

	@GetMapping("/modules")
	ApiResponse<List<String>> getAllModules(HttpServletRequest servletRequest) {
		return ApiResponse.<List<String>>builder().message("Lấy danh sách tên module thành công")
				.data(permissionService.getAllModules()).meta(buildMetaInfo(servletRequest)).build();
	}

	@PostMapping
	ApiResponse<PermissionResponse> createPermission(
			@RequestBody @Valid CreatePermissionRequest createPermissionRequest, HttpServletRequest servletRequest) {
		return ApiResponse.<PermissionResponse>builder().message("Tạo quyền thành công")
				.data(permissionService.createPermission(createPermissionRequest)).meta(buildMetaInfo(servletRequest))
				.build();
	}

	@PatchMapping("/{permissionId}")
	ApiResponse<PermissionResponse> updatePermissionById(
			@RequestBody @Valid UpdatePermissionRequest updatePermissionRequest, @PathVariable String permissionId,
			HttpServletRequest servletRequest) {
		return ApiResponse.<PermissionResponse>builder().message("Cập nhật quyền thành công")
				.data(permissionService.updatePermissionById(permissionId, updatePermissionRequest))
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@GetMapping("/{permissionId}")
	ApiResponse<PermissionResponse> getPermissionById(@PathVariable String permissionId,
			HttpServletRequest servletRequest) {
		return ApiResponse.<PermissionResponse>builder().message("Lấy thông tin quyền thành công")
				.data(permissionService.getPermissionById(permissionId)).meta(buildMetaInfo(servletRequest)).build();
	}

	@GetMapping
	ApiResponse<PaginationResponse> getAllPermissionWithQuery(@Filter Specification<Permission> spec, Pageable pageable,
			HttpServletRequest servletRequest) {
		return ApiResponse.<PaginationResponse>builder().message("Lấy danh sách quyền thành công với bộ lọc truy vấn")
				.data(permissionService.getAllPermissionWithQuery(spec, pageable)).meta(buildMetaInfo(servletRequest))
				.build();
	}

	@DeleteMapping("/{permissionId}")
	ApiResponse<Void> deletePermissionById(@PathVariable String permissionId, HttpServletRequest servletRequest) {
		permissionService.deletePermissionById(permissionId);
		return ApiResponse.<Void>builder().message("Xóa quyền thành công").meta(buildMetaInfo(servletRequest)).build();
	}
}
