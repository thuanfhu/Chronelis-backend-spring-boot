package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {
    Page<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId, Pageable pageable);

    @Modifying
    @Query("UPDATE ActivityLog al SET al.actor = :replacementUser WHERE al.actor.userId = :sourceUserId")
    int reassignActor(@Param("sourceUserId") String sourceUserId,
            @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);
}
