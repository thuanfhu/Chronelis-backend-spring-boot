package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.taskstatus.CreateTaskStatusRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.ReorderTaskStatusesRequest;
import com.devloopsx.chronelis.dto.request.taskstatus.UpdateTaskStatusRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.taskstatus.TaskStatusResponse;
import com.devloopsx.chronelis.service.TaskStatusService;
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
@RequestMapping("/api/v1/task-statuses")
public class TaskStatusController {
    TaskStatusService taskStatusService;

    @PostMapping
    ApiResponse<TaskStatusResponse> createStatus(@RequestBody @Valid CreateTaskStatusRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskStatusResponse>builder()
                .message("Tạo cột Kanban thành công")
                .data(taskStatusService.createStatus(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/project/{projectId}")
    ApiResponse<List<TaskStatusResponse>> listByProject(@PathVariable Long projectId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskStatusResponse>>builder()
                .message("Lấy danh sách cột Kanban thành công")
                .data(taskStatusService.listStatusesByProject(projectId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{statusId}")
    ApiResponse<TaskStatusResponse> updateStatus(@PathVariable Long statusId,
            @RequestBody @Valid UpdateTaskStatusRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskStatusResponse>builder()
                .message("Cập nhật cột Kanban thành công")
                .data(taskStatusService.updateStatus(statusId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/project/{projectId}/reorder")
    ApiResponse<List<TaskStatusResponse>> reorderStatuses(@PathVariable Long projectId,
            @RequestBody @Valid ReorderTaskStatusesRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskStatusResponse>>builder()
                .message("Reorder cột Kanban thành công")
                .data(taskStatusService.reorderStatuses(projectId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{statusId}")
    ApiResponse<Void> deleteStatus(@PathVariable Long statusId, HttpServletRequest servletRequest) {
        taskStatusService.deleteStatus(statusId);
        return ApiResponse.<Void>builder()
                .message("Xóa cột Kanban thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
