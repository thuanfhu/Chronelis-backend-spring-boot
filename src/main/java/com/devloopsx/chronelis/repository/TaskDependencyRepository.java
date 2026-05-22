package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.TaskDependency;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
  @Query(
      """
            SELECT dependency
            FROM TaskDependency dependency
            JOIN FETCH dependency.dependsOnTask dependsOnTask
            LEFT JOIN FETCH dependsOnTask.status
            WHERE dependency.task.id = :taskId
            ORDER BY dependency.createdAt ASC, dependency.id ASC
            """)
  List<TaskDependency> findIncomingByTaskId(@Param("taskId") Long taskId);

  @Query(
      """
            SELECT dependency
            FROM TaskDependency dependency
            JOIN FETCH dependency.task task
            LEFT JOIN FETCH task.status
            WHERE dependency.dependsOnTask.id = :taskId
            ORDER BY dependency.createdAt ASC, dependency.id ASC
            """)
  List<TaskDependency> findOutgoingByTaskId(@Param("taskId") Long taskId);

  @Query(
      """
            SELECT dependency
            FROM TaskDependency dependency
            JOIN FETCH dependency.dependsOnTask dependsOnTask
            LEFT JOIN FETCH dependsOnTask.status
            WHERE dependency.task.id IN :taskIds
            """)
  List<TaskDependency> findIncomingByTaskIds(@Param("taskIds") Collection<Long> taskIds);

  @Query(
      """
            SELECT dependency
            FROM TaskDependency dependency
            JOIN FETCH dependency.task task
            LEFT JOIN FETCH task.status
            WHERE dependency.dependsOnTask.id IN :taskIds
            """)
  List<TaskDependency> findOutgoingByTaskIds(@Param("taskIds") Collection<Long> taskIds);

  @Query(
      """
            SELECT dependency
            FROM TaskDependency dependency
            WHERE dependency.task.project.id = :projectId
            """)
  List<TaskDependency> findByProjectId(@Param("projectId") Long projectId);

  @Modifying
  void deleteByTaskIdOrDependsOnTaskId(Long taskId, Long dependsOnTaskId);

  boolean existsByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);
}
