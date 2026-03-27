package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.taskcomment.CreateTaskCommentRequest;
import com.devloopsx.chronelis.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.taskcomment.TaskCommentResponse;
import com.devloopsx.chronelis.service.TaskCommentService;
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
@RequestMapping("/api/v1/task-comments")
public class TaskCommentController {
    TaskCommentService taskCommentService;

    @PostMapping
    ApiResponse<TaskCommentResponse> addComment(@RequestBody @Valid CreateTaskCommentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskCommentResponse>builder()
                .message("Thêm bình luận thành công")
                .data(taskCommentService.addComment(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{commentId}")
    ApiResponse<TaskCommentResponse> updateComment(@PathVariable Long commentId,
            @RequestBody @Valid UpdateTaskCommentRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskCommentResponse>builder()
                .message("Cập nhật bình luận thành công")
                .data(taskCommentService.updateComment(commentId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable Long commentId, HttpServletRequest servletRequest) {
        taskCommentService.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Xóa bình luận thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/task/{taskId}")
    ApiResponse<List<TaskCommentResponse>> listCommentsByTask(@PathVariable Long taskId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskCommentResponse>>builder()
                .message("Lấy danh sách bình luận theo task thành công")
                .data(taskCommentService.listCommentsByTask(taskId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
