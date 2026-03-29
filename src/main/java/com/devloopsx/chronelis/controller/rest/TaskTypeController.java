package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.tasktype.CreateTaskTypeRequest;
import com.devloopsx.chronelis.dto.request.tasktype.UpdateTaskTypeRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.tasktype.TaskTypeResponse;
import com.devloopsx.chronelis.service.TaskTypeService;
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
@RequestMapping("/api/v1/task-types")
public class TaskTypeController {
    TaskTypeService taskTypeService;

    @PostMapping
    ApiResponse<TaskTypeResponse> createTaskType(@RequestBody @Valid CreateTaskTypeRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskTypeResponse>builder()
                .message("Tạo task type thành công")
                .data(taskTypeService.createTaskType(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskTypeId}")
    ApiResponse<TaskTypeResponse> updateTaskType(@PathVariable Long taskTypeId,
            @RequestBody @Valid UpdateTaskTypeRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskTypeResponse>builder()
                .message("Cập nhật task type thành công")
                .data(taskTypeService.updateTaskType(taskTypeId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{taskTypeId}")
    ApiResponse<TaskTypeResponse> getTaskType(@PathVariable Long taskTypeId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskTypeResponse>builder()
                .message("Lấy chi tiết task type thành công")
                .data(taskTypeService.getTaskType(taskTypeId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/project/{projectId}")
    ApiResponse<List<TaskTypeResponse>> listByProject(@PathVariable Long projectId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskTypeResponse>>builder()
                .message("Lấy danh sách task type theo project thành công")
                .data(taskTypeService.listByProject(projectId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{taskTypeId}")
    ApiResponse<Void> deleteTaskType(@PathVariable Long taskTypeId,
            HttpServletRequest servletRequest) {
        taskTypeService.deleteTaskType(taskTypeId);
        return ApiResponse.<Void>builder()
                .message("Xóa task type thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
