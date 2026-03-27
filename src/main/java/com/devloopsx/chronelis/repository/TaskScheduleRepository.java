package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long> {
    List<TaskSchedule> findByTaskIdOrderByScheduledStartAsc(Long taskId);

    Page<TaskSchedule> findByTaskProjectIdAndScheduledDateBetween(Long projectId, LocalDate fromDate, LocalDate toDate,
            Pageable pageable);

    Page<TaskSchedule> findByTaskProjectWorkspaceIdAndScheduledDateBetween(Long workspaceId, LocalDate fromDate,
            LocalDate toDate, Pageable pageable);
}
