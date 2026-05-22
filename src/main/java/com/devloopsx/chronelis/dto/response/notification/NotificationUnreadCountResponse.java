package com.devloopsx.chronelis.dto.response.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationUnreadCountResponse {
  long unreadCount;
}
