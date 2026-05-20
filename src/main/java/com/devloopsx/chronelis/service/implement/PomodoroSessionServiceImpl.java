package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.domain.PomodoroSession;
import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.pomodoro.SavePomodoroSessionRequest;
import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import com.devloopsx.chronelis.dto.response.pomodoro.PomodoroSessionResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.PomodoroSessionRepository;
import com.devloopsx.chronelis.repository.TaskRepository;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.PomodoroSessionService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PomodoroSessionServiceImpl implements PomodoroSessionService {
    PomodoroSessionRepository pomodoroSessionRepository;
    TaskRepository taskRepository;
    CollaborationAccessService collaborationAccessService;
    SecurityUtils securityUtils;

    @Override
    @Transactional
    public PomodoroSessionResponse saveSession(Long taskId, SavePomodoroSessionRequest request) {
        collaborationAccessService.ensureCurrentUserCanAccessTask(taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task không tồn tại"));
        User user = securityUtils.getAuthenticatedUser();

        LocalDateTime endedAt = request.getEndedAt() != null ? request.getEndedAt() : LocalDateTime.now();
        LocalDateTime startedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : endedAt.minusMinutes(request.getDurationMinutes());

        if (startedAt.isAfter(endedAt)) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Thời điểm bắt đầu Pomodoro không được sau thời điểm kết thúc");
        }

        PomodoroSession session = PomodoroSession.builder()
                .task(task)
                .user(user)
                .durationMinutes(request.getDurationMinutes())
                .startedAt(startedAt)
                .endedAt(endedAt)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(pomodoroSessionRepository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PomodoroSessionResponse> getCurrentUserSessionsByTask(Long taskId) {
        collaborationAccessService.ensureCurrentUserCanAccessTask(taskId);
        User user = securityUtils.getAuthenticatedUser();
        return pomodoroSessionRepository.findByTaskIdAndUserUserIdOrderByEndedAtDescCreatedAtDesc(taskId, user.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PomodoroSessionResponse toResponse(PomodoroSession session) {
        User user = session.getUser();
        return PomodoroSessionResponse.builder()
                .id(session.getId())
                .taskId(session.getTask().getId())
                .user(UserSummaryResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .durationMinutes(session.getDurationMinutes())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
