package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long>, JpaSpecificationExecutor<Goal> {
    Page<Goal> findByProjectId(Long projectId, Pageable pageable);
}
