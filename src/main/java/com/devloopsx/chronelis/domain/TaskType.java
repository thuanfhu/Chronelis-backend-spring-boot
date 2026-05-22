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
@Table(
    name = "task_types",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "name"}))
public class TaskType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  Workspace workspace;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "goal_id")
  Goal goal;

  @Column(nullable = false, length = 100)
  String name;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(length = 20)
  String color;

  @Column(length = 50)
  String icon;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  LocalDateTime updatedAt;
}
