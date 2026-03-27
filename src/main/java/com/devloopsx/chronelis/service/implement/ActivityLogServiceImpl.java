package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
import com.devloopsx.chronelis.domain.ActivityLog;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.Workspace;
import com.devloopsx.chronelis.dto.response.common.PaginationMeta;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.ActivityLogMapper;
import com.devloopsx.chronelis.repository.ActivityLogRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.repository.WorkspaceRepository;
import com.devloopsx.chronelis.service.ActivityLogService;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogServiceImpl implements ActivityLogService {
    ActivityLogRepository activityLogRepository;
    WorkspaceRepository workspaceRepository;
    UserRepository userRepository;
    ActivityLogMapper activityLogMapper;
    CollaborationAccessService collaborationAccessService;

    @Override
    public PaginationResponse listByWorkspace(Long workspaceId, String actorId, ActivityActionType actionType,
            ActivityTargetType targetType, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable) {
        collaborationAccessService.requireCurrentWorkspaceMember(workspaceId);

        Specification<ActivityLog> spec = (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);

        if (actorId != null && !actorId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actor").get("userId"), actorId));
        }
        if (actionType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actionType"), actionType));
        }
        if (targetType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType));
        }
        if (fromDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }
        if (toDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
        }

        Page<ActivityLog> page = activityLogRepository.findAll(spec, pageable);

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .content(page.getContent().stream().map(activityLogMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public void createLog(Long workspaceId, String actorId, ActivityActionType actionType,
            ActivityTargetType targetType,
            Long targetId, String description) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Workspace không tồn tại"));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        ActivityLog activityLog = ActivityLog.builder()
                .workspace(workspace)
                .actor(actor)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        activityLogRepository.save(activityLog);
    }
}
