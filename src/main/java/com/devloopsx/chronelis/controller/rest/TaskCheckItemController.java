package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.checkitem.CreateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.request.checkitem.ReorderTaskCheckItemsRequest;
import com.devloopsx.chronelis.dto.request.checkitem.UpdateTaskCheckItemRequest;
import com.devloopsx.chronelis.dto.response.checkitem.TaskCheckItemResponse;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.service.TaskCheckItemService;
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
@RequestMapping("/api/v1/task-check-items")
public class TaskCheckItemController {
    TaskCheckItemService taskCheckItemService;

    @PostMapping
    ApiResponse<TaskCheckItemResponse> createCheckItem(@RequestBody @Valid CreateTaskCheckItemRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskCheckItemResponse>builder()
                .message("Tạo check item thành công")
                .data(taskCheckItemService.createCheckItem(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{checkItemId}")
    ApiResponse<TaskCheckItemResponse> updateCheckItem(@PathVariable Long checkItemId,
            @RequestBody @Valid UpdateTaskCheckItemRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskCheckItemResponse>builder()
                .message("Cập nhật check item thành công")
                .data(taskCheckItemService.updateCheckItem(checkItemId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{checkItemId}/toggle")
    ApiResponse<TaskCheckItemResponse> toggleCheckItem(@PathVariable Long checkItemId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskCheckItemResponse>builder()
                .message("Đổi trạng thái check item thành công")
                .data(taskCheckItemService.toggleCheckItem(checkItemId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{checkItemId}")
    ApiResponse<Void> deleteCheckItem(@PathVariable Long checkItemId,
            HttpServletRequest servletRequest) {
        taskCheckItemService.deleteCheckItem(checkItemId);
        return ApiResponse.<Void>builder()
                .message("Xóa check item thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/task/{taskId}")
    ApiResponse<List<TaskCheckItemResponse>> listByTask(@PathVariable Long taskId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskCheckItemResponse>>builder()
                .message("Lấy danh sách check item theo task thành công")
                .data(taskCheckItemService.listByTask(taskId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/reorder")
    ApiResponse<Void> reorderCheckItems(@RequestBody @Valid ReorderTaskCheckItemsRequest request,
            HttpServletRequest servletRequest) {
        taskCheckItemService.reorderCheckItems(request);
        return ApiResponse.<Void>builder()
                .message("Sắp xếp lại check items thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
