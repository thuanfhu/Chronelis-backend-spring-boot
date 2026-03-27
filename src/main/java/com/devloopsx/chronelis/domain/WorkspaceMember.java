package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
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
@Table(name = "workspace_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_members_workspace_user", columnNames = { "workspace_id", "user_id" })
})
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    WorkspaceMemberRoleType role;

    @Column(name = "joined_at", nullable = false)
    LocalDateTime joinedAt;
}
