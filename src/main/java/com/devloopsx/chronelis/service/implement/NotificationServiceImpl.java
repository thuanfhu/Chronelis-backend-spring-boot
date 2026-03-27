package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.NotificationType;
import com.devloopsx.chronelis.constant.ReferenceType;
import com.devloopsx.chronelis.domain.Notification;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.notification.NotificationUnreadCountResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.NotificationMapper;
import com.devloopsx.chronelis.repository.NotificationRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.service.NotificationService;
import com.devloopsx.chronelis.service.RealtimeEventPublisherService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;
    RealtimeEventPublisherService realtimeEventPublisherService;
    SecurityUtils securityUtils;

    @Override
    public PaginationResponse listMyNotifications(Pageable pageable) {
        User currentUser = securityUtils.getAuthenticatedUser();
        Page<Notification> page = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(currentUser.getUserId(),
                pageable);

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .content(page.getContent().stream().map(notificationMapper::toResponse).toList())
                .build();
    }

    @Override
    public NotificationUnreadCountResponse getUnreadCount() {
        User currentUser = securityUtils.getAuthenticatedUser();
        long unreadCount = notificationRepository.countByUserUserIdAndIsReadFalse(currentUser.getUserId());
        return NotificationUnreadCountResponse.builder().unreadCount(unreadCount).build();
    }

    @Override
    @Transactional
    public void markOneAsRead(Long notificationId) {
        User currentUser = securityUtils.getAuthenticatedUser();
        int affectedRows = notificationRepository.markOneAsRead(notificationId, currentUser.getUserId());
        if (affectedRows == 0) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Notification không tồn tại");
        }

        long unreadCount = notificationRepository.countByUserUserIdAndIsReadFalse(currentUser.getUserId());
        realtimeEventPublisherService.publishUnreadCount(currentUser.getUserId(), unreadCount);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = securityUtils.getAuthenticatedUser();
        notificationRepository.markAllAsRead(currentUser.getUserId());
        realtimeEventPublisherService.publishUnreadCount(currentUser.getUserId(), 0);
    }

    @Override
    @Transactional
    public void createAndPublish(String recipientUserId, NotificationType type, String title, String message,
            ReferenceType referenceType, Long referenceId) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        realtimeEventPublisherService.publishNotificationEvent(recipientUserId, "notification.created",
                notificationMapper.toResponse(savedNotification));

        long unreadCount = notificationRepository.countByUserUserIdAndIsReadFalse(recipientUserId);
        realtimeEventPublisherService.publishUnreadCount(recipientUserId, unreadCount);
    }
}
