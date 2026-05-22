package com.devloopsx.chronelis.mapper;

import com.devloopsx.chronelis.domain.Notification;
import com.devloopsx.chronelis.dto.response.notification.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  NotificationResponse toResponse(Notification notification);
}
