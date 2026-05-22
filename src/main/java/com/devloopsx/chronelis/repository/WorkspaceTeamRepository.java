package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.WorkspaceTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkspaceTeamRepository extends JpaRepository<WorkspaceTeam, Long> {
  List<WorkspaceTeam> findByWorkspaceIdOrderByNameAsc(Long workspaceId);

  boolean existsByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);

  boolean existsByWorkspaceIdAndNameIgnoreCaseAndIdNot(Long workspaceId, String name, Long id);

  @Modifying
  @Query(
      "UPDATE WorkspaceTeam wt SET wt.createdBy = :replacementUser WHERE wt.createdBy.userId = :sourceUserId")
  int reassignCreatedBy(
      @Param("sourceUserId") String sourceUserId,
      @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);
}
