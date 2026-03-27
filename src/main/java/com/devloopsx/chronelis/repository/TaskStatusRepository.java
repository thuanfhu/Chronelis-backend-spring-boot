package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    List<TaskStatus> findByProjectIdOrderByPositionAsc(Long projectId);

    Optional<TaskStatus> findByProjectIdAndCodeIgnoreCase(Long projectId, String code);

    boolean existsByProjectIdAndCodeIgnoreCase(Long projectId, String code);

    boolean existsByProjectIdAndPosition(Long projectId, Integer position);
}
