package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long>, JpaSpecificationExecutor<Workspace> {
    @Query("SELECT DISTINCT w FROM Workspace w LEFT JOIN w.members m WHERE w.owner.userId = :userId OR m.user.userId = :userId")
    Page<Workspace> findVisibleByUserId(@Param("userId") String userId, Pageable pageable);
}
