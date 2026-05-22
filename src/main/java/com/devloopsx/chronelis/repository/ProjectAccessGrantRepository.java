package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import com.devloopsx.chronelis.domain.ProjectAccessGrant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectAccessGrantRepository extends JpaRepository<ProjectAccessGrant, Long> {
  List<ProjectAccessGrant> findByProjectIdOrderByCreatedAtAsc(Long projectId);

  Optional<ProjectAccessGrant> findByIdAndProjectId(Long id, Long projectId);

  Optional<ProjectAccessGrant> findByProjectIdAndUserUserId(Long projectId, String userId);

  Optional<ProjectAccessGrant> findByProjectIdAndTeamId(Long projectId, Long teamId);

  List<ProjectAccessGrant> findByProjectIdAndSubjectTypeAndTeamIdIn(
      Long projectId, ProjectAccessSubjectType subjectType, Collection<Long> teamIds);

  boolean existsByProjectIdAndUserUserId(Long projectId, String userId);

  boolean existsByProjectIdAndTeamId(Long projectId, Long teamId);

  void deleteByProjectId(Long projectId);

  @Query(
      "SELECT DISTINCT pa.project.id FROM ProjectAccessGrant pa WHERE pa.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.USER AND pa.user.userId = :userId")
  List<Long> findProjectIdsByUserGrant(@Param("userId") String userId);

  @Query(
      "SELECT DISTINCT pa.project.id FROM ProjectAccessGrant pa WHERE pa.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.TEAM AND pa.team.id IN :teamIds")
  List<Long> findProjectIdsByTeamGrants(@Param("teamIds") Collection<Long> teamIds);
}
