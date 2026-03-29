package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {
    List<WorkspaceInvite> findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(Long workspaceId);

    Optional<WorkspaceInvite> findByInviteCode(String inviteCode);
}
