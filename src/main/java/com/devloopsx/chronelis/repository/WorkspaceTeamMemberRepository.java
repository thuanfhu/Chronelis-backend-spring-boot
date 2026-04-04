package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceTeamMemberRepository extends JpaRepository<WorkspaceTeamMember, Long> {
    List<WorkspaceTeamMember> findByTeamIdOrderByJoinedAtAsc(Long teamId);

    boolean existsByTeamIdAndUserUserId(Long teamId, String userId);

    Optional<WorkspaceTeamMember> findByTeamIdAndUserUserId(Long teamId, String userId);

    void deleteByTeamIdAndUserUserId(Long teamId, String userId);

    void deleteByTeamId(Long teamId);
}
