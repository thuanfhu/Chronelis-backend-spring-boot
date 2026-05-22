package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import com.devloopsx.chronelis.domain.WorkspaceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
  Optional<WorkspaceMember> findByWorkspaceIdAndUserUserId(Long workspaceId, String userId);

  boolean existsByWorkspaceIdAndUserUserId(Long workspaceId, String userId);

  List<WorkspaceMember> findByUserUserId(String userId);

  List<WorkspaceMember> findByWorkspaceIdOrderByJoinedAtAsc(Long workspaceId);

  long countByWorkspaceIdAndRole(Long workspaceId, WorkspaceMemberRoleType role);

  void deleteByWorkspaceIdAndUserUserId(Long workspaceId, String userId);
}
