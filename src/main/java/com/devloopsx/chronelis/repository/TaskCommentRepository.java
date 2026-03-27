package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
