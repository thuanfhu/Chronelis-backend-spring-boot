package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByTaskId(Long taskId);

    List<PomodoroSession> findByTaskIdAndUserUserIdOrderByEndedAtDescCreatedAtDesc(Long taskId, String userId);
}
