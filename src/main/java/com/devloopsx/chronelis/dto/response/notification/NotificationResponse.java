package com.devloopsx.chronelis.dto.response.notification;

import com.devloopsx.chronelis.constant.NotificationType;
import com.devloopsx.chronelis.constant.ReferenceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    NotificationType type;
    String title;
    String message;
    ReferenceType referenceType;
    Long referenceId;
    Boolean isRead;
    LocalDateTime createdAt;
}
