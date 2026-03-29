package com.devloopsx.chronelis.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "workspaces")
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 150)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    List<Project> projects = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    List<WorkspaceMember> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    List<ActivityLog> activityLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    List<WorkspaceTeam> teams = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    List<WorkspaceInvite> invites = new ArrayList<>();
}
