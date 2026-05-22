package com.devloopsx.chronelis.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "task_schedules")
public class TaskSchedule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  Task task;

  @Column(name = "scheduled_start", nullable = false)
  LocalDateTime scheduledStart;

  @Column(name = "scheduled_end", nullable = false)
  LocalDateTime scheduledEnd;

  @Column(name = "scheduled_date", nullable = false)
  LocalDate scheduledDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  User createdBy;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  LocalDateTime updatedAt;
}
