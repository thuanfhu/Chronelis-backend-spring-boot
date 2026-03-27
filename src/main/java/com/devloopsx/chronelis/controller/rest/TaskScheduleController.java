package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.taskschedule.CreateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.request.taskschedule.UpdateTaskScheduleRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.taskschedule.TaskScheduleResponse;
import com.devloopsx.chronelis.service.TaskScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/task-schedules")
public class TaskScheduleController {
    TaskScheduleService taskScheduleService;

    @PostMapping
    ApiResponse<TaskScheduleResponse> createSchedule(@RequestBody @Valid CreateTaskScheduleRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskScheduleResponse>builder()
                .message("Tạo lịch task thành công")
                .data(taskScheduleService.createSchedule(request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{scheduleId}")
    ApiResponse<TaskScheduleResponse> updateSchedule(@PathVariable Long scheduleId,
            @RequestBody @Valid UpdateTaskScheduleRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.<TaskScheduleResponse>builder()
                .message("Cập nhật lịch task thành công")
                .data(taskScheduleService.updateSchedule(scheduleId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{scheduleId}")
    ApiResponse<Void> deleteSchedule(@PathVariable Long scheduleId, HttpServletRequest servletRequest) {
        taskScheduleService.deleteSchedule(scheduleId);
        return ApiResponse.<Void>builder()
                .message("Xóa lịch task thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/task/{taskId}")
    ApiResponse<List<TaskScheduleResponse>> listByTask(@PathVariable Long taskId, HttpServletRequest servletRequest) {
        return ApiResponse.<List<TaskScheduleResponse>>builder()
                .message("Lấy danh sách lịch theo task thành công")
                .data(taskScheduleService.listSchedulesByTask(taskId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/calendar/project/{projectId}")
    ApiResponse<PaginationResponse> getProjectCalendar(@PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy dữ liệu lịch theo project thành công")
                .data(taskScheduleService.getProjectCalendar(projectId, fromDate, toDate, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/calendar/workspace/{workspaceId}")
    ApiResponse<PaginationResponse> getWorkspaceCalendar(@PathVariable Long workspaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy dữ liệu lịch theo workspace thành công")
                .data(taskScheduleService.getWorkspaceCalendar(workspaceId, fromDate, toDate, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
