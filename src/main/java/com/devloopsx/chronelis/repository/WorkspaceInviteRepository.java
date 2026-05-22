package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceInvite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {
  List<WorkspaceInvite> findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(Long workspaceId);

  Optional<WorkspaceInvite> findByInviteCode(String inviteCode);

  @Modifying
  @Query(
      "UPDATE WorkspaceInvite wi SET wi.createdBy = :replacementUser WHERE wi.createdBy.userId = :sourceUserId")
  int reassignCreatedBy(
      @Param("sourceUserId") String sourceUserId,
      @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);
}
