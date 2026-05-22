package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.pomodoro.SavePomodoroSessionRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.pomodoro.PomodoroSessionResponse;
import com.devloopsx.chronelis.service.PomodoroSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pomodoro")
@RequiredArgsConstructor
public class PomodoroSessionController {

  private final PomodoroSessionService pomodoroSessionService;

  @PostMapping("/tasks/{taskId}")
  public ResponseEntity<ApiResponse<PomodoroSessionResponse>> saveSession(
      @PathVariable Long taskId,
      @RequestBody @Valid SavePomodoroSessionRequest request,
      HttpServletRequest servletRequest) {
    PomodoroSessionResponse session = pomodoroSessionService.saveSession(taskId, request);
    return ResponseEntity.ok(
        ApiResponse.<PomodoroSessionResponse>builder()
            .message("Lưu phiên Pomodoro thành công")
            .data(session)
            .meta(buildMetaInfo(servletRequest))
            .build());
  }

  @GetMapping("/tasks/{taskId}")
  public ResponseEntity<ApiResponse<List<PomodoroSessionResponse>>> getSessions(
      @PathVariable Long taskId, HttpServletRequest servletRequest) {
    List<PomodoroSessionResponse> sessions =
        pomodoroSessionService.getCurrentUserSessionsByTask(taskId);
    return ResponseEntity.ok(
        ApiResponse.<List<PomodoroSessionResponse>>builder()
            .message("Lấy lịch sử Pomodoro thành công")
            .data(sessions)
            .meta(buildMetaInfo(servletRequest))
            .build());
  }
}
