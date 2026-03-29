package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskCheckItemRepository extends JpaRepository<TaskCheckItem, Long> {
    List<TaskCheckItem> findByTaskIdOrderByPositionAsc(Long taskId);

    long countByTaskId(Long taskId);

    @Query("SELECT COALESCE(MAX(c.position), -1) FROM TaskCheckItem c WHERE c.task.id = :taskId")
    Integer findMaxPositionByTaskId(@Param("taskId") Long taskId);
}
