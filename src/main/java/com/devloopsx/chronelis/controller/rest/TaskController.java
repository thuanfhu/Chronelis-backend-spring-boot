package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.task.*;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.task.TaskResponse;
import com.devloopsx.chronelis.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/tasks")
public class TaskController {
    TaskService taskService;

    @PostMapping
    ApiResponse<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Tạo task thành công")
                .data(taskService.createTask(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskId}")
    ApiResponse<TaskResponse> updateTask(@PathVariable Long taskId,
            @RequestBody @Valid UpdateTaskRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Cập nhật task thành công")
                .data(taskService.updateTask(taskId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{taskId}")
    ApiResponse<TaskResponse> getTask(@PathVariable Long taskId, HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Lấy chi tiết task thành công")
                .data(taskService.getTask(taskId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/project/{projectId}")
    ApiResponse<PaginationResponse> listTasksByProject(@PathVariable Long projectId, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy danh sách task theo project thành công")
                .data(taskService.listTasksByProject(projectId, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/goal/{goalId}")
    ApiResponse<PaginationResponse> listTasksByGoal(@PathVariable Long goalId, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy danh sách task theo goal thành công")
                .data(taskService.listTasksByGoal(goalId, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskId}/move")
    ApiResponse<TaskResponse> moveTask(@PathVariable Long taskId, @RequestBody @Valid MoveTaskRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Di chuyển task thành công")
                .data(taskService.moveTask(taskId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskId}/reorder")
    ApiResponse<TaskResponse> reorderTask(@PathVariable Long taskId,
            @RequestBody @Valid ReorderTaskRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Reorder task thành công")
                .data(taskService.reorderTask(taskId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskId}/assignee")
    ApiResponse<TaskResponse> assignTask(@PathVariable Long taskId,
            @RequestBody @Valid AssignTaskRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Cập nhật người phụ trách task thành công")
                .data(taskService.assignTask(taskId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{taskId}/completion")
    ApiResponse<TaskResponse> updateCompletion(@PathVariable Long taskId,
            @RequestBody @Valid UpdateTaskCompletionRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskResponse>builder()
                .message("Cập nhật trạng thái hoàn thành task thành công")
                .data(taskService.updateTaskCompletion(taskId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{taskId}")
    ApiResponse<Void> deleteTask(@PathVariable Long taskId, HttpServletRequest servletRequest) {
        taskService.deleteTask(taskId);
        return ApiResponse.<Void>builder()
                .message("Xóa task thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
