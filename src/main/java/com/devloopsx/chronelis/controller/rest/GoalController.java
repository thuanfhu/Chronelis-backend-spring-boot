package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.goal.CreateGoalRequest;
import com.devloopsx.chronelis.dto.request.goal.UpdateGoalRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.goal.GoalResponse;
import com.devloopsx.chronelis.service.GoalService;
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
@RequestMapping("/api/v1/goals")
public class GoalController {
    GoalService goalService;

    @PostMapping
    ApiResponse<GoalResponse> createGoal(@RequestBody @Valid CreateGoalRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<GoalResponse>builder()
                .message("Tạo goal thành công")
                .data(goalService.createGoal(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{goalId}")
    ApiResponse<GoalResponse> updateGoal(@PathVariable Long goalId,
            @RequestBody @Valid UpdateGoalRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<GoalResponse>builder()
                .message("Cập nhật goal thành công")
                .data(goalService.updateGoal(goalId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{goalId}")
    ApiResponse<GoalResponse> getGoal(@PathVariable Long goalId, HttpServletRequest servletRequest) {
        return ApiResponse.<GoalResponse>builder()
                .message("Lấy chi tiết goal thành công")
                .data(goalService.getGoal(goalId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/project/{projectId}")
    ApiResponse<PaginationResponse> listGoalsByProject(@PathVariable Long projectId, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy danh sách goal theo project thành công")
                .data(goalService.listGoalsByProject(projectId, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{goalId}")
    ApiResponse<Void> deleteGoal(@PathVariable Long goalId, HttpServletRequest servletRequest) {
        goalService.deleteGoal(goalId);
        return ApiResponse.<Void>builder()
                .message("Xóa goal thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
