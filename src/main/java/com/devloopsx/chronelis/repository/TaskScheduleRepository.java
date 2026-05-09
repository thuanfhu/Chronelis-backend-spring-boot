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

        List<TaskSchedule> findByTaskAssigneeUserIdAndTaskIsCompletedFalseAndTaskProjectWorkspaceIdInAndScheduledDateBetweenOrderByScheduledStartAsc(
                        String userId,
                        List<Long> workspaceIds,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);
}
