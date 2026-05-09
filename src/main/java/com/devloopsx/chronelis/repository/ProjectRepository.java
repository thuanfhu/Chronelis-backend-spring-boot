package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Page<Project> findByWorkspaceId(Long workspaceId, Pageable pageable);

    @Query("SELECT p.id FROM Project p WHERE p.workspace.id = :workspaceId")
    List<Long> findIdsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    boolean existsByWorkspaceIdAndId(Long workspaceId, Long id);

    @Modifying
    @Query("UPDATE Project p SET p.createdBy = :replacementUser WHERE p.createdBy.userId = :sourceUserId")
    int reassignCreatedBy(@Param("sourceUserId") String sourceUserId,
            @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);
}
