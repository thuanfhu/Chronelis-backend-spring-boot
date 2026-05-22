package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
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
@Table(name = "workspace_invites")
public class WorkspaceInvite {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_id", nullable = false)
  Workspace workspace;

  @Column(name = "invite_code", nullable = false, unique = true, length = 20)
  String inviteCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_to_assign", nullable = false, length = 30)
  @Builder.Default
  WorkspaceMemberRoleType roleToAssign = WorkspaceMemberRoleType.MEMBER;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  User createdBy;

  @Column(name = "max_uses")
  Integer maxUses;

  @Column(name = "used_count", nullable = false)
  @Builder.Default
  Integer usedCount = 0;

  @Column(name = "expires_at")
  LocalDateTime expiresAt;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  Boolean isActive = true;

  @Column(name = "created_at", nullable = false)
  LocalDateTime createdAt;
}
