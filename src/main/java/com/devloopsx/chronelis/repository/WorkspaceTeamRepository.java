package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceTeamRepository extends JpaRepository<WorkspaceTeam, Long> {
    List<WorkspaceTeam> findByWorkspaceIdOrderByNameAsc(Long workspaceId);

    boolean existsByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);

    boolean existsByWorkspaceIdAndNameIgnoreCaseAndIdNot(Long workspaceId, String name, Long id);
}
