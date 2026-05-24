package com.devloopsx.chronelis.domain;

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
@Table(
    name = "workspace_teams",
    uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "name"}))
public class WorkspaceTeam {
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  User createdBy;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  LocalDateTime updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
  List<WorkspaceTeamMember> members = new ArrayList<>();
}
