package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.notification.NotificationUnreadCountResponse;
import com.devloopsx.chronelis.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  NotificationService notificationService;

  @GetMapping
  ApiResponse<PaginationResponse> listMyNotifications(
      Pageable pageable, HttpServletRequest servletRequest) {
    return ApiResponse.<PaginationResponse>builder()
        .message("Lấy danh sách thông báo thành công")
        .data(notificationService.listMyNotifications(pageable))
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @GetMapping("/unread-count")
  ApiResponse<NotificationUnreadCountResponse> unreadCount(HttpServletRequest servletRequest) {
    return ApiResponse.<NotificationUnreadCountResponse>builder()
        .message("Lấy số lượng thông báo chưa đọc thành công")
        .data(notificationService.getUnreadCount())
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/{notificationId}/read")
  ApiResponse<Void> markOneAsRead(
      @PathVariable Long notificationId, HttpServletRequest servletRequest) {
    notificationService.markOneAsRead(notificationId);
    return ApiResponse.<Void>builder()
        .message("Đánh dấu thông báo đã đọc thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PatchMapping("/read-all")
  ApiResponse<Void> markAllAsRead(HttpServletRequest servletRequest) {
    notificationService.markAllAsRead();
    return ApiResponse.<Void>builder()
        .message("Đánh dấu tất cả thông báo đã đọc thành công")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
