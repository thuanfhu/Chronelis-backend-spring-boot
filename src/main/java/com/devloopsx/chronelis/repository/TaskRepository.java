package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
        Page<Task> findByProjectId(Long projectId, Pageable pageable);

        Page<Task> findByGoalId(Long goalId, Pageable pageable);

        List<Task> findByStatusIdOrderByBoardPositionAscIdAsc(Long statusId);

        List<Task> findByStatusIdAndBoardPositionGreaterThanEqualOrderByBoardPositionAscIdAsc(Long statusId,
                        Integer boardPosition);

        List<Task> findByStatusIdAndBoardPositionGreaterThanOrderByBoardPositionAscIdAsc(Long statusId,
                        Integer boardPosition);

        long countByStatusId(Long statusId);

        long countByGoalId(Long goalId);

        long countByGoalIdAndIsCompletedTrue(Long goalId);

        @Query("SELECT COALESCE(MAX(t.boardPosition), -1) FROM Task t WHERE t.status.id = :statusId")
        Integer findMaxBoardPositionByStatusId(@Param("statusId") Long statusId);
}
