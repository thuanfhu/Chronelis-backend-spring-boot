package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public interface ActivityLogService {
  PaginationResponse listByWorkspace(
      Long workspaceId,
      String actorId,
      ActivityActionType actionType,
      ActivityTargetType targetType,
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      Pageable pageable);

  void createLog(
      Long workspaceId,
      String actorId,
      ActivityActionType actionType,
      ActivityTargetType targetType,
      Long targetId,
      String description);
}
