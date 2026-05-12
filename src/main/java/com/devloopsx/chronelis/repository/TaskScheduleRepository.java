package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long> {
        List<TaskSchedule> findByTaskIdOrderByScheduledStartAsc(Long taskId);

        void deleteByTaskId(Long taskId);

        @Modifying
        @Query("UPDATE TaskSchedule ts SET ts.createdBy = :replacementUser WHERE ts.createdBy.userId = :sourceUserId")
        int reassignCreatedBy(@Param("sourceUserId") String sourceUserId,
                        @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);

        Page<TaskSchedule> findByTaskProjectIdAndScheduledDateBetween(Long projectId, LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);

        Page<TaskSchedule> findByTaskProjectWorkspaceIdAndScheduledDateBetween(Long workspaceId, LocalDate fromDate,
                        LocalDate toDate, Pageable pageable);

        @Query("""
                        SELECT ts FROM TaskSchedule ts
                        WHERE ts.task.project.workspace.id = :workspaceId
                          AND ts.scheduledDate BETWEEN :fromDate AND :toDate
                          AND (
                              ts.task.project.workspace.owner.userId = :userId
                              OR ts.task.project.visibility = com.devloopsx.chronelis.constant.ProjectVisibilityType.PUBLIC
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = ts.task.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.USER
                                    AND grant.user.userId = :userId
                              )
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = ts.task.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.TEAM
                                    AND EXISTS (
                                        SELECT teamMember.id FROM WorkspaceTeamMember teamMember
                                        WHERE teamMember.team.id = grant.team.id
                                          AND teamMember.user.userId = :userId
                                    )
                              )
                          )
                        """)
        Page<TaskSchedule> findVisibleByWorkspaceCalendar(@Param("workspaceId") Long workspaceId,
                        @Param("userId") String userId,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);

        List<TaskSchedule> findByTaskAssigneeUserIdAndTaskIsCompletedFalseAndTaskProjectWorkspaceIdInAndScheduledDateBetweenOrderByScheduledStartAsc(
                        String userId,
                        List<Long> workspaceIds,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);

        @Query("""
                        SELECT ts FROM TaskSchedule ts
                        WHERE ts.task.assignee.userId = :userId
                          AND ts.task.isCompleted = false
                          AND ts.task.project.workspace.id IN :workspaceIds
                          AND ts.scheduledDate BETWEEN :fromDate AND :toDate
                          AND (
                              ts.task.project.workspace.owner.userId = :userId
                              OR ts.task.project.visibility = com.devloopsx.chronelis.constant.ProjectVisibilityType.PUBLIC
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = ts.task.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.USER
                                    AND grant.user.userId = :userId
                              )
                              OR EXISTS (
                                  SELECT grant.id FROM ProjectAccessGrant grant
                                  WHERE grant.project.id = ts.task.project.id
                                    AND grant.subjectType = com.devloopsx.chronelis.constant.ProjectAccessSubjectType.TEAM
                                    AND EXISTS (
                                        SELECT teamMember.id FROM WorkspaceTeamMember teamMember
                                        WHERE teamMember.team.id = grant.team.id
                                          AND teamMember.user.userId = :userId
                                    )
                              )
                          )
                        ORDER BY ts.scheduledStart ASC
                        """)
        List<TaskSchedule> findVisibleAssignedOpenSchedules(@Param("userId") String userId,
                        @Param("workspaceIds") List<Long> workspaceIds,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);
}
