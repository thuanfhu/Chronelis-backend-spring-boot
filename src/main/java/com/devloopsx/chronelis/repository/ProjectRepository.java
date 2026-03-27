package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Page<Project> findByWorkspaceId(Long workspaceId, Pageable pageable);

    boolean existsByWorkspaceIdAndId(Long workspaceId, Long id);
}
