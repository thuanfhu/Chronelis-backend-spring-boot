package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.projectaccess.UpdateProjectAccessRequest;
import com.devloopsx.chronelis.dto.request.projectaccess.UpsertProjectAccessRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.dto.response.projectaccess.ProjectAccessResponse;
import com.devloopsx.chronelis.service.ProjectAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/projects/{projectId}/access")
public class ProjectAccessController {
    ProjectAccessService projectAccessService;

    @GetMapping
    ApiResponse<List<ProjectAccessResponse>> listProjectAccess(@PathVariable Long projectId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<ProjectAccessResponse>>builder()
                .message("Lấy danh sách quyền truy cập project thành công")
                .data(projectAccessService.listProjectAccess(projectId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<EffectiveProjectAccessResponse> getCurrentUserEffectiveAccess(@PathVariable Long projectId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<EffectiveProjectAccessResponse>builder()
                .message("Lấy quyền hiệu lực project thành công")
                .data(projectAccessService.getCurrentUserEffectiveAccess(projectId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PostMapping
    ApiResponse<ProjectAccessResponse> upsertProjectAccess(@PathVariable Long projectId,
            @RequestBody @Valid UpsertProjectAccessRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<ProjectAccessResponse>builder()
                .message("Cập nhật quyền truy cập project thành công")
                .data(projectAccessService.upsertProjectAccess(projectId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{accessId}")
    ApiResponse<ProjectAccessResponse> updateProjectAccess(@PathVariable Long projectId,
            @PathVariable Long accessId,
            @RequestBody @Valid UpdateProjectAccessRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<ProjectAccessResponse>builder()
                .message("Cập nhật quyền truy cập project thành công")
                .data(projectAccessService.updateProjectAccess(projectId, accessId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{accessId}")
    ApiResponse<Void> revokeProjectAccess(@PathVariable Long projectId, @PathVariable Long accessId,
            HttpServletRequest servletRequest) {
        projectAccessService.revokeProjectAccess(projectId, accessId);
        return ApiResponse.<Void>builder()
                .message("Thu hồi quyền truy cập project thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
