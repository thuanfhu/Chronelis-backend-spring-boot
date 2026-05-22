package com.devloopsx.chronelis.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "pomodoro_sessions")
public class PomodoroSession {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  Task task;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(name = "duration_minutes", nullable = false)
  Integer durationMinutes;

  @Column(name = "started_at")
  LocalDateTime startedAt;

  @Column(name = "ended_at")
  LocalDateTime endedAt;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;
}
