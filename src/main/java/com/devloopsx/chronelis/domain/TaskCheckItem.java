package com.devloopsx.chronelis.domain;

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
@Table(name = "task_check_items")
public class TaskCheckItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    Task task;

    @Column(nullable = false, length = 200)
    String title;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    Boolean isChecked = false;

    @Column(nullable = false)
    @Builder.Default
    Integer position = 0;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
