package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.ProjectStatusType;
import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "projects")
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  Workspace workspace;

  @Column(nullable = false, length = 150)
  String name;

  @Column(columnDefinition = "TEXT")
  String imageUrl;

  @Column(columnDefinition = "TEXT")
  String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  ProjectStatusType status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  ProjectVisibilityType visibility = ProjectVisibilityType.PUBLIC;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  User createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_user_id")
  User managerUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_team_id")
  WorkspaceTeam managerTeam;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  LocalDateTime updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  List<Goal> goals = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  List<TaskStatus> taskStatuses = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  List<Task> tasks = new ArrayList<>();
}
