package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    void deleteByTaskId(Long taskId);

    @Modifying
    @Query("UPDATE TaskComment tc SET tc.user = :replacementUser WHERE tc.user.userId = :sourceUserId")
    int reassignCommentAuthor(@Param("sourceUserId") String sourceUserId,
            @Param("replacementUser") com.devloopsx.chronelis.domain.User replacementUser);
}
