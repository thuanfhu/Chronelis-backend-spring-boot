package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/activity-logs")
public class ActivityLogController {
    ActivityLogService activityLogService;

    @GetMapping("/workspace/{workspaceId}")
    ApiResponse<PaginationResponse> listByWorkspace(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) ActivityActionType actionType,
            @RequestParam(required = false) ActivityTargetType targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Lấy danh sách activity log thành công")
                .data(activityLogService.listByWorkspace(workspaceId, actorId, actionType, targetType, fromDateTime,
                        toDateTime, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
