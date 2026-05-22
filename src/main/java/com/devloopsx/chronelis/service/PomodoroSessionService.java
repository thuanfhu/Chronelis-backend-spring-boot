package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.pomodoro.SavePomodoroSessionRequest;
import com.devloopsx.chronelis.dto.response.pomodoro.PomodoroSessionResponse;
import java.util.List;

public interface PomodoroSessionService {
  PomodoroSessionResponse saveSession(Long taskId, SavePomodoroSessionRequest request);

  List<PomodoroSessionResponse> getCurrentUserSessionsByTask(Long taskId);
}
