package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.NotificationType;
import com.devloopsx.chronelis.constant.ReferenceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    NotificationType type;

    @Column(nullable = false, length = 200)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 30)
    ReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    Long referenceId;

    @Column(name = "is_read", nullable = false)
    Boolean isRead;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
}
