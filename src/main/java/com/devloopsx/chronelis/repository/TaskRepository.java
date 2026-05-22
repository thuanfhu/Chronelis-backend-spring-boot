package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  Page<Task> findByProjectId(Long projectId, Pageable pageable);

  Page<Task> findByGoalId(Long goalId, Pageable pageable);

  List<Task> findByStatusIdOrderByBoardPositionAscIdAsc(Long statusId);

  List<Task> findByStatusIdAndBoardPositionGreaterThanEqualOrderByBoardPositionAscIdAsc(
      Long statusId, Integer boardPosition);

  List<Task> findByStatusIdAndBoardPositionGreaterThanOrderByBoardPositionAscIdAsc(
      Long statusId, Integer boardPosition);

  long countByStatusId(Long statusId);

  long countByGoalId(Long goalId);

  long countByGoalIdAndIsCompletedTrue(Long goalId);

  @Query("SELECT COALESCE(MAX(t.boardPosition), -1) FROM Task t WHERE t.status.id = :statusId")
  Integer findMaxBoardPositionByStatusId(@Param("statusId") Long statusId);

  @Modifying
  @Query("UPDATE Task t SET t.goal = null WHERE t.goal.id = :goalId")
  int clearGoalReferences(@Param("goalId") Long goalId);

  @Modifying
  @Query("UPDATE Task t SET t.taskType = null WHERE t.taskType.id = :taskTypeId")
  int clearTaskTypeReferences(@Param("taskTypeId") Long taskTypeId);

  @Modifying
  @Query("DELETE FROM Task t WHERE t.project.id IN :projectIds")
  int deleteByProjectIdIn(@Param("projectIds") List<Long> projectIds);

  List<Task> findByAssigneeUserIdAndIsCompletedFalseAndProjectWorkspaceIdInOrderByUpdatedAtDesc(
      String userId, List<Long> workspaceIds, Pageable pageable);

  @Query(
      """
                        SELECT t FROM Task t
                        WHERE t.assignee.userId = :userId
                          AND t.isCompleted = false
                          AND t.project.workspace.id IN :workspaceIds
                          AND (
                              t.project.workspace.owner.userId = :userId
                              OR t.project.visibility = com.devloopsx.chronelis.constant.ProjectVisibilityType.PUBLIC
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = t.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.USER
                                    AND grant.user.userId = :userId
                              )
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = t.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.TEAM
                                    AND EXISTS (
                                        SELECT teamMember.id FROM WorkspaceTeamMember teamMember
                                        WHERE teamMember.team.id = grant.team.id
                                          AND teamMember.user.userId = :userId
                                    )
                              )
                          )
                        ORDER BY t.updatedAt DESC
                        """)
  List<Task> findVisibleAssignedOpenTasks(
      @Param("userId") String userId,
      @Param("workspaceIds") List<Long> workspaceIds,
      Pageable pageable);

  @Modifying
  @Query("UPDATE Task t SET t.assignee = null WHERE t.assignee.userId = :sourceUserId")
  int clearAssigneeReferences(@Param("sourceUserId") String sourceUserId);

  @Modifying
  @Query(
      "UPDATE Task t SET t.createdBy = :replacementUser WHERE t.createdBy.userId = :sourceUserId")
  int reassignCreatedBy(
      @Param("sourceUserId") String sourceUserId,
      @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);

  long countByProjectId(Long projectId);

  long countByProjectIdAndIsCompletedTrue(@Param("projectId") Long projectId);

  @Query(
      value =
          "SELECT DATE(created_at) as day, COUNT(*) as cnt FROM tasks "
              + "WHERE assignee_id = :userId "
              + "AND project_id IN (SELECT id FROM projects WHERE workspace_id IN :workspaceIds) "
              + "AND created_at >= :since "
              + "GROUP BY DATE(created_at) ORDER BY DATE(created_at)",
      nativeQuery = true)
  List<Object[]> countCreatedByDayForUser(
      @Param("userId") String userId,
      @Param("workspaceIds") List<Long> workspaceIds,
      @Param("since") LocalDateTime since);

  @Query(
      value =
          "SELECT DATE(completed_at) as day, COUNT(*) as cnt FROM tasks "
              + "WHERE assignee_id = :userId "
              + "AND project_id IN (SELECT id FROM projects WHERE workspace_id IN :workspaceIds) "
              + "AND is_completed = 1 AND completed_at >= :since "
              + "GROUP BY DATE(completed_at) ORDER BY DATE(completed_at)",
      nativeQuery = true)
  List<Object[]> countCompletedByDayForUser(
      @Param("userId") String userId,
      @Param("workspaceIds") List<Long> workspaceIds,
      @Param("since") LocalDateTime since);

  @Query(
      "SELECT t.priority, SUM(t.estimatedMinutes), COUNT(t) FROM Task t "
          + "WHERE t.assignee.userId = :userId "
          + "AND t.project.workspace.id IN :workspaceIds "
          + "AND t.isCompleted = false GROUP BY t.priority")
  List<Object[]> sumEstimatedByPriorityForUser(
      @Param("userId") String userId, @Param("workspaceIds") List<Long> workspaceIds);

  @Query(
      value =
          "SELECT DATE(created_at) as day, COUNT(*) as cnt FROM tasks "
              + "WHERE project_id = :projectId AND created_at >= :since "
              + "GROUP BY DATE(created_at) ORDER BY DATE(created_at)",
      nativeQuery = true)
  List<Object[]> countCreatedByDayForProject(
      @Param("projectId") Long projectId, @Param("since") LocalDateTime since);

  @Query(
      value =
          "SELECT DATE(completed_at) as day, COUNT(*) as cnt FROM tasks "
              + "WHERE project_id = :projectId AND is_completed = 1 AND completed_at >= :since "
              + "GROUP BY DATE(completed_at) ORDER BY DATE(completed_at)",
      nativeQuery = true)
  List<Object[]> countCompletedByDayForProject(
      @Param("projectId") Long projectId, @Param("since") LocalDateTime since);
}
