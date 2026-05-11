package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkspaceTeamMemberRepository extends JpaRepository<WorkspaceTeamMember, Long> {
    List<WorkspaceTeamMember> findByTeamIdOrderByJoinedAtAsc(Long teamId);

    List<WorkspaceTeamMember> findByTeamIdIn(Collection<Long> teamIds);

    @Query("SELECT wtm.team.id FROM WorkspaceTeamMember wtm WHERE wtm.team.workspace.id = :workspaceId AND wtm.user.userId = :userId")
    List<Long> findTeamIdsByWorkspaceIdAndUserId(@Param("workspaceId") Long workspaceId,
            @Param("userId") String userId);

    boolean existsByTeamIdAndUserUserId(Long teamId, String userId);

    Optional<WorkspaceTeamMember> findByTeamIdAndUserUserId(Long teamId, String userId);

    void deleteByTeamIdAndUserUserId(Long teamId, String userId);

    void deleteByTeamId(Long teamId);
}
