package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.constant.NotificationType;
import com.devloopsx.chronelis.constant.ReferenceType;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.notification.NotificationUnreadCountResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PaginationResponse listMyNotifications(Pageable pageable);

    NotificationUnreadCountResponse getUnreadCount();

    void markOneAsRead(Long notificationId);

    void markAllAsRead();

    void createAndPublish(String recipientUserId, NotificationType type, String title, String message,
            ReferenceType referenceType, Long referenceId);
}
