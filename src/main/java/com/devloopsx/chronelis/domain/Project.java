package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.ProjectStatusType;
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
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    ProjectStatusType status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

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
