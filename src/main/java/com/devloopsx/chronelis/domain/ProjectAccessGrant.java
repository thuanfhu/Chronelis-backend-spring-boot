package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "project_access_grants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_access_user", columnNames = { "project_id", "user_id" }),
        @UniqueConstraint(name = "uk_project_access_team", columnNames = { "project_id", "team_id" })
})
public class ProjectAccessGrant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 20)
    ProjectAccessSubjectType subjectType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    WorkspaceTeam team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    ProjectAccessRoleType role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    User grantedBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
