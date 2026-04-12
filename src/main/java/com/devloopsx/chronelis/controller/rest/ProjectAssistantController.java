package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantApplyRequest;
import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantPreviewRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantApplyResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantPreviewResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantStatusResponse;
import com.devloopsx.chronelis.service.ProjectAssistantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/project-assistant")
public class ProjectAssistantController {
    ProjectAssistantService projectAssistantService;

    @GetMapping("/status")
    ApiResponse<ProjectAssistantStatusResponse> getStatus(HttpServletRequest servletRequest) {
        return ApiResponse.<ProjectAssistantStatusResponse>builder()
                .message("Lấy trạng thái project assistant thành công")
                .data(this.projectAssistantService.getStatus())
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PostMapping("/projects/{projectId}/preview")
    ApiResponse<ProjectAssistantPreviewResponse> previewProject(@PathVariable Long projectId,
            @RequestBody @Valid ProjectAssistantPreviewRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<ProjectAssistantPreviewResponse>builder()
                .message("Tạo preview AI cho project thành công")
                .data(this.projectAssistantService.previewProject(projectId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PostMapping("/projects/{projectId}/apply")
    ApiResponse<ProjectAssistantApplyResponse> applyPlan(@PathVariable Long projectId,
            @RequestBody @Valid ProjectAssistantApplyRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<ProjectAssistantApplyResponse>builder()
                .message("Áp dụng kế hoạch AI cho project thành công")
                .data(this.projectAssistantService.applyPlan(projectId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}