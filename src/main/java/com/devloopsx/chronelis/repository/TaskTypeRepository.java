package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
    List<TaskType> findByProjectIdOrderByNameAsc(Long projectId);

    List<TaskType> findByWorkspaceIdOrderByNameAsc(Long workspaceId);

    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);

    boolean existsByProjectIdAndNameIgnoreCaseAndIdNot(Long projectId, String name, Long id);
}
