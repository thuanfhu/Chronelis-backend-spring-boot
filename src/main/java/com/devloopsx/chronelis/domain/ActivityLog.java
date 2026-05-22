package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.ActivityActionType;
import com.devloopsx.chronelis.constant.ActivityTargetType;
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
@Table(name = "activity_logs")
public class ActivityLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", nullable = false)
  User actor;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 50)
  ActivityActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  ActivityTargetType targetType;

  @Column(name = "target_id", nullable = false)
  Long targetId;

  @Column(nullable = false, length = 255)
  String description;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;
}
