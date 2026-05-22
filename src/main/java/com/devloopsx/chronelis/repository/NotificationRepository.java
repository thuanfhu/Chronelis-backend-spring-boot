package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Page<Notification> findByUserUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  long countByUserUserIdAndIsReadFalse(String userId);

  Optional<Notification> findByIdAndUserUserId(Long notificationId, String userId);

  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
  int markAllAsRead(@Param("userId") String userId);

  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.user.userId = :userId")
  int markOneAsRead(@Param("notificationId") Long notificationId, @Param("userId") String userId);

  void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
